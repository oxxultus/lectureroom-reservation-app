package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
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

    // 건물 정보와 층 정보를 가져오기 기능
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
    // 층 버튼 추가 메서드
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

    // 특정 층에 해당하는 강의실 버튼들을 동적으로 생성하여 UI에 추가하는 메서드
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

    // 캘린더에 예약 정보 갱신 하는 기능 - 강의 정보 불러오는 기능 추가 완료 - TODO: 메서드 분리 작업 필요
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);

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
                int startHour = 9 + k;  // 첫 교시는 9시부터 시작
                int endHour = startHour + 1;
                String start = String.format("%02d:00", startHour);
                String end = String.format("%02d:00", endHour);
                dummySubjects[j][k] = start + " ~ " + end;
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

    // 건물에 따른 강의실 정보 가져오는 기능
    private List<String> getDynamicRoomNames(String building, String floor) {
        if(building.equals("정보관")){
            if(floor.equals("9")){
                return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
            }
        }

        return List.of();
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
                {"0","정보관", "9", "A01", "S2023001", "대기", "2025-05-19","MONDAY", "13:00", "15:00"},
                {"1","정보관", "9", "A02", "S2023002", "완료", "2025-05-19","MONDAY", "09:00", "10:00"},
                {"2","정보관", "9", "A03", "S2023003", "대기", "2025-05-19","MONDAY", "10:00", "11:00"},
                {"3","정보관", "9", "A03", "S2023003", "대기", "2025-05-19","MONDAY", "10:00", "11:00"}
        };

        for (String[] data : dummyData) {
            if ("대기".equals(data[5])) {
                RoundReservationInformationButton btn = new RoundReservationInformationButton();
                btn.setId(data[0]);
                btn.setBuildingName(data[1]);
                btn.setFloor(data[2]);
                btn.setLectureRoom(data[3]);
                btn.setNumber(data[4]);
                btn.setStatus(data[5]);
                btn.setDate(data[6]);
                btn.setDayOfTheWeek(data[7]);
                btn.setStartTime(data[8]);
                btn.setEndTime(data[9]);

                btn.setText(data[1] + " / " + data[3] + " / " + data[8] + "~" + data[9]);
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
}