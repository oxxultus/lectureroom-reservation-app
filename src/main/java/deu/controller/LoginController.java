package deu.controller;

import deu.dto.BasicResponse;
import deu.dto.LoginRequest;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class LoginController {
    public void login(String id, String pw) {
        try (
                Socket socket = new Socket("localhost", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            LoginRequest req = new LoginRequest(id, pw);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                System.out.println("서버 응답: " + r.message); // ✅ 콘솔 출력
                if (r.success) {
                    System.out.println("→ 로그인 성공 후 처리 로직 여기에 작성");
                }
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
    }
}