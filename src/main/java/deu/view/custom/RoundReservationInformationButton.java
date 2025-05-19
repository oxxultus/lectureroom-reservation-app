package deu.view.custom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundReservationInformationButton extends ButtonRound{

    private String id;
    private String buildingName;
    private String floor;
    private String lectureRoom;
    private String number;
    private String status;
    private String date; // 날짜
    private String dayOfTheWeek; // 요일

    private String startTime; // 교시
    private String endTime;   // 교시
}