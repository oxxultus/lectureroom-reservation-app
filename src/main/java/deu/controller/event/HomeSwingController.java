package deu.controller.event;

import deu.controller.business.RoomReservationClientController;
import deu.controller.business.RoomReservationManagementClientController;
import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.RoomReservation;
import deu.view.*;
import deu.view.custom.PanelRound;
import deu.view.custom.RoundReservationInformationButton;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class HomeSwingController {

    private final Home view;
    private final UserClientController userClientController;
    private final RoomReservationManagementClientController roomReservationManagementClientController;
    private final RoomReservationClientController roomReservationClientController;

    public HomeSwingController(Home view) {
        this.view = view;
        this.userClientController = UserClientController.getInstance();
        this.roomReservationManagementClientController = RoomReservationManagementClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();

        // 이벤트 연결
        view.addLogoutListener(this::handleLogout);
        view.addReservationMenuListener(this::showReservationPanel);
        view.addMyReservationMenuListener(this::showMainPanel);
        view.addManagementMenuListener(this::showManagerMenu);
        view.addUserManagementListener(this::showUserManagerManagement);
        view.addReservationManagementListener(this::showReservationManagement);
        view.addCommonMenuListener(this::showCommonMenu);
        view.addDeleteReservationListner(this::deleteReservation);

        view.addMyReservationListInitListener(createMyReservationListInitListener());
        view.addUserReservationCalendarInitListener(createUserReservationCalendarInitListener());
        view.addUserProfileInitListner(createUserProfileInitListener());
    }

    // 개인 별 주간 예약 시간표를 확인하는 기능 =================================================================================

    // 로그인 사용자 예약 정보를 캘린더에 갱신 하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void refreshUserReservationCalendar() {
        view.getCalendar().setVisible(false);
        RoomReservation[][] data = new RoomReservation[7][13];

        // 1. 더미 예약 정보 객체 생성 (7일 x 13교시)
        BasicResponse response = roomReservationClientController.weekRoomReservationByUserNumber(view.getUserNumber());
        if(!response.code.equals("200")){
            JOptionPane.showMessageDialog(null, "개인 전체 예약 내역을 불러오지 못했습니다.");
        }else{
            data = (RoomReservation[][]) response.data;  // createDummyReservationGrid();
        }

        // 2. 시간 텍스트 더미 데이터 생성
        String[][] generateTimeSlots = generateTimeSlots();

        // 3. 캘린더 버튼 처리
        updateCalendarButtons(data, generateTimeSlots);


        view.getCalendar().setVisible(true);
    }

    // 캘린더 버튼 처리 분리 메서드
    private void updateCalendarButtons(RoomReservation[][] roomReservations, String[][] dummySubjects) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                updateSingleCalendarButton(day, period, roomReservations[day][period], dummySubjects[day][period]);
            }
        }
    }

    // 각 교시 별 버튼 처리
    private void updateSingleCalendarButton(int day, int period, RoomReservation roomReservation, String labelText) {
        String buttonName = "day" + day + "_" + period;

        for (Component comp : view.getCalendar().getComponents()) {
            if (comp instanceof TimeSlotButton btn && buttonName.equals(btn.getName())) {

                // 리스너 제거
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }

                btn.setText(labelText);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setForeground(Color.BLACK);

                if (roomReservation != null) {
                    btn.setEnabled(true);
                    btn.setRoomReservation(roomReservation);
                    btn.setText(roomReservation.getTitle());

                    // 선택된 예약 버튼이면 파란색, 아니면 초록색
                    TimeSlotButton selected = (TimeSlotButton) view.getSelectedCalendarButton();
                    if (selected != null &&
                            selected.getRoomReservation() != null &&
                            selected.getRoomReservation().getId().equals(roomReservation.getId())) {
                        btn.setBackground(new Color(0, 120, 215, 180)); // 반투명 파란색 (0~255, 255은 불투명)
                    } else {
                        btn.setBackground(Color.GREEN); // 일반 예약
                    }

                    setCalendarButtonClickListener(btn);

                } else {
                    // 예약이 없는 경우
                    btn.setRoomReservation(null);
                    btn.setEnabled(false);
                    btn.setBackground(Color.WHITE); // 비어 있는 칸
                }
            }
        }
    }

    // 버튼 클릭 이벤트 리스너 부여
    private void setCalendarButtonClickListener(TimeSlotButton btn) {
        btn.addActionListener(e -> {
            TimeSlotButton source = (TimeSlotButton) e.getSource();
            TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

            // 이전 선택된 버튼이 있다면 색상 복원
            if (prev != null && prev != source) {
                Color original = prev.getOriginalBackground();
                prev.setBackground(original != null ? original : Color.WHITE); // fallback 색상
                prev.repaint();
            }

            // 현재 버튼의 원래 색 저장 (최초 클릭 시만)
            if (source.getOriginalBackground() == null) {
                source.setOriginalBackground(source.getBackground());
            }

            // 현재 선택된 버튼 색상 지정
            source.setOpaque(true);
            source.setContentAreaFilled(true);
            source.setBackground(Color.RED);
            source.repaint();

            view.setSelectedCalendarButton(source);

            // 선택된 버튼의 예약 정보 필드에 반영
            RoomReservation r = source.getRoomReservation();
            view.getBuildingField().setText(r.getBuildingName());
            view.getFloorField().setText(r.getFloor());
            view.getReservationUniqueNumberField().setText(r.getId());
            view.getLectureRoomField().setText(r.getLectureRoom());
            view.getTitleField().setText(r.getTitle());
            view.getDescriptionField().setText(r.getDescription());
        });
    }

    // 시간 텍스트 생성 ("09:00~10:00" 형식)
    private String[][] generateTimeSlots() {
        String[][] result = new String[7][13];
        int startHour = 9;

        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                int hour = startHour + period;
                result[day][period] = String.format("%02d:00~%02d:00", hour, hour + 1);
            }
        }

        return result;
    }

    // =================================================================================================================

    // 예약을 삭제하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void deleteReservation(ActionEvent e) {
        String buildingName = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String uniqueNumber = view.getReservationUniqueNumberField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String userNumber = view.getUserNumber();

        // 삭제 여부 확인 다이얼로그
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "정말 이 예약을 삭제하시겠습니까?\n[" + title + "] " + buildingName + " " + floor + "층 " + lectureRoom,
                "예약 삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 버튼 잠금 또는 UI 비활성화 처리 필요 시 여기서 추가 가능

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                try {
                    return roomReservationClientController.deleteRoomReservation(userNumber, uniqueNumber);
                } catch (Exception ex) {
                    return new BasicResponse("500", "예외 발생: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();

                    if (!"200".equals(response.code)) {
                        JOptionPane.showMessageDialog(null, "예약 삭제에 실패했습니다: " + response.data, "삭제 실패", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    resetReservationTextField();
                    refreshUserReservationCalendar();
                    JOptionPane.showMessageDialog(null, "예약이 성공적으로 삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "삭제 처리 중 예외 발생: " + ex.getMessage(), "삭제 예외", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // 사용자의 모든 예약 내역 출력 하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void refreshMyReservationList() {
        JPanel myReservationList = view.getMyReservationList();
        String userNumber = view.getUserNumber();

        SwingWorker<java.util.List<RoomReservation>, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected java.util.List<RoomReservation> doInBackground() {
                BasicResponse response = roomReservationClientController.userRoomReservationList(userNumber);

                if (response == null || !"200".equals(response.code) || response.data == null) {
                    errorMessage = "개인 전체 예약 내역을 불러오지 못했습니다.";
                    return null;
                }

                try {
                    return (java.util.List<RoomReservation>) response.data;
                } catch (ClassCastException e) {
                    errorMessage = "예약 데이터를 처리할 수 없습니다.";
                    return null;
                }
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(null, errorMessage, "예약 불러오기 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    java.util.List<RoomReservation> reservations = get();
                    if (reservations == null) return;

                    myReservationList.removeAll();

                    int count = Math.min(reservations.size(), 5);
                    for (int i = 0; i < count; i++) {
                        RoomReservation res = reservations.get(i);
                        String date = res.getDate();
                        String room = res.getLectureRoom();
                        String startTime = res.getStartTime();

                        String periodText = timeToPeriod(startTime) + "교시";
                        String labelText = date + " / " + room + " / " + periodText;

                        PanelRound round = new PanelRound();
                        round.setLayout(new BorderLayout());
                        round.setRoundTopLeft(10);
                        round.setRoundTopRight(10);
                        round.setRoundBottomLeft(10);
                        round.setRoundBottomRight(10);
                        round.setBackground(new Color(20, 90, 170));

                        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
                        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        label.setForeground(Color.WHITE);

                        round.add(label, BorderLayout.CENTER);
                        myReservationList.add(round);
                    }

                    myReservationList.revalidate();
                    myReservationList.repaint();

                    view.getReservationCount().setText(String.valueOf(reservations.size()));
                    view.getReservationTotalCount().setText(String.valueOf(Math.max(0, 5 - reservations.size())));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "예약 목록 로딩 중 오류가 발생했습니다.\n" + e.getMessage(),
                            "예약 목록 오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // 사용자 프로필 정보를 갱신 하는 기능
    private void refreshUserProfile(){
        String number = view.getUserNumber();

        BasicResponse responseName = userClientController.findUserName(view.getUserNumber(), view.getUserPassword());
        String name  = (String) responseName.data;

        // 프로필 설정
        view.getProfileNumberField().setText("번호: " + view.getUserNumber());
        view.getProfileNameField().setText(name);
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 내 예약 리스트가 생성될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createMyReservationListInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshMyReservationList();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 캘린더가 생성 될 때 갱신 되는 기능 - 수정 금지
    private AncestorListener createUserReservationCalendarInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserReservationCalendar();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 프로필이 생성 될 때 갱신 되는 기능 - 수정 금지
    private AncestorListener createUserProfileInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserProfile();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 로그아웃 버튼 기능 - 수정 금지
    private void handleLogout(ActionEvent e) {
        Auth frame = (Auth) SwingUtilities.getWindowAncestor(view);
        BasicResponse result = userClientController.logout(view.getUserNumber(), view.getUserPassword());

        if (result.code.equals("200") && frame != null) {
            // 떠 있는 외부 프레임 닫기
            if (Home.getInstance() != null) {
                Home.getInstance().closeFloatingFrames();
            }

            // 로그인 화면으로 전환
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "login");

            // home 패널 제거
            for (Component comp : frame.getContentPane().getComponents()) {
                if ("home".equals(comp.getName())) {
                    frame.getContentPane().remove(comp);
                    break;
                }
            }

            // 싱글톤 인스턴스 해제
            Home.setInstanceNull();

            frame.revalidate();
            frame.repaint();
        } else {
            JOptionPane.showMessageDialog(view, result.data, "로그아웃 실패", JOptionPane.WARNING_MESSAGE);
        }
    }
    // 예약 메뉴 전환 - 수정 금지
    private void showReservationPanel(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        Reservation reservation = new Reservation(view.getUserNumber(), view.getUserPassword());
        new ReservationSwingController(reservation);
        view.replaceMainContent(view.getMenuPanel(), reservation);
    }
    // 기본 메뉴 전환 - 수정 금지
    private boolean hasAlreadyRefreshed = false;
    private void showMainPanel(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        checkManagementAuthority();
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());

        if (!hasAlreadyRefreshed) {
            refreshUserReservationCalendar();
            hasAlreadyRefreshed = true;
        }
    }
    // 관리자 전용 메뉴 전환 - 수정 금지
    private void showManagerMenu(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 사용자 관리 메뉴 전환 - 수정 금지
    private void showUserManagerManagement(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        UserManagement panel = new UserManagement();

        new UserManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 예악 관리 메뉴 전환 - 수정 금지
    private void showReservationManagement(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 일반 사용자 전용 매뉴 전환 - 수정 금지
    private void showCommonMenu(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());
    }
    // 관리자 패널 허용 여부 기눙 - 수정 금지
    private void checkManagementAuthority() {
        view.getUserNumber()
                .chars()
                .mapToObj(c -> Character.toUpperCase((char) c)) // 대소문자 무시
                .findFirst()
                .ifPresent(ch -> view.getManegementMenu().setVisible(ch == 'M'));
    }
    // 교시 연산 메서드 - 수정 금지
    private int timeToPeriod(String startTime) {
        try {
            int hour = Integer.parseInt(startTime.split(":")[0]);
            return hour - 8; // 예: 09:00 - 1교시
        } catch (Exception e) {
            return -1; // 오류 발생 시 -1 반환
        }
    }
    // 예약 후 필드값 비우는 메서드  - 수정 금지
    private void resetReservationTextField(){
        view.getBuildingField().setText("");
        view.getLectureRoomField().setText("");
        view.getTitleField().setText("");
        view.getFloorField().setText("");
        view.getReservationUniqueNumberField().setText("");
        view.getDescriptionField().setText("");
    }
}