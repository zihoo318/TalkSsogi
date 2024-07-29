package com.talkssogi.TalkSsogi_server.controller;


import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.service.AnalysisResultService;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api")
public class Page2Controller {

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;
    private final AnalysisResultService analysisResultService;

    @Autowired
    public Page2Controller(ChattingRoomService chattingRoomService, UserService userService, AnalysisResultService analysisResultService) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
        this.analysisResultService = analysisResultService;
    }

    @GetMapping("/chatrooms") // 채팅방 목록 보내기
    public ResponseEntity<Map<Integer, String>> getChatRooms(@RequestParam String ID) {
        // 특정 사용자의 정보를 가져옵니다.
        User user = userService.findUserById(ID);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 사용자의 채팅방 목록을 가져옵니다.
        Set<ChattingRoom> chatRooms = user.getChatList();
        // 반환할 채팅방 목록을 저장할 Map을 선언합니다.
        Map<Integer, String> chatRoomsMap = new HashMap<>();

        // 각 채팅방에 대해 번호와 이름을 Map에 추가합니다.
        for (ChattingRoom room : chatRooms) {
            Integer roomNumber = room.getCrNum(); // 채팅방 번호 가져오기
            String roomName = room.getAnalysisResult() != null
                    ? room.getAnalysisResult().getChatroomName()
                    : "분석을 실행해주세요"; // 채팅방 이름 가져오기 (이름이 없는 경우의 설정)
            chatRoomsMap.put(roomNumber, roomName); // 채팅방 번호와 이름을 Map에 추가합니다.
        }


        // 생성된 채팅방 목록을 HttpStatus OK와 함께 ResponseEntity로 반환합니다.
        return new ResponseEntity<>(chatRoomsMap, HttpStatus.OK);
    }

    @DeleteMapping("/chatrooms/{crNum}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Integer crNum) {
        ChattingRoom room = chattingRoomService.findByCrNum(crNum);

        if (room == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        chattingRoomService.deleteChattingRoom(crNum);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
