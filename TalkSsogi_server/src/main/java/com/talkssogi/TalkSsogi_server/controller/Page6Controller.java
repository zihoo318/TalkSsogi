package com.talkssogi.TalkSsogi_server.controller;


import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
page6Service에서 파이썬 코드를 실행해서 생성된 AnalysisResult의 wordCloudImageUrl을 전달하도록 할건데
아직 파이썬 코드를 못 만들어서 임시로 wordCloudImageUrl에 값이 있다고 치고 전달하도록 서비스파일이랑 컨트롤러 파일을 만들기
=>안드로이드 스튜디오에서 만든 코드 다시 보고 어떤 형태로 데이터를 넘겨줘야하는지 고려해서 api만들기
 */

@RestController
@RequestMapping("/api")
public class Page6Controller {

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;

    @Autowired
    public Page6Controller(ChattingRoomService chattingRoomService, UserService userService) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
    }

    @GetMapping("/members/{crnum}")
    public ResponseEntity<List<String>> getChattingRoomMembers(@PathVariable("crnum") Integer crnum) {
        List<String> members = null;
        try {
            members = chattingRoomService.getChattingRoomMembers(crnum);
            if (members == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 수정 가능한 리스트로 변환
            List<String> modifiableMembers = new ArrayList<>(members);
            // "전체"를 리스트의 첫 번째 위치에 추가
            modifiableMembers.add(0, "전체");

            return new ResponseEntity<>(modifiableMembers, HttpStatus.OK);
        } catch (Exception e) {
            // 예외 발생 시 로그 기록
            Logger.getLogger(Page6Controller.class.getName()).log(Level.SEVERE, "Error occurred while fetching members", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 파이썬컨트롤러에 있음
//    @GetMapping("/wordCloudImageUrl/{crnum}/{userId}")
//    public ResponseEntity<String> getWordCloudImageUrl(@PathVariable Integer crnum, @PathVariable String userId) {
//        String wordCloudImageUrl = chattingRoomService.findWordCloudImageUrlByCrnumAndUserId(crnum, userId);
//        if (wordCloudImageUrl == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(wordCloudImageUrl, HttpStatus.OK);
//    }
}