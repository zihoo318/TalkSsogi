package com.talkssogi.TalkSsogi_server.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "user")
public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class); // 로그 출력하기 위함

    @Id //기본키
    @Column(name = "userId") // 데이터베이스에서 사용되는 컬럼 이름
    private String userId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    User가 여러 개의 ChattingRoom와 관계를 맺고 있다
//    ChattingRoom 엔티티의 user 필드에 의해 매핑된다 chattingRoom에서 외래 키를 관리하고 User는 관계를 읽기만
//    부모 엔티티와의 연관 관계가 끊어진 자식 엔티티를 자동으로 삭제할지를 결정
    private Set<ChattingRoom> chatList = new HashSet<>();

    // 기본 생성자 (Hibernate가 사용)
    public User() {
    }

    // 명시적 생성자 (애플리케이션 코드가 사용)
    public User(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Set<ChattingRoom> getChatList() {
        return chatList;
    }

    public void addChatRoom(ChattingRoom room) {
        this.chatList.add(room);
        room.setUser(this);  // 양방향 관계 유지

        // 로그 출력
        logger.info("Added chat room with ID {} to user with ID {}. Current chat list: {}",
                room.getCrNum(), this.userId, this.chatList);
    }


}