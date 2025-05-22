package deu.controller.business;

import deu.model.dto.request.command.UserManagementCommandRequest;
import deu.model.dto.request.data.user.*;
import deu.model.dto.response.BasicResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserManagementClientController {

    // Singleton 인스턴스
    private static final UserManagementClientController instance = new UserManagementClientController();

    private UserManagementClientController() {}

    public static UserManagementClientController getInstance() {
        return instance;
    }

    // 사용자 정보 수정 처리
    public BasicResponse updateUser(String number, String password, String name, String major) {
        try (
                Socket socket = new Socket("localhost", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            UserDataModificationRequest userDataModificationRequest = new UserDataModificationRequest(number, password, name, major);
            UserManagementCommandRequest req = new UserManagementCommandRequest("사용자 수정", userDataModificationRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 사용자 삭제 처리
    public BasicResponse deleteUser(String number) {
        try (
                Socket socket = new Socket("localhost", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            DeleteRequest deleteRequest = new DeleteRequest(number);
            UserManagementCommandRequest req = new UserManagementCommandRequest("사용자 삭제", deleteRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 사용자 단일 조회 처리
    public BasicResponse findUser(String number) {
        try (
                Socket socket = new Socket("localhost", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            FindRequest findRequest = new FindRequest(number);
            UserManagementCommandRequest req = new UserManagementCommandRequest("사용자 조회", findRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 전체 사용자 목록 조회 처리
    public BasicResponse findAllUsers() {
        try (
                Socket socket = new Socket("localhost", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            UserManagementCommandRequest req = new UserManagementCommandRequest("전체 사용자 조회", null);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }
}
