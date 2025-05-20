package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.view.Reservation;
import deu.view.custom.ButtonRound;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Arrays;

public class ReservationSwingController {
    private final Reservation view;
    private final LectureClientController lectureClientController = new LectureClientController();

    public ReservationSwingController(Reservation view) {
        this.view = view;

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addReservationButtionListener(this::lectureRoomReservationButton);
    }

    // 건물, 층, 강의실을 동적으로 추가하고 해당 강의실의 강의 시간표와 예약 시간표를 가져온다.==============================================

    // 1. 건물과 층을 선택 하는 기능 - (2 호출)
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        // UI 초기화
        clearSelectionUI();

        // 선택된 건물 설정
        String selectedBuilding = view.getSelectedBuilding();
        view.getBuildingField().setText(selectedBuilding);

        // 정보관이 아닌 경우: 종료
        if (!"정보관".equals(selectedBuilding)) {
            refreshReservationWriteDataField();
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();
            view.getFloorDisplayField().setText("");
            return;
        }

        // 정보관일 경우: 1~9층 층 버튼 추가
        addFloorButtons(selectedBuilding);
    }

    // 2. 층 버튼 생성해서 UI에 추가하는 메서드 - (3 호출)
    private void addFloorButtons(String buildingName) {
        // buildingName으로 기준으로 층 정보 가져온다.
        // 최대 층 = 컨트롤러 호출 -> 파일읽기 -> 값 전달받아 사용
        for (int i = 1; i <= 9; i++) {
            // final int currentFloor = i;
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(i), 45, 45);
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

                refreshReservationWriteDataField(); // 입력 초기화

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
                refreshReservationWriteDataField(); // 입력 패널 초기화

                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);

                updateCalendarWithDummyData();
            });

            view.getLectureRoomList().add(roomBtn);
        }
    }

    // 4. 캘린더에 예약 정보 갱신 하는 기능 - (5,6,7 호출)
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);

        // 1. 서버에서 주간 강의 데이터 요청
        Lecture[][] schedule = fetchWeeklyScheduleFromServer();
        if (schedule == null) return;

        // 2. 더미 텍스트 생성
        String[][] dummySubjects = generateDummySubjects();

        // 3. 캘린더 버튼 구성
        updateCalendarButtons(schedule, dummySubjects);

        view.getCalendar().setVisible(true);
    }

    // 5. 서버에서 주간 강의 데이터를 요청하여 반환
    private Lecture[][] fetchWeeklyScheduleFromServer() {
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

    // 6. 교시별 시간 문자열 생성
    private String[][] generateDummySubjects(){
        String[][] dummySubjects = new String[7][13];
        for (int j = 0; j < 7; j++) {
            for (int k = 0; k < 13; k++) {
                int startHour = 9 + k;
                int endHour = startHour + 1;
                dummySubjects[j][k] = String.format("%02d:00 ~ %02d:00", startHour, endHour);
            }
        }
        return dummySubjects;
    }

    // 7. 스케줄 및 더미 텍스트를 기반으로 시간표 버튼 UI 갱신
    private void updateCalendarButtons(Lecture[][] schedule, String[][] dummySubjects) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof TimeSlotButton dayBtn && buttonName.equals(comp.getName())) {

                        // 기본 텍스트 설정
                        dayBtn.setText(dummySubjects[day][period]);

                        // 기존 리스너 제거
                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        if (schedule[day][period] != null) { // 강의 데이터 삽입
                            // 해당 칸이 강의로 예약되어 있음
                            dayBtn.setBackground(new Color(100, 149, 237)); // 파란색
                            // dayBtn.setText(schedule[day][period].getTitle());
                            dayBtn.setLecture(schedule[day][period]);
                            dayBtn.setText(dayBtn.getLecture().getTitle());
                            dayBtn.setEnabled(false);
                        } else {
                            // 예약 가능
                            dayBtn.setBackground(null);
                            dayBtn.setEnabled(true);
                            dayBtn.addActionListener(ev -> {
                                JButton source = (JButton) ev.getSource();
                                String name = source.getName();
                                view.getReservationTimeField().setText(name);

                                // 기존 선택된 버튼 초기화
                                if (view.getSelectedCalendarButton() != null) {
                                    view.getSelectedCalendarButton().setBackground(null);
                                }

                                source.setBackground(new Color(255, 200, 0)); // 노란색
                                view.setSelectedCalendarButton(source);
                            });
                        }
                    }
                }
            }
        }
    }

    // =================================================================================================================

    // 예약하는 버튼 기능
    private void lectureRoomReservationButton(ActionEvent e){

        // 사용자 이름 가져오기
        String userName = view.getUserNumber();

        // 예약을 위한 정보 가져오기
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();

        /*
         * TODO: 예약 컨트롤러를 호출해서 해당 데이터로 예약 정보 저장하기
         * - 예약 성공, 실패 시 처리도 추가 해야 합니다.
         */


        //예약 후 작성 데이터 초기화
        refreshReservationWriteDataField();
    }

    // 수정 안해도 되는 부분 ================================================================================================

    // 필드 갱신하는 기능 - 수정 금지
    private void refreshReservationWriteDataField(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");
    }

    // 선택 UI 초기화 메서드 - 수정 금지
    private void clearSelectionUI() {
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }

    // 건물에 따른 강의실 정보 가져오는 기능( 추후 파일에서 정보를 읽어오는 형태로 변경 예정) - 수정 금지
    private List<String> getDynamicRoomNames(String building, String floor) {
        if(building.equals("정보관")){
            if(floor.equals("9")){
                return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
            }
        }

        return List.of();
    }
}