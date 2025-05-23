package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.command.ReservationCommandRequest;
import deu.model.dto.request.data.reservation.DeleteRoomReservationRequest;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.RoomReservation;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RoomReservationClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final RoomReservationClientController instance = new RoomReservationClientController();

    private RoomReservationClientController() {}

    // 예약 신청
    public BasicResponse addRoomReservation(RoomReservation roomReservation) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            ReservationCommandRequest req = new ReservationCommandRequest("예약 요청", roomReservation);
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

    // 예약 수정
    public BasicResponse modifyRoomReservation(RoomReservation roomReservation) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            ReservationCommandRequest req = new ReservationCommandRequest("예약 수정", roomReservation);
            out.writeObject(req);

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

    // 개인별 예약 삭제 TODO: String number, String id를 감싸는 DTO 추가 해야됨, number 와 id에 해당하는 예약의 number가 동일하면삭제
    public BasicResponse deleteRoomReservation(String number, String roomReservationId) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            DeleteRoomReservationRequest deleteRoomReservationRequest = new DeleteRoomReservationRequest(number, roomReservationId);
            ReservationCommandRequest req = new ReservationCommandRequest("예약 삭제", deleteRoomReservationRequest);
            out.writeObject(req);

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

    // 개인별 주간 예약 불러오기 (당일 ~ +6일) TODO: RoomReservation[7][13]
    public BasicResponse weekRoomReservationByUserNumber(String number) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            ReservationCommandRequest req = new ReservationCommandRequest("사용자 예약 조회", number);
            out.writeObject(req);

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

    // 어래 기능은 관리자에서도 동일하다.=======================================================================================

    // 건물 강의실별 주간 예약 불러오기 (당일 +6일 까지) TODO: RoomReservation[7][13]
    public BasicResponse weekRoomReservationByLectureroom(String building, String floor, String lectureroom) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            RoomReservationRequest roomReservationRequest = new RoomReservationRequest(building, floor, lectureroom);
            ReservationCommandRequest req = new ReservationCommandRequest("강의실 예약 조회", roomReservationRequest);
            out.writeObject(req);

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
