package com.zaryxstudios.okaso.config;

import com.zaryxstudios.okaso.common.config.OkasoConfigurationProvider;
import com.zaryxstudios.okaso.common.config.OkasoConfigurationSection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultConfigurationProvider implements OkasoConfigurationProvider {

    private final Yaml yaml;
    private final ObjectMapper mapper;

    public DefaultConfigurationProvider() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OkasoConfigurationSection load(File file) {
        if (file == null || !file.exists()) {
            return new DefaultConfigurationSection();
        }

        String name = file.getName().toLowerCase();
        try (InputStream is = new FileInputStream(file)) {
            if (name.endsWith(".json")) {
                Map<String, Object> raw = mapper.readValue(is, LinkedHashMap.class);
                return new DefaultConfigurationSection(raw);
            } else {
                Object loaded = yaml.load(is);
                if (loaded instanceof Map) {
                    return new DefaultConfigurationSection((Map<String, Object>) loaded);
                }
                return new DefaultConfigurationSection();
            }
        } catch (IOException e) {
            return new DefaultConfigurationSection();
        }
    }

    @Override
    public void save(OkasoConfigurationSection section, File file) {
        if (section == null || file == null) return;

        if (!(section instanceof DefaultConfigurationSection)) {
            throw new IllegalArgumentException(
                "Cannot save section of type " + section.getClass().getName());
        }

        Map<String, Object> data = ((DefaultConfigurationSection) section).getRaw();

        String name = file.getName().toLowerCase();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            if (name.endsWith(".json")) {
                mapper.writeValue(writer, data);
            } else {
                yaml.dump(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
