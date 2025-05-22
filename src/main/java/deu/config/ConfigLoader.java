package deu.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigLoader {

    private static volatile Config configInstance;

    // private 생성자: 외부에서 인스턴스화 방지
    private ConfigLoader() {}

    // 전역 접근 메서드
    public static Config getConfig() {
        if (configInstance == null) {
            synchronized (ConfigLoader.class) {
                if (configInstance == null) { // double-checked locking
                    configInstance = load();
                }
            }
        }
        return configInstance;
    }

    // 실제 YAML 로드 로직
    private static Config load() {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream("config.yaml")) {
            return yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("YAML 설정 로딩 실패", e);
        }
    }
}