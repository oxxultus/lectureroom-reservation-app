package deu.controller.event;

import deu.view.UserManagement;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

public class UserManagementSwingController {
    private final UserManagement view;

    public UserManagementSwingController(UserManagement view) {
        this.view = view;

        // 이벤트 연결
        this.view.updateButtonListener(this::updateUserdata);
        this.view.deleteButtonListener(this::deleteUserdata);
        this.view.addTableInitListener(createTableInitListener());
        addTableSelectionListener();
    }

    // JTable에 사용자 목록을 불러오는 메서드
    private void refreshUserTable() {
        List<String[]> users = Arrays.asList(
                new String[]{"S2023001", "홍길동", "pass123", "컴퓨터공학과"},
                new String[]{"P2023002", "이순신", "navy456", "정보보호학과"},
                new String[]{"A2023003", "유관순", "freedom789", "간호학과"}
        );

        DefaultTableModel model = (DefaultTableModel) view.getUserTable().getModel();
        model.setRowCount(0);

        for (String[] user : users) {
            model.addRow(user);
        }
    }

    // 변경 사항 저장 버튼 기능
    private void updateUserdata(ActionEvent e) {
        String userName = view.getEditProfileNameField().getText();
        String userNumber = view.getEditProfileNumberField().getText();
        String userPassword = view.getEditProfilePasswordField().getText();
        String userMajor = view.getEditProfileMajorField().getText();

        // 테이블 갱신
        refreshUserTable();

        // TODO: 서버와 통신하여 수정 처리

        // 완료 후 필드 비우기
        clearProfileField();
    }

    // 삭제 버튼 기능
    private void deleteUserdata(ActionEvent e) {
        String userName = view.getEditProfileNameField().getText();
        String userNumber = view.getEditProfileNumberField().getText();
        String userPassword = view.getEditProfilePasswordField().getText();
        String userMajor = view.getEditProfileMajorField().getText();

        // 테이블 갱신
        refreshUserTable();

        // TODO: 서버와 통신하여 삭제 처리

        // 완료 후 필드 비우기
        clearProfileField();
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 테이블 항목 선택 시 텍스트 필드에 채워 넣는 이벤트 - 수정 금지
    private void addTableSelectionListener() {
        view.getUserTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = view.getUserTable().getSelectedRow();
                if (selectedRow != -1) {
                    view.getProfileNumberField().setText(view.getUserTable().getValueAt(selectedRow, 0).toString());
                    view.getProfileNameField().setText(view.getUserTable().getValueAt(selectedRow, 2).toString());
                    view.getProfileMajorField().setText(view.getUserTable().getValueAt(selectedRow, 3).toString());

                    view.getEditProfileNumberField().setText(view.getUserTable().getValueAt(selectedRow, 0).toString());
                    view.getEditProfileNameField().setText(view.getUserTable().getValueAt(selectedRow, 1).toString());
                    view.getEditProfilePasswordField().setText(view.getUserTable().getValueAt(selectedRow, 2).toString());
                    view.getEditProfileMajorField().setText(view.getUserTable().getValueAt(selectedRow, 3).toString());
                }
            }
        });
    }

    // 테이블이 생성 될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createTableInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserTable();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }

    // 불러온 사용자 데이터 필드 비우기 - 수정 금지
    private void clearProfileField(){
        view.getProfileNameField().setText("");
        view.getProfileNumberField().setText("");
        view.getProfileMajorField().setText("");

        view.getEditProfileNameField().setText("");
        view.getEditProfileNumberField().setText("");
        view.getEditProfilePasswordField().setText("");
        view.getEditProfileMajorField().setText("");
    }
}
