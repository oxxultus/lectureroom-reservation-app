package deu.controller.event;

import deu.view.ReservationManagement;
import deu.view.custom.ButtonRound;
import deu.view.custom.RoundReservationInformationButton;

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

public class ReservationManagementSwingController {
    private final ReservationManagement view;

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

    // 건물 정보 가져오기 기능
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

        // TODO: 해당 부분도 언젠가는 동적으로 변경하는 것이 좋을 것 같지만 이번 프로젝트에서는 변경하지 않겠습니다.
        if (!"정보관".equals(selectedBuilding)) {
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            return;
        }

        // TODO: 해당 건물의 층 정보를 가져와서 해당 층의 강의실 리스트를 출력하고 해당 강의실 클릭 시 시간표에 해당 강의실의 빈 시각 정보가 나타나는 기능 입니다.
        for (int i = 1; i <= 9; i++) {
            final int currentFloor = i;
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(currentFloor), 45, 45);
            floorBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            floorBtn.setForeground(Color.BLACK);

            // TODO: 층 버튼이 가지는 기능을 추가하는 부분입니다.
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

                // TODO: 선택된 층이 9층일 경우 해당 층의 강의실 정보를 가져와 버튼으로 생성하고 해당 버튼의 기능을 추가하는 부분입니다.
                if ("9".equals(floorBtn.getText())) {
                    for (String room : getDynamicRoomNames()) {
                        ButtonRound roomBtn = view.createStyledButton(room, 100, 30);
                        roomBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
                        roomBtn.setForeground(Color.BLACK);

                        // TODO: 강의실 버튼이 가지는 기능을 추가하는 부분입니다.
                        roomBtn.addActionListener(roomEv -> {
                            view.getCalendar().setVisible(false);

                            if (view.getSelectedRoomButton() != null) {
                                view.getSelectedRoomButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                                view.getSelectedRoomButton().setForeground(Color.BLACK);
                            }

                            roomBtn.setBackground(view.ROOM_SELECTED_COLOR);
                            roomBtn.setForeground(Color.WHITE);
                            view.setSelectedRoomButton(roomBtn);

                            // TODO: 해당 강의실의 시간표정보를 가져와 갱신하는 부분입니다. (transfer_data => 강의실이름)
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

        // TODO: 각 캘린더의 시간대 별 버튼에 부여된 이름으로 버튼의 객체를 가져오기 위한 임시 변수(각 버튼은 "day행_열" 형식으로 이름이 지정되어 있습니다.)
        String[][] dummySubjects = new String[7][13];
        for (int j = 0; j < 7; j++) {
            for (int k = 0; k < 13; k++) {
                dummySubjects[j][k] = (j + 1) + "-" + (k + 1);
            }
        }

        // TODO: 각 시간대 별로 예약 내역에 따른 색상을 지정하는 부분입니다.(예: 예약대기중(RED), 예약완료(BLUE))
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof JButton && buttonName.equals(comp.getName())) {
                        JButton dayBtn = (JButton) comp;
                        dayBtn.setText(dummySubjects[day][period]);

                        // TODO: 해당 방식 처럼 특정 시간대의 색상을 변경하는 것이 가능합니다.
                        // TODO: 또한 예약된 시간대인 경우 버튼을 비 활성화 하는 것도 가능합니다.
                        if ("day2_0".equals(buttonName)) {
                            dayBtn.setBackground(Color.GREEN);
                            dayBtn.setEnabled(false);
                        } else {
                            dayBtn.setBackground(null);
                        }

                        // TODO: 해당 부분에서 기존의 버튼에 지정된 기능을 초기화 합니다.(오류 방지를 위해)
                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        // TODO: 해당 부분에서 모든 버튼의 공통적인 기능을 추가합니다.
                        dayBtn.addActionListener(ev -> {
                            JButton source = (JButton) ev.getSource();
                            String name = source.getName();
                            view.getReservationTimeField().setText(name);

                            if (view.getSelectedCalendarButton() != null) {
                                view.getSelectedCalendarButton().setBackground(null);
                            }

                            // TODO: 버튼 선택시 변경될 색상을 지정 합니다.
                            source.setBackground(new Color(255, 200, 0));
                            view.setSelectedCalendarButton(source);
                        });
                    }
                }
            }
        }

        view.getCalendar().setVisible(true);
    }

    // 수정하기 버튼 기능
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

    // 삭제하기 버튼 기능
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

    // 건물에 따른 강의실 정보 가져오는 기능
    private List<String> getDynamicRoomNames() {

        // TODO: 건물과 해당 층/건물에 따라 다르게 리턴 해야 합니다.
        return Arrays.asList("R101", "R102", "R103");
    }

    // 예약 목록을 갱신하는 기능
    private void reservationListPanelRefresh() {
       JPanel reservationListPanel = view.getReservationList();

       reservationListPanel.removeAll();
       reservationListPanel.setLayout(new GridLayout(0, 1, 0, 5)); // 세로 1열

       List<RoundReservationInformationButton> pendingReservations = getPendingReservations();

       for (RoundReservationInformationButton btn : pendingReservations) {
           btn.addActionListener(e -> processReservationChoice(btn));
           reservationListPanel.add(btn);
       }

       reservationListPanel.revalidate();
       reservationListPanel.repaint();
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

    // 서버 에서 예약 신청 내역 중 "대기" 인 내역을 버튼에 반영 해서 버튼 목록을 가져 오는 기능
    private List<RoundReservationInformationButton> getPendingReservations() {
        List<RoundReservationInformationButton> result = new ArrayList<>();

        // 예시 데이터: 실제로는 파일이나 서버에서 데이터를 가져와야 함
        String[][] dummyData = {
                {"정보관", "9", "A01", "S2023001", "대기", "2025-05-10T13:00", "2025-05-10T15:00"},
                {"정보관", "9", "A02", "S2023002", "완료", "2025-05-11T09:00", "2025-05-11T10:00"},
                {"정보관", "9", "A03", "S2023003", "대기", "2025-05-11T10:00", "2025-05-11T11:00"},
                {"정보관", "9", "A03", "S2023003", "대기", "2025-05-11T10:00", "2025-05-11T11:00"}
        };

        for (String[] data : dummyData) {
            if ("대기".equals(data[4])) {
                RoundReservationInformationButton btn = new RoundReservationInformationButton();
                btn.setBuildingName(data[0]);
                btn.setFloor(data[1]);
                btn.setLectureRoom(data[2]);
                btn.setNumber(data[3]);
                btn.setStatus(data[4]);
                btn.setStartTime(data[5]);
                btn.setEndTime(data[6]);

                btn.setText(data[0] + " / " + data[2] + " / " + data[5].substring(11, 16) + "~" + data[6].substring(11, 16));
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

    // 수정 안해도 되는 부분 ===========================================================================================

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

        //view.getLectureRoomField().setText("");
        // view.getReservationTimeField().setText("");
        // view.getBuildingField().setText("");
        // view.getFloorField().setText("");
    }
}