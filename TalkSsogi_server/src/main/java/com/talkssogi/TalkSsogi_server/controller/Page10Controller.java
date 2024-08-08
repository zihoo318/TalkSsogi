package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
public class Page10Controller {

    private static final Logger logger = LoggerFactory.getLogger(Page10Controller.class);

    @Autowired
    private ChattingRoomService chattingRoomService;
    @Autowired
    private PythonController pythonController;

    @GetMapping("/caller-prediction")
    public ResponseEntity<String> getCallerPrediction(@RequestParam Integer crnum, @RequestParam String keyword) {
        ResponseEntity<String> response = pythonController.getCallerPrediction(crnum, keyword);

        logger.info("Caller Prediction 결과 for keyword와 crnum {}, {}: {}", keyword, crnum, response.getBody());

        // 발신자 예측 결과를 ChattingRoom 엔터티에서 가져옴
        ChattingRoom chattingRoom = chattingRoomService.findByCrNum(crnum);
        if (chattingRoom != null) {
            return ResponseEntity.ok(chattingRoom.getCallerPrediction());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ChattingRoom not found");
        }
    }
}
