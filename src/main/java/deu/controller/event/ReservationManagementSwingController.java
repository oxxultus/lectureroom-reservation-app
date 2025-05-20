package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.view.ReservationManagement;
import deu.view.custom.ButtonRound;
import deu.view.custom.RoundReservationInformationButton;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReservationManagementSwingController {
    private final ReservationManagement view;
    private final LectureClientController lectureClientController = new LectureClientController();

    public ReservationManagementSwingController(ReservationManagement view) {
        this.view = view;

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addUpdateButtonListener(this::updateButton);
        view.addDeleteButtonListener(this::deleteButton);
        view.addReservationFrameButtonListener(this::reservationListFrameButton);
        view.addReservationFrameRefreshButtonListener(this::reservationListPanelRefreshButton);
        view.addReservationListInitListener(createReservationListPanelInitListener());
    }

    // 건물, 층, 강의실을 동적으로 추가하고 해당 강의실의 강의 시간표와 예약 시간표를 가져온다.==============================================

    // 1. 건물 정보와 층 정보를 가져오기 기능 - (2 호출)
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        // UI 초기화
        clearSelectionUI();

        // 선택된 건물 설정
        String selectedBuilding = (String) view.getSelectedBuilding();
        view.getBuildingField().setText(selectedBuilding);

        // 정보관이 아닌 경우: 종료
        if (!"정보관".equals(selectedBuilding)) {
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();
            view.getFloorDisplayField().setText("");

            clearReservationFieldData();
            return;
        }

        // 정보관일 경우: 1~9층 층 버튼 추가
        addFloorButtons(selectedBuilding);
    }

    // 2. 층 버튼 추가 메서드 - (3 호출)
    private void addFloorButtons(String buildingName) {
        // buildingName으로 기준으로 층 정보 가져온다.
        // 최대 층 = 컨트롤러 호출 -> 파일읽기 -> 값 전달받아 사용
        for (int i = 1; i <= 9; i++) {
            final int currentFloor = i;
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(currentFloor), 45, 45);
            floorBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            floorBtn.setForeground(Color.BLACK);

            floorBtn.addActionListener(ev -> {
                view.getCalendar().setVisible(false);

                // 기존 선택 해제
                if (view.getSelectedFloorButton() != null) {
                    view.getSelectedFloorButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedFloorButton().setForeground(Color.BLACK);
                }

                // 선택 설정
                floorBtn.setBackground(view.FLOOR_SELECTED_COLOR);
                floorBtn.setForeground(Color.WHITE);
                view.setSelectedFloorButton(floorBtn);

                view.getFloorDisplayField().setText(floorBtn.getText());
                view.getFloorField().setText(floorBtn.getText());

                view.getLectureRoomList().removeAll();
                view.setSelectedRoomButton(null);

                clearReservationFieldData(); // 입력 초기화

                addLectureRoomButtons(buildingName, floorBtn.getText()); // 층별 강의실 버튼 동적 생성

                view.getLectureRoomList().revalidate();
                view.getLectureRoomList().repaint();
            });

            view.getFloorButtonPanel().add(floorBtn);
        }

        view.getFloorButtonPanel().revalidate();
        view.getFloorButtonPanel().repaint();
    }

    // 3. 특정 층에 해당하는 강의실 버튼들을 동적으로 생성하여 UI에 추가하는 메서드 - (4 호출)
    private void addLectureRoomButtons(String buildingName, String floor) {
        // 9층만 강의실 버튼을 표시하도록 처리
        if (!"9".equals(floor)) return;

        for (String room : getDynamicRoomNames(buildingName, floor)) {
            ButtonRound roomBtn = view.createStyledButton(room, 100, 30);
            roomBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            roomBtn.setForeground(Color.BLACK);

            roomBtn.addActionListener(roomEv -> {
                view.getCalendar().setVisible(false);

                if (view.getSelectedRoomButton() != null) {
                    view.getSelectedRoomButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedRoomButton().setForeground(Color.BLACK);
                }

                roomBtn.setBackground(view.ROOM_SELECTED_COLOR);
                roomBtn.setForeground(Color.WHITE);
                clearReservationFieldData(); // 입력 패널 초기화

                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);

                updateCalendarWithDummyData();
            });

            view.getLectureRoomList().add(roomBtn);
        }
    }

    // 4. 캘린더에 예약, 강의 정보 갱신하는 기능 - (5,6,7 호출)
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);

        // 5. 강의 데이터 가져오기
        Lecture[][] schedule = fetchWeeklyLectureSchedule();
        if (schedule == null) return;

        // 6. 예약 데이터 가져오기
        RoomReservation[][] reservationSchedule = fetchWeeklyReservationSchedule();

        // 7. 교시별 라벨 생성 (ex: 09:00~10:00)
        String[][] dummySubjects = generateTimeSlotLabels();

        // 캘린더에 강의/예약 정보 적용
        applyScheduleToCalendar(schedule, reservationSchedule, dummySubjects);

        view.getCalendar().setVisible(true);
    }

    // 5. 서버로부터 주간 강의 스케줄 받아오기
    private Lecture[][] fetchWeeklyLectureSchedule() {
        BasicResponse res = lectureClientController.returnLectureOfWeek(
                view.getBuildingField().getText(),
                view.getFloorField().getText(),
                view.getLectureRoomField().getText()
        );

        if (res == null) {
            JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            return null;
        }

        if (!"200".equals(res.code) || !(res.data instanceof Lecture[][])) {
            JOptionPane.showMessageDialog(null, "강의 데이터를 불러오지 못했습니다: " + res.data);
            return null;
        }

        return (Lecture[][]) res.data;
    }

    // 6. 서버로부터 주간 예약 스케줄 받아오기 TODO: 예약 정보를 서버로 부터 받아와야 한다.
    private RoomReservation[][] fetchWeeklyReservationSchedule() {
        /* 아래와 유사하게 받아오면 된다. 현제는 임시데이터가 들어 가 있다.
            BasicResponse res = lectureClientController.returnLectureOfWeek(
                    view.getBuildingField().getText(),
                    view.getFloorField().getText(),
                    view.getLectureRoomField().getText()
            );
         */
        RoomReservation[][] grid = new RoomReservation[7][13];

        grid[0][11] = createDummyReservation2("정보관", "2", "A01", "S2023001", "스터디 모임", "자료구조 복습 스터디", "2025-05-19", "MONDAY", "09:00", "10:00");
        grid[0][12] = createDummyReservation2("정보관", "2", "A01", "S2023002", "회의", "프로젝트 회의", "2025-05-19", "MONDAY", "10:00", "11:00");
        grid[1][12] = createDummyReservation2("정보관", "3", "B01", "S2023003", "연습", "발표 연습", "2025-05-20", "TUESDAY", "12:00", "13:00");
        return grid;
    }

    // 7. 교시별 시간 문자열 생성
    private String[][] generateTimeSlotLabels() {
        String[][] labels = new String[7][13];
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                int startHour = 9 + period;
                int endHour = startHour + 1;
                labels[day][period] = String.format("%02d:00 ~ %02d:00", startHour, endHour);
            }
        }
        return labels;
    }

    // 8. 캘린더 버튼에 강의 및 예약 정보 반영
    private void applyScheduleToCalendar(Lecture[][] schedule, RoomReservation[][] reservationSchedule, String[][] labels) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof TimeSlotButton dayBtn && buttonName.equals(comp.getName())) {
                        dayBtn.setText(labels[day][period]);

                        // 기존 리스너 제거
                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        if (schedule[day][period] != null) {
                            // 강의가 있는 경우
                            dayBtn.setBackground(new Color(100, 149, 237)); // 파란색
                            dayBtn.setLecture(schedule[day][period]);
                            dayBtn.setText(schedule[day][period].getTitle());
                            dayBtn.setEnabled(false);
                        } else if (reservationSchedule != null && reservationSchedule[day][period] != null) {
                            // 예약이 있는 경우
                            dayBtn.setBackground(new Color(255, 165, 0)); // 주황색
                            dayBtn.setRoomReservation(reservationSchedule[day][period]); // 예약 객체 전달
                            dayBtn.setText(reservationSchedule[day][period].getTitle());
                            dayBtn.setEnabled(false);
                        } else {
                            // 비어있는 시간대
                            dayBtn.setBackground(null);
                            dayBtn.setEnabled(true);
                            dayBtn.addActionListener(ev -> {
                                JButton source = (JButton) ev.getSource();
                                view.getReservationTimeField().setText(source.getName());

                                if (view.getSelectedCalendarButton() != null) {
                                    view.getSelectedCalendarButton().setBackground(null);
                                }

                                source.setBackground(new Color(255, 200, 0));
                                view.setSelectedCalendarButton(source);
                            });
                        }
                    }
                }
            }
        }
    }

    // 수정하기 버튼 기능  TODO: 서버랑 연결해야 한다.
    private void updateButton(ActionEvent e) {

        // 필드 값을 가져온다.
        view.getReservationUserNumber().getText();
        view.getTitleField().getText();
        view.getBuildingField().getText();
        view.getFloorField().getText();
        view.getLectureRoomField().getText();
        view.getReservationTimeField().getText();
        view.getDescriptionField().getText();

        // TODO: 가져온 값을 바탕으로 컨트롤러를 호출하여 서버와 통신하여 수정 처리한다.
    }

    // 삭제하기 버튼 기능  TODO: 서버랑 연결해야 한다.
    private void deleteButton(ActionEvent e) {

        // 필드 값을 가져온다.
        view.getReservationUserNumber().getText();
        view.getTitleField().getText();
        view.getBuildingField().getText();
        view.getFloorField().getText();
        view.getLectureRoomField().getText();
        view.getReservationTimeField().getText();

        // TODO: 가져온 값을 바탕으로 컨트롤러를 호출하여 서버와 통신하여 삭제 처리한다.


        // 필드 초기화
        clearReservationFieldData();
    }

    // 예약 대기 목록 =====================================================================================================

    // 예약 대기 목록을 갱신하는 기능
    private void reservationListPanelRefresh() {
        JPanel reservationListPanel = view.getReservationList();

        reservationListPanel.removeAll();
        reservationListPanel.setLayout(new GridLayout(0, 1, 0, 5)); // 세로 1열

        // 전체 예약 리스트를 가져오는 과정 (예시)
        List<RoomReservation> allRoomReservations = getAllReservationsFromServerOrFile();

        List<RoundReservationInformationButton> pendingReservations = getPendingReservations(allRoomReservations);

        for (RoundReservationInformationButton btn : pendingReservations) {
            btn.addActionListener(e -> processReservationChoice(btn));
            reservationListPanel.add(btn);
        }

        reservationListPanel.revalidate();
        reservationListPanel.repaint();
    }

    // 임시로 전체 예약 리스트를 반환하는 메서드
    private List<RoomReservation> getAllReservationsFromServerOrFile() {

        // TODO: 해당 부분에서 서버로부터 강의 객체 리스트를 전달 받으면 된다.
        List<RoomReservation> dummyList = new ArrayList<>();

        dummyList.add(createDummyReservation("0", "정보관", "9", "A01", "S2023001", "대기", "2025-05-19", "MONDAY", "13:00", "15:00"));
        dummyList.add(createDummyReservation("1", "정보관", "9", "A02", "S2023002", "완료", "2025-05-19", "MONDAY", "09:00", "10:00"));
        dummyList.add(createDummyReservation("2", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("3", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("0", "정보관", "9", "A01", "S2023001", "대기", "2025-05-19", "MONDAY", "13:00", "15:00"));
        dummyList.add(createDummyReservation("1", "정보관", "9", "A02", "S2023002", "완료", "2025-05-19", "MONDAY", "09:00", "10:00"));
        dummyList.add(createDummyReservation("2", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("3", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("0", "정보관", "9", "A01", "S2023001", "대기", "2025-05-19", "MONDAY", "13:00", "15:00"));
        dummyList.add(createDummyReservation("1", "정보관", "9", "A02", "S2023002", "완료", "2025-05-19", "MONDAY", "09:00", "10:00"));
        dummyList.add(createDummyReservation("2", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("3", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("0", "정보관", "9", "A01", "S2023001", "대기", "2025-05-19", "MONDAY", "13:00", "15:00"));
        dummyList.add(createDummyReservation("1", "정보관", "9", "A02", "S2023002", "완료", "2025-05-19", "MONDAY", "09:00", "10:00"));
        dummyList.add(createDummyReservation("2", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("3", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("0", "정보관", "9", "A01", "S2023001", "대기", "2025-05-19", "MONDAY", "13:00", "15:00"));
        dummyList.add(createDummyReservation("1", "정보관", "9", "A02", "S2023002", "완료", "2025-05-19", "MONDAY", "09:00", "10:00"));
        dummyList.add(createDummyReservation("2", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));
        dummyList.add(createDummyReservation("3", "정보관", "9", "A03", "S2023003", "대기", "2025-05-19", "MONDAY", "10:00", "11:00"));

        return dummyList;
    }

    // 예약 대기 목록을 서버로 부터 받아오는 기능  TODO: 서버로 부터 예약 대기 목록을 받아와야 한다.
    private List<RoundReservationInformationButton> getPendingReservations(List<RoomReservation> allRoomReservations) {
        List<RoundReservationInformationButton> result = new ArrayList<>();

        for (RoomReservation roomReservation : allRoomReservations) {
            if ("대기".equals(roomReservation.getStatus())) {
                RoundReservationInformationButton btn = new RoundReservationInformationButton();
                btn.setRoomReservation(roomReservation);
                btn.setText(roomReservation.getBuildingName() + " / " +
                        roomReservation.getLectureRoom() + " / " +
                        roomReservation.getStartTime() + "~" + roomReservation.getEndTime());
                btn.setPreferredSize(new Dimension(112, 40));
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
                btn.setRoundTopLeft(10);
                btn.setRoundTopRight(10);
                btn.setRoundBottomLeft(10);
                btn.setRoundBottomRight(10);

                result.add(btn);
            }
        }

        return result;
    }

    // 예약 수락, 거절 팝업 인터페이스 기능
    private void processReservationChoice(RoundReservationInformationButton btn) {
        int choice = JOptionPane.showOptionDialog(
                view,
                "이 예약을 어떻게 처리하시겠습니까?",
                "예약 처리",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"예약 수락", "예약 거절"},
                "예약 수락"
        );

        if (choice == JOptionPane.YES_OPTION) {
            // int code = new ReservationController().acceptReservation(btn);
            int code = 200;
            if (code == 200) {
                JOptionPane.showMessageDialog(view, "예약이 수락되었습니다.");
            } else if (code == 409) {
                JOptionPane.showMessageDialog(view, "동일 시간대에 이미 예약이 존재합니다.", "예약 실패", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            // int code = new ReservationController().rejectReservation(btn.getNumber());
            int code = 200;
            if (code == 200) {
                JOptionPane.showMessageDialog(view, "예약이 거절되었습니다.");
            } else {
                JOptionPane.showMessageDialog(view, "처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
        reservationListPanelRefresh(); // 리스트 갱신
    }

    // 수정 안해도 되는 부분 ================================================================================================

    // 예약 목록의 새로고침 버튼 기능 - 수정 금지
    private void reservationListPanelRefreshButton(ActionEvent e) {
        System.out.println("reservationListPanelRefreshButton");
        reservationListPanelRefresh();
    }
    // 예약 목록이 생성될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createReservationListPanelInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                reservationListPanelRefresh();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 예약 목록 프레임 띄우는 기능 - 수정 금지
    private void reservationListFrameButton(ActionEvent e){
        JFrame authFrame = (JFrame) SwingUtilities.getWindowAncestor(view);
        JFrame reservationListFrame = view.getReservationListFrame(); // 또는 직접 필드 사용

        if (authFrame != null && reservationListFrame != null) {
            int authX = authFrame.getX();
            int authY = authFrame.getY();
            int authWidth = authFrame.getWidth();
            int authHeight = authFrame.getHeight();

            // 프레임 우측에 위치 (10px 여백)
            int x = authX + authWidth + 10;
            int y = authY + (authHeight - reservationListFrame.getHeight()) / 2;

            reservationListFrame.setLocation(x, y);
            reservationListFrame.setVisible(true);
            reservationListFrame.toFront(); // 항상 위로
        }
    }
    // 입력 필드 초기화 - 수정 금지
    private void clearReservationFieldData(){
        // 필드 값을 비운다.
        view.getReservationUserNumber().setText("");
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");

        //view.getLectureRoomField().setText("");
        // view.getReservationTimeField().setText("");
        // view.getBuildingField().setText("");
        // view.getFloorField().setText("");
    }
    // 선택 UI 초기화 메서드 - 수정 금지
    private void clearSelectionUI() {
        // 초기화
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }
    // 건물에 따른 강의실 정보 가져오는 기능 - 수정 금지
    private List<String> getDynamicRoomNames(String building, String floor) {
        if(building.equals("정보관")){
            if(floor.equals("9")){
                return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
            }
        }

        return List.of();
    }

    // 반복 제거를 위한 Reservation 생성 메서드 - 수정 금지 [ ! ] 테스트용
    private RoomReservation createDummyReservation(String id, String buildingName, String floor,
                                                   String lectureRoom, String number, String status,
                                                   String date, String dayOfWeek, String startTime, String endTime) {
        RoomReservation roomReservation = new RoomReservation();
        roomReservation.setId(id);
        roomReservation.setBuildingName(buildingName);
        roomReservation.setFloor(floor);
        roomReservation.setLectureRoom(lectureRoom);
        roomReservation.setNumber(number);
        roomReservation.setStatus(status);
        roomReservation.setDate(date);
        roomReservation.setDayOfTheWeek(dayOfWeek);
        roomReservation.setStartTime(startTime);
        roomReservation.setEndTime(endTime);
        return roomReservation;
    }
    private RoomReservation createDummyReservation2(String building, String floor, String room,
                                                   String userId, String title, String description,
                                                   String date, String dayOfWeek, String start, String end) {
        RoomReservation r = new RoomReservation();
        r.setId(UUID.randomUUID().toString());
        r.setBuildingName(building);
        r.setFloor(floor);
        r.setLectureRoom(room);
        r.setNumber(userId);
        r.setStatus("완료");
        r.setTitle(title);
        r.setDescription(description);
        r.setDate(date);
        r.setDayOfTheWeek(dayOfWeek);
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }
}