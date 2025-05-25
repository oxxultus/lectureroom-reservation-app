## 프로젝트 구조
```
├── view/                      # Swing 기반 화면 클래스
│   ├── HomeView.java
│   └── AuthView.java
├── controller/
│   ├── event/                 # UI 이벤트 컨트롤러 (버튼/액션 리스너)
│   │   ├── HomeEventController.java
│   │   └── AuthEventController.java
│   └── business/              # 서버 통신 컨트롤러
│       ├── UserClientController.java
│       ├── LectureClientController.java
│       └── ReservationClientController.java
├── component/                 # 커스텀 Swing 컴포넌트
├── model/                     # DTO 클래스 정의
│   ├── dto/
│   └── entity/
└── ClientMain.java            # 클라이언트 진입점
```
## 실행 방법

### 요구사항
- Java 21 이상
- Maven 3.x 이상

### 실행 명령어
```
mvn clean package
java -jar target/DeuLectureRoomClient-1.0.0.jar
```
- 서버 주소: localhost:9999
- 리소스 경로: ./assets/, ./data/
