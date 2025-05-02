# 📚 LectureRoomReservationApp

소프트웨어공학 팀 프로젝트로 진행된 **강의실 예약 관리 시스템**입니다.  
본 프로젝트는 동의대학교 컴퓨터소프트웨어공학과에서 운영 중인 강의실을  
보다 **효율적으로 활용**할 수 있도록 설계된 Java 기반의 GUI 프로그램입니다.

---

## 📝 개요

- 강의 시간이 아닌 **빈 시간대의 강의실**을 학생들이 자유롭게 예약할 수 있도록 지원
- **개인 학습**, **스터디**, **단체 프로젝트** 등 다양한 목적으로 강의실을 활용할 수 있도록 설계
- 수기 예약이나 비효율적인 사용을 방지하고, **편리한 예약 관리 인터페이스**를 제공

---

## 📌 설계 제약 사항

| 항목           | 내용                                                         |
|----------------|--------------------------------------------------------------|
| 사용 언어      | Java (JDK 21)                                                |
| 아키텍처 패턴  | MVC 패턴 적용 (Model / View / Controller / Service / Repository 구분) |
| 프레임워크     | Spring Boot 등 프레임워크 사용 금지                          |
| 데이터 저장    | 데이터베이스 사용 가능하나 평가에선 제외됨 (→ 파일 시스템 기반으로 영구 저장) |

---

## ✅ 주요 기능

### 👤 일반 사용자
- 🗓️ **빈 강의실 예약**: 사용자가 시간표를 확인 후 예약 가능
- 📋 **개인 예약 관리**: 자신의 예약 현황 확인 및 취소

### 🛠️ 관리자
- 📌 **예약 관리**: 전체 예약 현황 확인, 중복/비정상 예약 정리
- 👥 **사용자 관리**: 사용자 목록 확인 및 정보 수정/삭제

---

## 🖥️ 실행 환경

- **Java 버전**: JDK 21
- **운영 체제**: Windows 10 이상 / macOS Ventura 이상
- **IDE**: NetBeans 12.6, IntelliJ IDEA (동시 사용)
- **실행 방법**: `Main.java` 실행
- **외부 라이브러리**:
    - [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) – YAML 파일 입출력 처리용

---

## 🧱 아키텍처 계층 및 디자인 패턴 적용

| 계층         | 적용 패턴                        | 설명 |
|--------------|----------------------------------|------|
| **View (Swing)** | MVC View, *(Observer 선택)*          | 사용자 UI 구성, 이벤트 처리, 상태 표시 |
| **Controller**   | Front Controller, *(Command 선택)*  | 사용자 요청 수신 및 분기 처리 |
| **Service**      | Facade, Strategy, Template Method *(선택)* | 검증/저장/알림 등의 로직을 단순화하여 제공 |
| **Repository**   | Repository, Singleton *(DAO 유사)* | 파일 기반 CRUD 및 데이터 접근 추상화 |
| **Model (Entity)** | DTO, JavaBean *(Builder 선택)*      | 도메인 객체, 계층 간 데이터 전달 역할 |

---

## 🗂️ 프로젝트 구조
    src
    └── main
    ├── java
    │   └── deu
    │       ├── controller      # 사용자 요청 처리, 이벤트 흐름 제어
    │       ├── model           # Entity, DTO 등 데이터 구조 정의
    │       ├── repository      # 파일 기반 CRUD 및 데이터 접근
    │       ├── service         # 비즈니스 로직, 검증 및 처리
    │       └── view            # Swing 기반 사용자 인터페이스
    └── resources
    └── Images-new.logo     # 이미지 및 정적 리소스 파일

---

## 👨‍👩‍👧‍👦 팀원 정보

- 👑 **팀장**: 강준화
- 👥 **팀원**: 김영진, 김원형, 이건일, 이시연

---

## UI
![로그인](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png)
![회원가입](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85.png)
![내 예약 정보](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%EB%82%B4%20%EC%98%88%EC%95%BD%20%EC%A0%95%EB%B3%B4.png)
![강의실 예약](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%EA%B0%95%EC%9D%98%EC%8B%A4%20%EC%98%88%EC%95%BD.png)
![강의실 예약 관리](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%EA%B0%95%EC%9D%98%EC%8B%A4%20%EC%98%88%EC%95%BD%EA%B4%80%EB%A6%AC.png)
![사용자 관리](https://github.com/oxxultus/LectureRoomReservationSystem/blob/main/UI%20IMAGES/%EC%82%AC%EC%9A%A9%EC%9E%90%20%EA%B4%80%EB%A6%AC.png)
> 이 프로젝트는 향후 웹 기반 리팩토링을 고려하고 있으며, 구조적 확장성을 중시하여 설계되었습니다.
