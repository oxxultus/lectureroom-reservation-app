package deu.controller.event;

import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.RoomReservation;
import deu.view.*;
import deu.view.custom.PanelRound;
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
    private final UserClientController userClientController = new UserClientController();

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

    // 개인 별 주간 예약 시간표를 확인하는 기능 =================================================================================

    // 로그인 사용자 예약 정보를 캘린더에 갱신 하는 기능  TODO: 서버랑 연결하여 예약 객체를 바로 받아와야 한다.
    private void refreshUserReservationCalendar() {
        view.getCalendar().setVisible(false);

        // 1. 더미 예약 정보 객체 생성 (7일 x 13교시) TODO: 서버에서 7x13 배열로 된 예약 객체를 바로 받아오면 된다. 단 당일 + 7 일의 형식으로 전달해 줘야 한다.
        RoomReservation[][] weeklyRoomReservations = createDummyReservationGrid();

        // 2. 시간 텍스트 더미 데이터 생성
        String[][] dummySubjects = generateTimeSlots(); // ex: "09:00~10:00"

        // 3. 캘린더 버튼 처리
        updateCalendarButtons(weeklyRoomReservations, dummySubjects);

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

                if (roomReservation != null) {
                    // btn.setBackground(Color.GREEN);
                    btn.setEnabled(true);
                    btn.setRoomReservation(roomReservation);
                    btn.setText(btn.getRoomReservation().getTitle());
                    setCalendarButtonClickListener(btn);
                } else {
                    btn.setBackground(null);
                    btn.setEnabled(false);
                    btn.setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    // 버튼 클릭 이벤트 리스너 부여
    private void setCalendarButtonClickListener(TimeSlotButton btn) {
        btn.addActionListener(e -> {
            TimeSlotButton source = (TimeSlotButton) e.getSource();
            if (view.getSelectedCalendarButton() != null) {
                view.getSelectedCalendarButton().setBackground(null);
            }
            source.setBackground(Color.GREEN);
            view.setSelectedCalendarButton(source);

            RoomReservation roomReservation = btn.getRoomReservation();

            view.getBuildingField().setText(roomReservation.getBuildingName());
            view.getFloorField().setText(roomReservation.getFloor());
            view.getReservationUserNumberField().setText(roomReservation.getNumber());
            view.getLectureRoomField().setText(roomReservation.getLectureRoom());
            view.getTitleField().setText(roomReservation.getTitle());
            view.getDescriptionField().setText(roomReservation.getDescription());
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

    // 예약을 삭제하는 기능  TODO: 서버랑 연결해야 한다.
    private void deleteReservation(ActionEvent e) {
        // 텍스트 필드에서 값 가져오는 부분
        String buildingName = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String number = view.getReservationUserNumberField().getText();
        String lectureroom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();

        /*
         * TODO: 해당 예약을 삭제하는 로직
         * - 삭제가 안될 시 처리가 필요합니다.
         */

        // 예약 캘린더 갱신
        refreshUserReservationCalendar(); //myReservationList
    }

    // 사용자의 모든 예약 내역 출력 하는 기능  TODO: 서버랑 연결하여 예약 객체를 바로 받아와야 한다.
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
            JOptionPane.showMessageDialog(view, result.data, "로그아웃 실패", JOptionPane.WARNING_MESSAGE);
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
        checkManagementAuthority();
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
    // 관리자 패널 허용 여부 기눙 - 수정 금지
    private void checkManagementAuthority() {
        view.getUserNumber()
                .chars()
                .mapToObj(c -> Character.toUpperCase((char) c)) // 대소문자 무시
                .findFirst()
                .ifPresent(ch -> view.getManegementMenu().setVisible(ch == 'M'));
    }

    // 추후 삭제 =========================================================================================================

    // 테스트용 예약 객체를 7 x 13 배열 형태로 반환
    private RoomReservation[][] createDummyReservationGrid() {
        RoomReservation[][] grid = new RoomReservation[7][13];

        grid[0][0] = createDummyReservation("정보관", "2", "A01", "S2023001", "스터디 모임", "자료구조 복습 스터디", "2025-05-19", "MONDAY", "09:00", "10:00");
        grid[0][1] = createDummyReservation("정보관", "2", "A01", "S2023002", "회의", "프로젝트 회의", "2025-05-19", "MONDAY", "10:00", "11:00");
        grid[1][3] = createDummyReservation("정보관", "3", "B01", "S2023003", "연습", "발표 연습", "2025-05-20", "TUESDAY", "12:00", "13:00");
        grid[2][0] = createDummyReservation("정보관", "3", "B02", "S2023004", "스터디", "알고리즘 문제풀이", "2025-05-21", "WEDNESDAY", "09:00", "10:00");
        grid[2][5] = createDummyReservation("정보관", "4", "C01", "S2023005", "스터디", "운영체제 스터디", "2025-05-21", "WEDNESDAY", "14:00", "15:00");
        grid[3][2] = createDummyReservation("정보관", "5", "C02", "S2023006", "면접 준비", "모의 면접 및 피드백", "2025-05-22", "THURSDAY", "11:00", "12:00");
        grid[4][5] = createDummyReservation("정보관", "4", "D01", "S2023007", "스터디", "DB 스터디", "2025-05-23", "FRIDAY", "14:00", "15:00");
        grid[5][8] = createDummyReservation("정보관", "3", "D02", "S2023008", "프로젝트", "기말 프로젝트 작업", "2025-05-24", "SATURDAY", "17:00", "18:00");
        grid[6][10] = createDummyReservation("정보관", "3", "E01", "S2023009", "학회 준비", "학회 발표 준비", "2025-05-25", "SUNDAY", "19:00", "20:00");
        grid[6][11] = createDummyReservation("정보관", "3", "E01", "S2023010", "스터디", "캡스톤디자인 스터디", "2025-05-25", "SUNDAY", "20:00", "21:00");

        return grid;
    }
    // 테스트용 예약 생성자
    private RoomReservation createDummyReservation(String building, String floor, String room,
                                                   String userId, String title, String description,
                                                   String date, String dayOfWeek, String start, String end) {
        RoomReservation r = new RoomReservation();
        r.setId(UUID.randomUUID().toString());
        r.setBuildingName(building);
        r.setFloor(floor);
        r.setLectureRoom(room);
        r.setNumber(userId);
        r.setStatus("대기");
        r.setTitle(title);
        r.setDescription(description);
        r.setDate(date);
        r.setDayOfTheWeek(dayOfWeek);
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }

}