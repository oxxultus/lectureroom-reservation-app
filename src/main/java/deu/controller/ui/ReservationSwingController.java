package deu.controller.ui;

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

    public ReservationSwingController(Reservation view) {
        this.view = view;

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addReservationButtionListener(this::lectureRoomReservationButton);
    }

    // 건물을 선택 하는 기능
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
                            view.setSelectedRoomButton(roomBtn);

                            updateCalendarWithDummyData(room);
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

    // 캘린더에 클릭 시 각 강의실의 정보를 갱신 하는 기능
    private void updateCalendarWithDummyData(String room) {
        view.getCalendar().setVisible(false);
        view.getLectureRoomField().setText(room);

        String[][] dummySubjects = new String[7][13];
        for (int j = 0; j < 7; j++) {
            for (int k = 0; k < 13; k++) {
                dummySubjects[j][k] = (j + 1) + "-" + (k + 1);
            }
        }

        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof JButton && buttonName.equals(comp.getName())) {
                        JButton dayBtn = (JButton) comp;
                        dayBtn.setText(dummySubjects[day][period]);

                        if ("day2_0".equals(buttonName)) {
                            dayBtn.setBackground(Color.GREEN);
                            dayBtn.setEnabled(false);
                        } else {
                            dayBtn.setBackground(null);
                        }

                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        dayBtn.addActionListener(ev -> {
                            JButton source = (JButton) ev.getSource();
                            String name = source.getName();
                            view.getReservationTimeField().setText(name);

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

        // TODO: 예약 컨트롤러를 호출해서 해당 데이터로 예약 정보 저장하기


        //예약 후 작성 데이터 초기화
        refreshReservationWriteDataField();
    }

    // 건물에 따른 강의실 정보 가져오는 기능
    private List<String> getDynamicRoomNames() {
        // 예: 해당 층/건물에 따라 다르게 리턴
        return Arrays.asList("R101", "R102", "R103");
    }

    // 필드 갱신하는 기능 - 수정 금지
    private void refreshReservationWriteDataField(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
    }
}