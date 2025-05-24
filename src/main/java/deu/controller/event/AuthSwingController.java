package deu.controller.event;

import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.view.Auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;

public class AuthSwingController {
    private final Auth view;
    private final UserClientController userController;

    public AuthSwingController(Auth view) {
        this.view = view;
        this.userController = UserClientController.getInstance();

        // 이벤트 연결
        view.addLoginListener(this::handleLogin);
        view.addSignupListener(this::handleSignup);
        view.addSwitchToSignupListener(this::switchToSignup);
        view.addSwitchToLoginListener(this::switchToLogin);
        view.addGithubServerButtonListener(this::handleGithubServer);
        view.addGithubClientButtonListener(this::handleGithubClient);
    }

    // 로그인 버튼 기능
    private void handleLogin(ActionEvent e) {
        String id = view.getLoginId();
        String pw = view.getLoginPassword();
        BasicResponse res = userController.login(id, pw);

        if (res != null && res.code.equals("200")) {
            view.transitionToHome(id, pw);
        } else if (res != null && res.code.equals("401")) {
            view.showError("로그인 실패: " + (res != null ? res.data : "비밀번호 오류"));
        } else {
            view.showError("로그인 실패: " + (res != null ? res.data : "존재하지 않는 사용자(아이디 오류)"));
        }
    }

    // 회원 가입 버튼 기능
    private void handleSignup(ActionEvent e) {
        String id = view.getSignupId();
        String pw = view.getSignupPassword();
        String name = view.getSignupName();
        String major = view.getSignupMajor();
        BasicResponse res = userController.signup(id, pw, name, major);

        if (res != null && res.code.equals("200")) {
            view.showSuccess("회원가입 성공!");
            view.switchToLoginPanel();
        } else {
            view.showError("회원가입 실패: " + (res != null ? res.data : "서버 오류"));
        }
    }

    // 깃허브 이동 버튼 기능
    private void handleGithubServer(ActionEvent e){
        openWebpage("https://github.com/oxxultus/lectureroom-reservation-server");
    }
    private void handleGithubClient(ActionEvent e){
        openWebpage("https://github.com/oxxultus/lectureroom-reservation-app");
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 회원 가입 패널 전환 - 수정 금지
    private void switchToSignup(ActionEvent e) {
        view.switchToSignupPanel();
    }

    // 로그인 패널 전환 - 수정 금지
    private void switchToLogin(ActionEvent e) {
        view.switchToLoginPanel();
    }

    // 브라우저 여는 메서드 - 수정금지
    private void openWebpage(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "브라우저를 열 수 없습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}