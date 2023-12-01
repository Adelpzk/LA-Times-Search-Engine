package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties prop = new Properties();
    private static boolean loaded = false;

    public void loadProperties() {
        if (!loaded) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new IOException("Unable to find config.properties");
                }
                prop.load(input);
                loaded = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Error loading properties", ex);
            }
        }
    }

    public static String getProperty(String key) {
        ConfigLoader configLoader = new ConfigLoader();
        if (!loaded) {
            configLoader.loadProperties();
        }
        return prop.getProperty(key);
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        ConfigLoader configLoader = new ConfigLoader();
        if (!loaded) {
            configLoader.loadProperties();
        }
        return Boolean.parseBoolean(prop.getProperty(key, String.valueOf(defaultValue)));
    }
}
