package com.talkssogi.TalkSsogi_server.service;

import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.repository.ChattingRoomRepository;
import com.talkssogi.TalkSsogi_server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChattingRoomRepository chattingRoomRepository;

    @Transactional
    public List<String> getAllUserIds() {
        List<User> users = userRepository.findAll();
        return users.stream().map(User::getUserId).collect(Collectors.toList());
    }

    // 사용자 ID 존재 여부 확인(페이지7에서 사용)
    @Transactional
    public boolean userIdExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Transactional
    public boolean userIdExistsForPage1(String userId) {
        return userRepository.existsById(userId);
    }

    @Transactional
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public User findUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    @Transactional
    public Set<ChattingRoom> getChattingRoomsByUserId(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            return new HashSet<>(user.getChatList());
        }
        return new HashSet<>();
    }

    @Transactional
    private int generateNextChattingRoomNumber(User user) {
        Set<ChattingRoom> chatRooms = user.getChatList();
        int count = chatRooms.size();
        return count + 1;
    }

    public void registerUser(String userId) {
        User user = new User(userId);
        userRepository.save(user);
    }
}
