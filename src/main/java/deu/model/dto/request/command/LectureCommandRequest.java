package deu.model.dto.request.command;

import java.io.Serializable;

public class UserManagementCommandRequest implements Serializable {
    public String command;
    public Object payload;

    public UserManagementCommandRequest(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }
}