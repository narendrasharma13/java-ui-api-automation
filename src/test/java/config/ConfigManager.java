package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import utils.Constants;
import utils.FileUtils;

/**
 * Thread-safe singleton for centralized configuration management.
 *
 * Lookup order for every key: JVM system property (-Dkey=value) > environment
 * variable (key upper-cased, dots replaced by underscores) > local.properties
 * (git-ignored, for secrets) > config.properties.
 */
public final class ConfigManager
{

    private static volatile ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        loadConfiguration();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadConfiguration() {
        try {
            loadPropertiesFromClasspath(Constants.DEFAULT_CONFIG);
            loadPropertiesFromClasspath(Constants.LOCAL_CONFIG);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load configuration", e);
        }
        FileUtils.ensureDirectoryExists(get("screenshot.path", "screenshots"));
    }

    private void loadPropertiesFromClasspath(String resourcePath) throws IOException {
        try (InputStream inputStream = FileUtils.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return;
            }
            Properties loaded = new Properties();
            loaded.load(inputStream);
            properties.putAll(loaded);
        }
    }

    private String lookup(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        return value == null ? null : value.trim();
    }

    public String get(String key) {
        String value = lookup(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing configuration value for key: " + key);
        }
        return value;
    }

    public String get(String key, String defaultValue) {
        String value = lookup(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public long getLong(String key, long defaultValue) {
        return Long.parseLong(get(key, String.valueOf(defaultValue)));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public String getUrl() {
        return get("url");
    }

    public String getBaseUrl() {
        return get("baseUrl");
    }

    public String getApiKey() {
        return get("apiKey");
    }

    public String getBrowser() {
        return get(Constants.BROWSER_PROPERTY, Constants.CHROME).toLowerCase();
    }

    public boolean isHeadless() {
        return getBoolean(Constants.HEADLESS_PROPERTY, false);
    }

    public boolean isMobileEmulation() {
        return getBoolean("mobile.emulation", false);
    }

    public String getDeviceName() {
        return get("mobile.device", "iPhone 14 Pro Max");
    }

    public int getTimeout() {
        return getInt("timeout", 30);
    }

    public int getRetries() {
        return getInt("retries", 2);
    }

    public long getPollingInterval() {
        return getLong("polling.interval", 500L);
    }
}
