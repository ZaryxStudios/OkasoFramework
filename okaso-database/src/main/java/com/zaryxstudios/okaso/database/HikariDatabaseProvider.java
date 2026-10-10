package com.zaryxstudios.okaso.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;

public class HikariDatabaseProvider {

    private static final Logger LOGGER = Logger.getLogger(HikariDatabaseProvider.class.getName());

    @Getter
    private final HikariDataSource dataSource;
    private final AtomicBoolean initialized;
    private final AtomicLong queryCount;
    private final AtomicLong errorCount;
    private final AtomicLong totalQueryTimeMs;
    private final ExecutorService asyncExecutor;

    public HikariDatabaseProvider(String jdbcUrl, String username, String password, int poolSize) {
        this(jdbcUrl, username, password, poolSize, new Properties());
    }

    public HikariDatabaseProvider(String jdbcUrl, String username, String password, int poolSize, Properties extraProps) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);
        config.setMaximumPoolSize(poolSize > 0 ? poolSize : 10);
        config.setMinimumIdle(Math.min(2, poolSize > 0 ? poolSize : 10));
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        if (extraProps != null) {
            extraProps.forEach((k, v) -> config.addDataSourceProperty(k.toString(), v.toString()));
        }

        this.dataSource = new HikariDataSource(config);
        this.initialized = new AtomicBoolean(true);
        this.queryCount = new AtomicLong(0);
        this.errorCount = new AtomicLong(0);
        this.totalQueryTimeMs = new AtomicLong(0);
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Okaso-DB-Async");
            t.setDaemon(true);
            return t;
        });
        
        registerMBean();
    }

    public HikariDatabaseProvider(String jdbcUrl, String username, String password) {
        this(jdbcUrl, username, password, 10);
    }

    public HikariDatabaseProvider(String jdbcUrl, String username, String password, Properties extraProps) {
        this(jdbcUrl, username, password, 10, extraProps);
    }

    public List<Map<String, Object>> query(String sql, Object... params) {
        long start = System.currentTimeMillis();
        try {
            List<Map<String, Object>> rows = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = prepare(conn, sql, params);
                 ResultSet rs = stmt.executeQuery()) {
                int colCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            throw new RuntimeException("Database query failed: " + sql, e);
        } finally {
            queryCount.incrementAndGet();
            totalQueryTimeMs.addAndGet(System.currentTimeMillis() - start);
        }
    }

    public <T> List<T> query(String sql, Function<ResultSet, T> mapper, Object... params) {
        long start = System.currentTimeMillis();
        try {
            List<T> rows = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = prepare(conn, sql, params);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapper.apply(rs));
                }
                return rows;
            }
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            throw new RuntimeException("Database query failed: " + sql, e);
        } finally {
            queryCount.incrementAndGet();
            totalQueryTimeMs.addAndGet(System.currentTimeMillis() - start);
        }
    }

    public Optional<Map<String, Object>> queryOne(String sql, Object... params) {
        return query(sql, params).stream().findFirst();
    }

    public <T> Optional<T> queryOne(String sql, Function<ResultSet, T> mapper, Object... params) {
        return query(sql, mapper, params).stream().findFirst();
    }

    public int execute(String sql, Object... params) {
        long start = System.currentTimeMillis();
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepare(conn, sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            throw new RuntimeException("Database execute failed: " + sql, e);
        } finally {
            queryCount.incrementAndGet();
            totalQueryTimeMs.addAndGet(System.currentTimeMillis() - start);
        }
    }

    public int[] executeBatch(String sql, java.util.List<Object[]> batchParams) {
        long start = System.currentTimeMillis();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.addBatch();
            }
            return stmt.executeBatch();
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            throw new RuntimeException("Database batch execute failed: " + sql, e);
        } finally {
            queryCount.incrementAndGet();
            totalQueryTimeMs.addAndGet(System.currentTimeMillis() - start);
        }
    }

    public void transaction(Consumer<Connection> consumer) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            consumer.accept(conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    public <T> T transaction(Function<Connection, T> function) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            T result = function.apply(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    public CompletableFuture<List<Map<String, Object>>> queryAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> query(sql, params), asyncExecutor);
    }

    public CompletableFuture<Integer> executeAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> execute(sql, params), asyncExecutor);
    }

    public CompletableFuture<int[]> executeBatchAsync(String sql, List<Object[]> batchParams) {
        return CompletableFuture.supplyAsync(() -> executeBatch(sql, batchParams), asyncExecutor);
    }

    public boolean isAlive() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<String, Object> getHealthInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("alive", isAlive());
        info.put("poolSize", dataSource.getMaximumPoolSize());
        info.put("activeConnections", getPoolMetric("ActiveConnections"));
        info.put("idleConnections", getPoolMetric("IdleConnections"));
        info.put("totalConnections", getPoolMetric("TotalConnections"));
        info.put("threadsAwaitingConnection", getPoolMetric("ThreadsAwaitingConnection"));
        info.put("queryCount", queryCount.get());
        info.put("errorCount", errorCount.get());
        info.put("avgQueryTimeMs", queryCount.get() > 0 ? totalQueryTimeMs.get() / queryCount.get() : 0);
        return info;
    }

    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            info.put("productName", meta.getDatabaseProductName());
            info.put("productVersion", meta.getDatabaseProductVersion());
            info.put("driverName", meta.getDriverName());
            info.put("driverVersion", meta.getDriverVersion());
            info.put("url", meta.getURL());
            info.put("userName", meta.getUserName());
        } catch (SQLException e) {
            info.put("error", e.getMessage());
        }
        return info;
    }

    public void close() {
        if (initialized.compareAndSet(true, false)) {
            asyncExecutor.shutdown();
            dataSource.close();
            unregisterMBean();
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }

    private int getPoolMetric(String metric) {
        try {
            Object poolMXBean = dataSource.getHikariPoolMXBean();
            if (poolMXBean != null) {
                Method method = poolMXBean.getClass().getMethod("get" + metric);
                Object result = method.invoke(poolMXBean);
                return result instanceof Number ? ((Number) result).intValue() : 0;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void registerMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.zaryxstudios.okaso.database:type=HikariDatabaseProvider");
            if (!mbs.isRegistered(name)) {
                mbs.registerMBean(new HikariDatabaseProviderMXBean() {
                    @Override public long getQueryCount() {
                        return queryCount.get();
                    }
                    @Override public long getErrorCount() {
                        return errorCount.get();
                    }
                    @Override public long getTotalQueryTimeMs() {
                        return totalQueryTimeMs.get();
                    }
                    @Override public double getAvgQueryTimeMs() {
                        return queryCount.get() > 0 ? (double) totalQueryTimeMs.get() / queryCount.get() : 0;
                    }
                    @Override public int getPoolSize() {
                        return dataSource.getMaximumPoolSize();

                    }
                    @Override public int getActiveConnections() {
                        return dataSource.getHikariPoolMXBean().getActiveConnections();
                    }
                    @Override public int getIdleConnections() {
                        return dataSource.getHikariPoolMXBean().getIdleConnections();
                    }
                    @Override public int getTotalConnections() {
                        return dataSource.getHikariPoolMXBean().getTotalConnections();
                    }
                    @Override public int getThreadsAwaitingConnection() {
                        return dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                    }
                    @Override public boolean isAlive() {
                        return isAlive();
                    }
                }, name);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to register MBean", e);
        }
    }

    private void unregisterMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.zaryxstudios.okaso.database:type=HikariDatabaseProvider");
            if (mbs.isRegistered(name)) {
                mbs.unregisterMBean(name);
            }
        } catch (Exception ignored) {}
    }

    public interface HikariDatabaseProviderMXBean {
        long getQueryCount();
        long getErrorCount();
        long getTotalQueryTimeMs();
        double getAvgQueryTimeMs();
        int getPoolSize();
        int getActiveConnections();
        int getIdleConnections();
        int getTotalConnections();
        int getThreadsAwaitingConnection();
        boolean isAlive();
    }
}
