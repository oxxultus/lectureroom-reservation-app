package deu.model.dto.request.command;

import java.io.Serializable;

public class ReservationCommandRequest implements Serializable {
    public String command;
    public Object payload;

    public ReservationCommandRequest(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }
}