package deu.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigLoader {
    public static Config load() {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream("config.yaml")) {
            return yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("YAML 로딩 실패", e);
        }
    }
}
