package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.controller.LoginRequest;
import com.talkssogi.TalkSsogi_server.controller.RegisterRequest;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class Page1Controller {

    private final UserService userService;

    @Autowired
    public Page1Controller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/userId")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        String userId = user.getUserId();
        if (userService.userIdExistsForPage1(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User ID already exists");
        }
        User newUser = new User(userId);
        userService.addUser(newUser);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String userId = loginRequest.getUserId();
        if (userService.userIdExists(userId)) {
            return ResponseEntity.ok("Success");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid ID");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        String userId = registerRequest.getUserId();
        if (userService.userIdExists(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        } else {
            userService.registerUser(userId);
            return ResponseEntity.ok("Registration successful");
        }
    }

    @GetMapping("/checkUserId")
    public ResponseEntity<String> checkUserId(@RequestParam String userId) {
        if (userService.userIdExistsForPage1(userId)) {
            return ResponseEntity.ok("Username is already in use");
        } else {
            return ResponseEntity.ok("Username is available");
        }
    }
}
