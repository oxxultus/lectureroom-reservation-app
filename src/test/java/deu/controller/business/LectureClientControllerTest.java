package deu.controller.business;

import deu.model.dto.response.BasicResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// 해당 테스트는 서버가 켜져 있어야지만 작동 가능한 테스트 입니다.
class LectureClientControllerTest {

    private final LectureClientController controller = new LectureClientController();

    @Test
    void testReturnLectureOfWeek() {
        // given: 정상적인 building, floor, room 입력값
        String building = "정보관";
        String floor = "9";
        String room = "912";

        // when: 서버에 주간 강의 정보를 요청
        BasicResponse response = controller.returnLectureOfWeek(building, floor, room);

        // then: 응답이 null이 아니고, 코드가 "200"인지 확인
        assertNotNull(response, "서버로부터 응답이 null이면 안됩니다.");
        assertEquals("200", response.code, "서버 응답 코드가 '200'이어야 합니다.");

        // 데이터 타입 확인
        assertTrue(response.data instanceof deu.model.entity.Lecture[][], "응답 데이터는 2차원 Lecture 배열이어야 합니다.");

        // 강의 배열의 일부 값 출력 (선택)
        deu.model.entity.Lecture[][] schedule = (deu.model.entity.Lecture[][]) response.data;
        System.out.println("배정된 첫 교시 강의: " + schedule[0][0]);
    }
}