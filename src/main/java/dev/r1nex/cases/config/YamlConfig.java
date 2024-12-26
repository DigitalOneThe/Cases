package dev.r1nex.cases.config;

import dev.r1nex.cases.Cases;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class YamlConfig {

    private final Cases plugin;
    private final HashMap<String, YamlConfiguration> configs = new HashMap<>();

    public YamlConfig(Cases plugin) {
        if (!new File(plugin.getDataFolder() + File.separator + "config.yml").exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
        }
        this.plugin = plugin;
    }

    public void loadYaml(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.options().copyDefaults(true);
        try {
            yamlConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        StringBuilder stringBuilder = new StringBuilder(path);
        String resultYaml = stringBuilder.delete(0, 14).toString();

        configs.put(resultYaml, yamlConfiguration);
    }

    public YamlConfiguration getYaml(String file) {
        if (configs.get(file) != null) return configs.get(file);
        return null;
    }
}
