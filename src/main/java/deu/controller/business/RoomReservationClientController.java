package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import lombok.Getter;

public class RoomReservationClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final RoomReservationClientController instance = new RoomReservationClientController();

    private RoomReservationClientController() {}
}
