CREATE DATABASE purgoChat;
USE purgoChat;

-- 1. 채팅방 테이블 생성
CREATE TABLE ChatRoom (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user1_name VARCHAR(255) NULL,
                          user2_name VARCHAR(255) NULL,
                          user3_name VARCHAR(255) NULL,
                          user4_name VARCHAR(255) NULL,
                          user5_name VARCHAR(255) NULL,
                          user6_name VARCHAR(255) NULL,
                          user7_name VARCHAR(255) NULL,
                          user8_name VARCHAR(255) NULL,
                          badword_count INT DEFAULT 0,
                          leave_count INT DEFAULT 0
);

-- 2. 메시지 테이블 생성
CREATE TABLE Message (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         chatroom_id INT NOT NULL,
                         sender_name VARCHAR(255) NOT NULL,
                         receiver_name VARCHAR(255) NOT NULL,
                         content VARCHAR(1000) NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (chatroom_id) REFERENCES ChatRoom(id)
);