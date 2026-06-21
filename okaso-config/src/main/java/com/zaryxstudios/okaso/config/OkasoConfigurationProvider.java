package com.zaryxstudios.okaso.config;

import com.zaryxstudios.okaso.common.config.ConfigurationProvider;
import com.zaryxstudios.okaso.common.config.ConfigurationSection;

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

public class OkasoConfigurationProvider implements ConfigurationProvider {

    private final Yaml yaml;
    private final ObjectMapper mapper;

    public OkasoConfigurationProvider() {
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
    public ConfigurationSection load(File file) {
        if (file == null || !file.exists()) {
            return new OkasoConfigurationSection();
        }

        String name = file.getName().toLowerCase();
        try (InputStream is = new FileInputStream(file)) {
            if (name.endsWith(".json")) {
                Map<String, Object> raw = mapper.readValue(is, LinkedHashMap.class);
                return new OkasoConfigurationSection(raw);
            } else {
                Object loaded = yaml.load(is);
                if (loaded instanceof Map) {
                    return new OkasoConfigurationSection((Map<String, Object>) loaded);
                }
                return new OkasoConfigurationSection();
            }
        } catch (IOException e) {
            return new OkasoConfigurationSection();
        }
    }

    @Override
    public void save(ConfigurationSection section, File file) {
        if (section == null || file == null) return;

        if (!(section instanceof OkasoConfigurationSection)) {
            throw new IllegalArgumentException(
                "Cannot save section of type " + section.getClass().getName());
        }

        Map<String, Object> data = ((OkasoConfigurationSection) section).getRaw();

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
