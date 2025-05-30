![header](https://capsule-render.vercel.app/api?type=waving&color=gradient&height=192&section=header&text=BACKEND%20CHAT&fontSize=90&animation=fadeIn&fontColor=FFF)


<p align="center">
  <img src="https://img.shields.io/badge/spring-%236DB33F.svg?&style=for-the-badge&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/fastapi-%23009688.svg?&style=for-the-badge&logo=fastapi&logoColor=white"/>
  <img src="https://img.shields.io/badge/gradle-%2302303A.svg?&style=for-the-badge&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/java-%23007396.svg?&style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/docker-%232496ED.svg?&style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/mysql-%234479A1.svg?&style=for-the-badge&logo=mysql&logoColor=white"/>
    <br>
  <img src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white"/>
</p>

## 프로젝트 소개
- 시연용 채팅 backend code
- WebSocket을 이용한 실시간 채팅 시스템
- 비속어가 포함된 메시지를 FastAPI를 통해 필터링 후 JPA 기반으로 채팅 내용 및 사용자 상태를 관리
- 비속어 감지 시 비속어 사용 횟수 증가
- 모든 사용자가 채팅방을 나간 경우 채팅방 초기화 처리

## Member
- 송보민(PL)   :  프록시 서버 연동
- 구강현       :  ERD 작성 및 쿼리 작성, 초기 설정, 코드 유지 보수
- 정혜지       :  ERD 작성 및 쿼리 작성, 코드 유지 보수
- 표상혁       :  코드 설계

## 기본 설정
- application.properties 파일 생성
- 노션 -> 백엔드 정보 모음/application.properties 참고
- java 17
- Spring Boot 3.4.4
- gradle-8.13

## 로컬 docker 설치
- docker run --name mysql-db -e MYSQL_ROOT_PASSWORD={YOURPASSWORD} -p 3306:3306 -d mysql:8.4
  - 쿼리 : query.sql

## 프로젝트 기간
- 2025.03.26 ~ 2025.06.05

# 툴체인 & 프레임워크

## 프레임워크

| 분류            | 사용 기술           | 설명                                            |
| ------------- | --------------- | --------------------------------------------- |
| **백엔드 프레임워크** | Spring Boot     | 채팅 API, WebSocket, JPA 등 통합 애플리케이션 프레임워크 구성   |
| **실시간 통신**    | WebSocket       | 클라이언트와 서버 간 실시간 메시지 송수신을 위한 이중 연결 통신 처리       |
| **보안**        | Spring Security | 인증/인가 필터 체인을 통한 보안 설정 및 접근 제어 구성              |
| **데이터베이스**    | MySQL           | 채팅방, 사용자, 메시지 로그 등 영속적 데이터 저장 및 관리            |
| **ORM**       | Spring Data JPA | 엔티티 객체 기반으로 DB 테이블을 매핑하고 Repository를 통한 쿼리 수행 |

## 툴체인

| 분류           | 사용 기술         | 설명                                           |
| ------------ | ------------- | -------------------------------------------- |
| **IDE**      | IntelliJ IDEA | Spring Boot 및 Java 프로젝트 개발에 최적화된 통합 개발 환경    |
| **빌드 도구**    | Gradle        | 프로젝트 빌드, 실행, 의존성 관리를 자동화하는 빌드 시스템            |
| **버전 관리**    | Git + GitHub  | 협업을 위한 소스 코드 이력 관리 및 브랜치 기반 버전 관리 시스템        |
| **테스트 도구**   | Postman       | WebSocket 및 REST API의 테스트와 시뮬레이션에 사용되는 툴     |
| **기타 라이브러리** | Lombok        | 반복되는 getter, setter, constructor 등의 코드 자동 생성 |
| **JDK/JRE**  | Java 17       | 애플리케이션 실행을 위한 Java 런타임 환경 (LTS 버전)           |
| **DB 툴**     | DBeaver       | MySQL RDS 접속 및 테이블, 데이터 확인용 데이터베이스 GUI 툴     |
| **인프라 관리**   | AWS Console   | EC2, RDS 등 AWS 리소스를 관리하고 모니터링하는 웹 기반 대시보드    |


---

## 채팅 API
- WebSocket 연결
   - Endpoint : `/ws/chat`
   - 프로토콜 : `WebSocket + STOMP`

- 메시지 전송
  - Publish : `/pub/chat/message`
  - Payload 예시 :
  ```
  {
   "type": "TALK",         // 메시지 유형 (ENTER, TALK, LEAVE)
   "sender": "닉네임",
   "message": "보낼 메시지"
   }
   ```
  
- 메시지 수신
  - Subscribe :  `/sub/chat/room/1`
   - Payload 예시 :
   ```
  {
  "type": "TALK",
  "sender": "닉네임",
  "message": "정제된 메시지",
  "timestamp": "2025-05-30T10:00:00"
   }
  ```
---

## 프로젝트 디렉토리 구조

```
main/
├── resources/
│   ├── application.properties              # Spring Boot 환경 설정
└── java/org/example/purgo_chat/
    ├── PurgoChatApplication.java           # 메인 실행 클래스
    ├── controller/                         # WebSocket 컨트롤러 및 API 진입점
    ├── service/                            # 비즈니스 로직 (채팅 처리, 필터링 요청 등)
    ├── handler/                            # 사용자 입장/퇴장, 메시지 전송 분기 처리
    ├── config/                             # WebSocket, CORS, RestTemplate 설정
    ├── dto/                                # STOMP 메시지 송수신용 DTO
    ├── entity/                             # ChatRoom, ChatMessage 엔티티
    └── repository/                         # ChatRoomRepository, ChatMessageRepository 등 JPA 인터페이스
```

---

## 주요 디렉토리 설명
- controller/
  - ChatController.java 등 WebSocket 메시지 처리용 컨트롤러가 위치. /pub/chat/message 수신 처리 담당.

- service/
  - ChatService, FilterService 등이 존재하며, 메시지 저장, 비속어 필터링 요청 전송, 메시지 브로드캐스트 로직 수행.

- handler/
  - StompHandler.java 존재. 클라이언트의 CONNECT 요청 JWT 인증 필터링 및 세션 연결 처리 담당.

- config/
  - WebSocketConfig.java, WebConfig.java, RestTemplateConfig.java 등 설정 파일 포함. WebSocket, CORS, HTTP 요청 설정 관리.

- dto/
  - ChatMessageDto, FilterResponse 등 WebSocket 및 AI 필터링 응답 DTO 정의.

- entity/
  - ChatRoom, ChatMessageEntity 등 DB 테이블과 매핑되는 클래스 정의.

- repository/
  - ChatRoomRepository, ChatMessageRepository 등 DB 접근을 위한 JPA 레포지토리 인터페이스 정의.

---

## 전체 흐름 구조

1. 클라이언트가 /ws/chat 엔드포인트로 WebSocket 연결
2. 클라이언트는 /pub/chat/message 경로로 STOMP 메시지 전송
3. 메시지의 type 값에 따라 서버에서 다음과 같이 처리:
- ENTER: 채팅방에 사용자 닉네임을 빈 슬롯(user1~user8)에 등록
- TALK: 메시지를 FastAPI 프록시 서버로 전달해 욕설 여부 분석 → 정제된 메시지 저장 및 /sub/chat/room/1 브로드캐스트
- LEAVE: 사용자 닉네임 제거 및 leaveCount 증가 → leaveCount == 2일 경우 채팅방 삭제

```plaintext
[Client] ─▶ WebSocket 연결 (/ws/chat)
   │
   ├─▶ /pub/chat/message (type=ENTER)
   │      └─▶ [ChatService] 채팅방 사용자 슬롯에 닉네임 등록
   │
   ├─▶ /pub/chat/message (type=TALK)
   │      └─▶ [FilterService] FastAPI에 욕설 필터링 요청
   │              └─▶ [ChatService] 정제된 메시지 저장 및 브로드캐스트 (/sub/chat/room/1)
   │
   └─▶ /pub/chat/message (type=LEAVE)
          └─▶ [ChatService] 닉네임 제거 및 leaveCount 증가 → 8명 퇴장 시 채팅방 초기화
```

---

##  욕설 필터링 방식

- `BadwordFilterService`는 FastAPI 서버에 메시지를 POST 요청으로 전달
- FastAPI 응답 구조 예시:
```json
{
  "isAbusive": true,
  "originalText": "욕설 포함된 문장",
  "rewrittenText": "***"
}
```
- 욕설이 감지되면 `chatService.incrementBadwordCount()` 실행
- FastAPI 장애 시 원문 그대로 반환


---

## 핵심 기능 요약

- 실시간 채팅 (WebSocket)
- 욕설 필터링 (FastAPI 연동)
- 채팅방 입퇴장 추적
- JPA를 통한 DB 자동 매핑 및 저장
- 비속어 사용 횟수 저장


