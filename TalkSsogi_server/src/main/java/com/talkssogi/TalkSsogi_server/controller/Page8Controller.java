// controller/Page8Controller.java
package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basics")
public class Page8Controller {

    @Autowired
    private ChattingRoomService chattingRoomService;

    @GetMapping("/activityAnalysis")
    public Map<String, List<String>> getActivityAnalysis(@RequestParam Integer crNum) {
        // 채팅방 번호에 해당하는 기본 활동 분석 결과를 서비스에서 가져옵니다.
        return chattingRoomService.getBasicActivityAnalysis(crNum);
    }
}
