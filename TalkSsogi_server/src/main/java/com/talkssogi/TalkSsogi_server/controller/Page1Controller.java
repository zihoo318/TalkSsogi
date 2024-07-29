package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class Page1Controller {

    private final UserService userService;


    @Autowired
    public Page1Controller(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userIds")  // 기존에 있는 아이디들 목록으로 보내주기(for 아이디 중복 확인)
    public ResponseEntity<List<String>> getAllUserIds() {
        List<String> userIds = userService.getAllUserIds();
        return new ResponseEntity<>(userIds, HttpStatus.OK);
    }

    @PostMapping("/userId")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        // User 객체에서 userId 추출
        String userId = user.getUserId();

        // 새로운 사용자 객체를 생성
        User newUser = new User(userId);

        // UserService를 통해 사용자 저장
        userService.addUser(newUser);

        // 성공 응답 반환
        return ResponseEntity.ok("User created successfully");
    }
}