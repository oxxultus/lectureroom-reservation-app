package deu.controller.event;

import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.view.*;
import deu.view.custom.PanelRound;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeSwingController {

    private final Home view;

    public HomeSwingController(Home view) {
        this.view = view;

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

    // 사용자 예약 정보를 캘린더에 갱신 하는 기능
    private void refreshUserReservationCalendar() {
        // 현재 시각을 기준으로 날자를 계산하는 부분입니다. (편의를 위해 작성 해 두었습니다.)
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd(E)");

        // TODO: 해당 시간대에 예약이 되어 있는 경우 true로 표시 합니다. (인터페이스의 버튼 기능을 disable 하기 위함 입니다.)
        // 예시 예약 데이터: [day][period]
        boolean[][] isReserved = new boolean[7][13];
        isReserved[2][0] = true; // 3일 후 1교시
        isReserved[4][5] = true; // 5일 후 6교시

        // TODO: 캘린더에 지정된 버튼의 이름은 day행_열 을 기준 으로 되어있습니다. (즉 개인의 예약 정보이기. 때문에 개인의 예약 정보만 가져와야 합니다.)
        for (int day = 0; day < 7; day++) {
            java.time.LocalDate targetDate = today.plusDays(day);
            String dayText = targetDate.format(dayFormatter); // 예: "05-08(수)"

            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof JButton && buttonName.equals(comp.getName())) {
                        JButton btn = (JButton) comp;

                        // 리스너 초기화
                        for (ActionListener al : btn.getActionListeners()) {
                            btn.removeActionListener(al);
                        }

                        // TODO: 예약된 경우의 버튼 표시 입니다. (다른 인수(int 1,2,3 혹은 string 대기, 완료)등을 사용 하여 예약 대기, 완료 상태를 표시할 수 있도록 변경하는 것이 좋을 것 같습니다.)
                        if (isReserved[day][period]) {
                            //btn.setText(dayText + " / " + (period + 1) + "교시");
                            btn.setText((period + 1) + "교시");
                            btn.setBackground(Color.GREEN);
                            btn.setEnabled(false);
                        } else { // TODO: 예약이 안 된 경우의 버튼 표시 입니다.
                            btn.setText((period + 1) + "교시");
                            btn.setBackground(null);
                            btn.setEnabled(true);

                            // TODO: 각 시간별 버튼에 대한 기능을 넣는 부분입니다.
                            btn.addActionListener(ev -> {
                                JButton source = (JButton) ev.getSource();
                                if (view.getSelectedCalendarButton() != null) {
                                    view.getSelectedCalendarButton().setBackground(null);
                                }
                                source.setBackground(new Color(255, 200, 0));
                                view.setSelectedCalendarButton(source);

                                // 버튼 클릭 시 정보 출력
                                // 버튼 이름에서 day와 period 정보 추출 (예: day2_0 → 2, 0)
                                String buttonName2 = source.getName(); // 반드시 name이 지정돼 있어야 함 (지정되어 있습니다.)
                                String[] tokens = buttonName.replace("day", "").split("_");
                                int dayIndex = Integer.parseInt(tokens[0]);
                                int periodIndex = Integer.parseInt(tokens[1]);

                                // TODO: 선택된 데이터를 바탕으로 서버를 통해 데이터를 조회하여 가져와야 합니다.
                                // 예시 데이터 (실제에선 파일 또는 서버에서 조회)
                                String userNumber = "S2023001";
                                String lectureRoom = "정보관 A01";
                                String title = (dayIndex + 1) + "일차 / " + (periodIndex + 1) + "교시 예약";
                                String description = "세부 설명 없음";

                                // 텍스트 필드에 출력
                                view.getBuildingField().setText("정보관");
                                view.getFloorField().setText("9층");
                                view.getReservationUserNumberField().setText("에약자 이름");
                                view.getLectureRoomField().setText(lectureRoom);
                                view.getTitleField().setText(title);
                                view.getDescriptionField().setText(description);
                            });
                        }
                    }
                }
            }
        }

        view.getCalendar().setVisible(true);
    }

    // 예약을 삭제하는 기능
    private void deleteReservation(ActionEvent e) {
        // 텍스트 필드에서 값 가져오는 부분
        String buildingName = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String number = view.getReservationUserNumberField().getText();
        String lectureroom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();

        /**
         * TODO: 해당 예약을 삭제하는 로직
         * - 삭제가 안될 시 처리가 필요합니다.
         */

        // 예약 캘린더 갱신
        refreshUserReservationCalendar(); //myReservationList
    }

    // 사용자의 모든 예약 내역 출력 하는 기능
    private void refreshMyReservationList() {
        JPanel myReservationList = view.getMyReservationList();

        // 기존 패널 내용 초기화
        myReservationList.removeAll();

        // TODO: 컨트롤러를 통해 서버와 통신하여 해당 사용자의 예약 내역을 받아와야 합니다.
        // 예시 예약 정보 5개 이내로 준비 (실제에선 파일에서 읽거나 서비스로부터 받아야 함)
        String[][] reservations = {
                {"2025-05-08", "정보관 A01", "3교시"},
                {"2025-05-09", "정보관 A03", "5교시"},
                {"2025-05-10", "정보관 A02", "1교시"},
                {"2025-05-11", "정보관 A06", "7교시"},
                {"2025-05-12", "정보관 A04", "2교시"}
        };

        // TODO: 해당 부분에서 사용자의 예약 내역을 바탕으로 화면에 표시하는 부분입니다.
        // 최대 5개까지만 표시
        int count = Math.min(reservations.length, 5);
        for (int i = 0; i < count; i++) {
            String[] data = reservations[i];
            String labelText = data[0] + " / " + data[1] + " / " + data[2];

            PanelRound round = new PanelRound();
            round.setLayout(new BorderLayout());
            round.setRoundTopLeft(10);
            round.setRoundTopRight(10);
            round.setRoundBottomLeft(10);
            round.setRoundBottomRight(10);
            round.setBackground(new Color(20, 90, 170)); // ✅ 배경색 설정

            JLabel label = new JLabel(labelText, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(Color.WHITE); // ✅ 글자색 설정

            round.add(label, BorderLayout.CENTER);
            myReservationList.add(round);
        }

        myReservationList.revalidate();
        myReservationList.repaint();

        // 현재 예약 개수 정보 가져와서 출력하기
        view.getReservationCount().setText(String.valueOf(reservations.length));
        view.getReservationTotalCount().setText(String.valueOf(5 - reservations.length));
    }

    // 사용자 프로필 정보를 갱신 하는 기능
    private void refreshUserProfile(){
        String number = view.getUserNumber();

        // TODO: 이름은 컨트롤러 호출해서 서버에서 해당 번호에 맞는 이름을 반환받는 메서드 필요합니다.
        String name = "임시데이터";

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
        BasicResponse result = new UserClientController().logout(view.getUserNumber(), view.getUserPassword());

        if (result.code.equals("200") && frame != null) {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "login");

            // home 제거
            for (Component comp : frame.getContentPane().getComponents()) {
                if ("home".equals(comp.getName())) {
                    frame.getContentPane().remove(comp);
                    break;
                }
            }
            frame.revalidate();
            frame.repaint();
        } else {
            JOptionPane.showMessageDialog(view, result.message, "로그아웃 실패", JOptionPane.WARNING_MESSAGE);
        }
    }
    // 예약 메뉴 전환 - 수정 금지
    private void showReservationPanel(ActionEvent e) {
        Reservation reservation = new Reservation(view.getUserNumber(), view.getUserPassword());
        new ReservationSwingController(reservation);
        view.replaceMainContent(view.getMenuPanel(), reservation);
    }
    // 기본 메뉴 전환 - 수정 금지
    private void showMainPanel(ActionEvent e) {
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());
        refreshUserReservationCalendar();
    }
    // 관리자 전용 메뉴 전환 - 수정 금지
    private void showManagerMenu(ActionEvent e) {
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 사용자 관리 메뉴 전환 - 수정 금지
    private void showUserManagerManagement(ActionEvent e) {
        UserManagement panel = new UserManagement();

        new UserManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 예악 관리 메뉴 전환 - 수정 금지
    private void showReservationManagement(ActionEvent e) {
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 일반 사용자 전용 매뉴 전환 - 수정 금지
    private void showCommonMenu(ActionEvent e) {
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());
    }

}