package deu.view.custom;

import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeSlotButton extends ButtonRound {
    private RoomReservation roomReservation;
    private Lecture lecture;
}
