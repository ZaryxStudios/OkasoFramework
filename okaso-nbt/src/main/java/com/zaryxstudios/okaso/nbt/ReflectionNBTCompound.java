package com.zaryxstudios.okaso.nbt;

import com.zaryxstudios.okaso.common.nbt.NBTCompound;

import java.lang.reflect.Method;
import java.util.Set;

public class ReflectionNBTCompound implements NBTCompound {

    private static final String NMS_TAG = "net.minecraft.server";
    private static final String NMS_CLASS = "NBTTagCompound";

    private final Object handle; 

    public ReflectionNBTCompound(Object handle) {
        this.handle = handle;
    }

    public Object getHandle() {
        return handle;
    }

    @Override
    public String getString(String key) {
        return (String) invoke("getString", key);
    }

    @Override
    public int getInt(String key) {
        return (int) invoke("getInt", key);
    }

    @Override
    public double getDouble(String key) {
        return (double) invoke("getDouble", key);
    }

    @Override
    public long getLong(String key) {
        return (long) invoke("getLong", key);
    }

    @Override
    public short getShort(String key) {
        return (short) invoke("getShort", key);
    }

    @Override
    public byte getByte(String key) {
        return (byte) invoke("getByte", key);
    }

    @Override
    public boolean getBoolean(String key) {
        return (boolean) invoke("getBoolean", key);
    }

    @Override
    public int[] getIntArray(String key) {
        return (int[]) invoke("getIntArray", key);
    }

    @Override
    public byte[] getByteArray(String key) {
        return (byte[]) invoke("getByteArray", key);
    }

    @Override
    public NBTCompound getCompound(String key) {
        Object compound = invoke("getCompound", key);
        if (compound != null) {
            return new ReflectionNBTCompound(compound);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getKeys() {
        try {
            Method getKeys = handle.getClass().getMethod("getKeys");
            return (Set<String>) getKeys.invoke(handle);
        } catch (Exception e) {
            try {
                Method c = handle.getClass().getMethod("c");
                return (Set<String>) c.invoke(handle);
            } catch (Exception e2) {
                return java.util.Collections.emptySet();
            }
        }
    }

    @Override
    public void setString(String key, String value) {
        invoke("setString", key, value);
    }

    @Override
    public void setInt(String key, int value) {
        invoke("setInt", key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        invoke("setDouble", key, value);
    }

    @Override
    public void setLong(String key, long value) {
        invoke("setLong", key, value);
    }

    @Override
    public void setShort(String key, short value) {
        invoke("setShort", key, value);
    }

    @Override
    public void setByte(String key, byte value) {
        invoke("setByte", key, value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        invoke("setBoolean", key, value);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        invoke("setIntArray", key, value);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        invoke("setByteArray", key, value);
    }

    @Override
    public void setCompound(String key, NBTCompound compound) {
        if (compound instanceof ReflectionNBTCompound) {
            invoke("set", key, ((ReflectionNBTCompound) compound).getHandle());
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            Method hasKey = handle.getClass().getMethod("hasKey", String.class);
            return (boolean) hasKey.invoke(handle, key);
        } catch (Exception e) {
            try {
                Method d = handle.getClass().getMethod("d", String.class);
                return (boolean) d.invoke(handle, key);
            } catch (Exception e2) {
                return false;
            }
        }
    }

    @Override
    public void remove(String key) {
        try {
            Method remove = handle.getClass().getMethod("remove", String.class);
            remove.invoke(handle, key);
        } catch (Exception e) {
            try {
                Method a = handle.getClass().getMethod("a", String.class);
                a.invoke(handle, key);
            } catch (Exception e2) {
            }
        }
    }

    @Override
    public float getFloat(String key) {
        Object result = invoke("getFloat", key);
        return result instanceof Float ? (float) result : 0.0f;
    }

    @Override
    public void setFloat(String key, float value) {
        invoke("setFloat", key, value);
    }

    @Override
    public void clear() {
        for (String key : getKeys()) {
            remove(key);
        }
    }

    @Override
    public boolean isEmpty() {
        return getKeys().isEmpty();
    }

    @Override
    public int size() {
        return getKeys().size();
    }

    private Object invoke(String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
                if (paramTypes[i] == Integer.class) paramTypes[i] = int.class;
                else if (paramTypes[i] == Double.class) paramTypes[i] = double.class;
                else if (paramTypes[i] == Long.class) paramTypes[i] = long.class;
                else if (paramTypes[i] == Short.class) paramTypes[i] = short.class;
                else if (paramTypes[i] == Byte.class) paramTypes[i] = byte.class;
                else if (paramTypes[i] == Boolean.class) paramTypes[i] = boolean.class;
            }
            Method method = handle.getClass().getMethod(methodName, paramTypes);
            return method.invoke(handle, args);
        } catch (Exception e) {
            return null;
        }
    }
}
