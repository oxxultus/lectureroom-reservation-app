package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.command.ReservationCommandRequest;
import deu.model.dto.request.data.lecture.LectureRequest;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.RoomReservation;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RoomReservationManagementClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final RoomReservationManagementClientController instance = new RoomReservationManagementClientController();

    private RoomReservationManagementClientController() {}
    // 예약 수정
    public BasicResponse ModifyRoomReservation(RoomReservation roomReservation) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // LectureRequest lectureRequest = new LectureRequest(building, floor, lectureroom);
            // LectureCommandRequest req = new LectureCommandRequest("주간 강의 조회", lectureRequest);
            // out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                // System.out.println("서버 응답: " + r.data);
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 관리자 예약 삭제
    public BasicResponse DeleteRoomReservation(String id) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // LectureRequest lectureRequest = new LectureRequest(building, floor, lectureroom);
            // LectureCommandRequest req = new LectureCommandRequest("주간 강의 조회", lectureRequest);
            //  out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                // System.out.println("서버 응답: " + r.data);
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 예약 상태가 "대기" 인 모든 예약 내역 반환
    public BasicResponse FindAllRoomReservation(String id) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // LectureRequest lectureRequest = new LectureRequest(building, floor, lectureroom);
            // LectureCommandRequest req = new LectureCommandRequest("주간 강의 조회", lectureRequest);
            //  out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                // System.out.println("서버 응답: " + r.data);
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 예약 상태 변경 "대기 -> 완료"
    public BasicResponse ChangeRoomReservationStatus(String id) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // LectureRequest lectureRequest = new LectureRequest(building, floor, lectureroom);
            // LectureCommandRequest req = new LectureCommandRequest("주간 강의 조회", lectureRequest);
            //  out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                // System.out.println("서버 응답: " + r.data);
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

}
