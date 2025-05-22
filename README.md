# 💬 Purgo Chat - 실시간 채팅 및 욕설 필터링 시스템

Purgo Chat은 WebSocket을 이용한 실시간 채팅 시스템으로, 욕설이 포함된 메시지를 FastAPI를 통해 필터링하고, JPA 기반으로 채팅 내용 및 사용자 상태를 관리합니다.

---

## 📦 프로젝트 디렉토리 구조

```
main/
├── resources/
│   ├── application.properties              # Spring Boot 설정 파일
│   └── static/
│       └── index.html                      # 정적 웹 파일
└── java/org/example/purgo_chat/
    ├── PurgoChatApplication.java           # 메인 실행 클래스
    ├── controller/                         # WebSocket 컨트롤러
    ├── service/                            # 비즈니스 로직 (채팅, 필터링 등)
    ├── handler/                            # WebSocket 핸들러
    ├── config/                             # 설정 클래스 (WebSocket, RestTemplate)
    ├── dto/                                # 데이터 전송 객체 (DTO)
    ├── entity/                             # JPA 엔티티 (DB 매핑 클래스)
    └── repository/                         # Spring Data JPA 레포지토리
```

---

## 📁 주요 디렉토리 설명

- `controller/` : WebSocket 메시지의 진입점 (ENTER, TALK, LEAVE 처리)
- `service/` : ChatService, BadwordFilterService 등 핵심 로직 담당
- `handler/` : WebSocket 메시지의 구체적인 분기 처리
- `config/` : WebSocket 설정 및 외부 API 호출을 위한 RestTemplate 설정
- `dto/` : 클라이언트와 주고받는 JSON 구조 정의
- `entity/` : JPA 기반 테이블 매핑 (ChatRoom, Message)
- `repository/` : Spring Data JPA를 활용한 DB 접근 레이어

---

## 🔁 전체 흐름 구조

1. 클라이언트가 WebSocket을 통해 접속
2. 메시지 타입에 따라 분기 처리:
   - `ENTER`: 사용자 닉네임 등록
   - `TALK`: 메시지를 FastAPI에 전달하여 욕설 여부 분석 → 저장 및 전송
   - `LEAVE`: 사용자 퇴장 처리
3. DB에는 채팅방(ChatRoom)과 메시지(Message)가 저장됨

```plaintext
[Client] ─▶ WebSocket 연결
   │
   ├─▶ ENTER → [ChatService] 사용자 등록
   ├─▶ TALK  → [BadwordFilterService] 욕설 분석
   │           └─▶ [ChatService] 메시지 저장 + 브로드캐스트
   └─▶ LEAVE → [ChatService] 퇴장 처리
```

---

## ⚙️ 욕설 필터링 방식

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

## 🗃️ 데이터베이스 구조 및 설명 (MySQL 기준)

### 📁 ChatRoom 테이블  
채팅방의 정보를 저장합니다. 사용자 닉네임, 욕설 수, 퇴장 수 등을 관리합니다.

```sql
CREATE TABLE ChatRoom (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_name VARCHAR(255) NULL,
    user2_name VARCHAR(255) NULL,
    badword_count INT DEFAULT 0,
    leave_count INT DEFAULT 0
);
```

### 📁 Message 테이블  
각 메시지 기록을 저장합니다. 채팅방 ID를 외래키로 연결하며, 송수신자 및 생성 시간을 포함합니다.

```sql
CREATE TABLE Message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id INT NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chatroom_id) REFERENCES ChatRoom(id)
);
```

---

## 🧩 핵심 기능 요약

- ✅ 실시간 채팅 (WebSocket)
- ✅ 욕설 필터링 (FastAPI 연동)
- ✅ 채팅방 입퇴장 추적
- ✅ JPA를 통한 DB 자동 매핑 및 저장

---

## 🛠️ 향후 개선 아이디어

- 사용자 인증(JWT 기반)
- 채팅방 다중화
- 욕설 자동 필터 수준 조정
- 관리자 모니터링 기능

- 
