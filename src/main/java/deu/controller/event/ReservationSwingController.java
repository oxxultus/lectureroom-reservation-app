package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.view.Reservation;
import deu.view.custom.ButtonRound;

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

    // 건물과 층을 선택 하는 기능
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        // 초기화
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();

        String selectedBuilding = (String) view.getSelectedBuilding();
        view.getBuildingField().setText(selectedBuilding);

        if (!"정보관".equals(selectedBuilding)) {
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();

            view.getFloorDisplayField().setText("");

            refreshReservationWriteDataField();
            return;
        }

        for (int i = 1; i <= 9; i++) {
            final int currentFloor = i;
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(currentFloor), 45, 45);
            floorBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            floorBtn.setForeground(Color.BLACK);

            floorBtn.addActionListener(ev -> {
                view.getCalendar().setVisible(false);

                if (view.getSelectedFloorButton() != null) {
                    view.getSelectedFloorButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedFloorButton().setForeground(Color.BLACK);
                }

                floorBtn.setBackground(view.FLOOR_SELECTED_COLOR);
                floorBtn.setForeground(Color.WHITE);
                view.setSelectedFloorButton(floorBtn);

                view.getFloorDisplayField().setText(floorBtn.getText());
                view.getFloorField().setText(floorBtn.getText());

                view.getLectureRoomList().removeAll();
                view.setSelectedRoomButton(null);

                refreshReservationWriteDataField(); // 입력 패널 초기화

                if ("9".equals(floorBtn.getText())) {
                    for (String room : getDynamicRoomNames()) {
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

                view.getLectureRoomList().revalidate();
                view.getLectureRoomList().repaint();
            });

            view.getFloorButtonPanel().add(floorBtn);
        }

        view.getFloorButtonPanel().revalidate();
        view.getFloorButtonPanel().repaint();
    }

    // 캘린더에 예약 정보 갱신 하는 기능 - 강의 정보 불러오는 기능 추가 완료
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);
        System.out.println(view.getLectureRoomField().getText());

        // 1. 서버에서 주간 강의 일정 요청
        BasicResponse res = lectureClientController.returnLectureOfWeek(
                view.getBuildingField().getText(),
                view.getFloorField().getText(),
                view.getLectureRoomField().getText()
        );

        // 2. 실패 시 알림 후 종료
        if (res == null) {
            JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            return;
        }

        if (!"200".equals(res.code) || !(res.data instanceof Lecture[][])) {
            JOptionPane.showMessageDialog(null, "강의 데이터를 불러오지 못했습니다: " + res.data);
            return;
        }

        Lecture[][] schedule = (Lecture[][]) res.data;

        // 3. 더미 텍스트 초기화
        String[][] dummySubjects = new String[7][13];
        for (int j = 0; j < 7; j++) {
            for (int k = 0; k < 13; k++) {
                dummySubjects[j][k] = (j + 1) + "-" + (k + 1);
            }
        }

        // 4. 버튼에 강의 스케줄 반영
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof JButton && buttonName.equals(comp.getName())) {
                        JButton dayBtn = (JButton) comp;
                        dayBtn.setText(dummySubjects[day][period]);

                        // 기존 리스너 제거
                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        if (schedule[day][period] != null) {
                            // 예약된 강의가 있는 경우
                            dayBtn.setBackground(new Color(100, 149, 237)); // 파란색
                            dayBtn.setText(schedule[day][period].getTitle());
                            dayBtn.setEnabled(false);
                        } else {
                            // 예약 가능한 경우
                            dayBtn.setBackground(null);
                            dayBtn.setEnabled(true);
                            dayBtn.addActionListener(ev -> {
                                JButton source = (JButton) ev.getSource();
                                String name = source.getName();
                                view.getReservationTimeField().setText(name);

                                // 이전 선택 버튼 색상 원복
                                if (view.getSelectedCalendarButton() != null) {
                                    view.getSelectedCalendarButton().setBackground(null);
                                }

                                // 현재 선택 버튼 표시
                                source.setBackground(new Color(255, 200, 0)); // 노란색
                                view.setSelectedCalendarButton(source);
                            });
                        }
                    }
                }
            }
        }

        view.getCalendar().setVisible(true);
    }

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

        /**
         * TODO: 예약 컨트롤러를 호출해서 해당 데이터로 예약 정보 저장하기
         * - 예약 성공, 실패 시 처리도 추가 해야 합니다.
         */


        //예약 후 작성 데이터 초기화
        refreshReservationWriteDataField();
    }

    // 건물에 따른 강의실 정보 가져오는 기능
    private List<String> getDynamicRoomNames() {
        // TODO: 건물과 해당 층/건물에 따라 다르게 리턴 해야 합니다.
        return Arrays.asList("R101", "R102", "912");
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 필드 갱신하는 기능 - 수정 금지
    private void refreshReservationWriteDataField(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");
    }
}