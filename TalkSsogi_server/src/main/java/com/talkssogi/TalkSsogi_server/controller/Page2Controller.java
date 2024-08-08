package com.talkssogi.TalkSsogi_server.controller;


import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping("/api")
public class Page2Controller {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class);

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;

    @Autowired
    public Page2Controller(ChattingRoomService chattingRoomService, UserService userService) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
    }

    @GetMapping(value = "/chatrooms", produces="application/json; charset=utf8") // 채팅방 목록 보내기
    public ResponseEntity<Map<Integer, String>> getChatRooms(@RequestParam("ID") String ID) {
        logger.info("Fetching chat rooms for user ID: {}", ID);

        // 응답 헤더에 Content-Type과 charset을 명시적으로 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));


        // 특정 사용자의 정보를 가져옵니다.
        User user = userService.findUserById(ID);

        if (user == null) {
            logger.warn("User not found: {}", ID);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 사용자의 채팅방 목록을 가져옵니다.
        Set<ChattingRoom> chatRooms = user.getChatList();
        // 반환할 채팅방 목록을 저장할 Map을 선언합니다.
        Map<Integer, String> chatRoomsMap = new HashMap<>();

        // 각 채팅방에 대해 번호와 이름을 Map에 추가합니다.
        for (ChattingRoom room : chatRooms) {
            Integer roomNumber = room.getCrNum(); // 채팅방 번호 가져오기
            String roomName = room.getChatroomName() != null
                    ? room.getChatroomName()
                    : "분석을 실행해주세요"; // 채팅방 이름 가져오기 (이름이 없는 경우의 설정)
            chatRoomsMap.put(roomNumber, roomName); // 채팅방 번호와 이름을 Map에 추가합니다.
        }

        logger.info("Returning chat rooms for user ID 여기여여여여여여겨고ㅑ 인코딩 설정 확인 : {}", chatRoomsMap);
        // 생성된 채팅방 목록을 HttpStatus OK와 함께 ResponseEntity로 반환합니다.
        return new ResponseEntity<>(chatRoomsMap, headers, HttpStatus.OK);
    }

    // 클라이언트로부터 채팅방 번호를 받아 ChattingRoomService의 deleteChattingRoom 메서드를 호출하여 삭제를 시도함
    // 삭제 성공 여부에 따라 적절한 HTTP 상태 코드 (204 NO_CONTENT 또는 404 NOT_FOUND)를 반환함
    @DeleteMapping("/chatrooms/{crNum}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Integer crNum) {
        try {
            logger.info("Deleting chat room with ID: {}", crNum);

            boolean isDeleted = chattingRoomService.deleteChattingRoom(crNum);

            if (isDeleted) {
                logger.info("Successfully deleted chat room with ID: {}", crNum);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                logger.warn("Chat room not found with ID: {}", crNum);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error occurred while deleting chat room with ID(채팅방 삭제 중 오류 발생): {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}