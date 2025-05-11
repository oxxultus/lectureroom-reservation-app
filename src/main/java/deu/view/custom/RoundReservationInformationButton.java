package deu.view.custom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundReservationInformationButton extends ButtonRound{

    private String buildingName;
    private String floor;
    private String lectureRoom;
    private String number;
    private String status;

    private String startTime;
    private String endTime;
}