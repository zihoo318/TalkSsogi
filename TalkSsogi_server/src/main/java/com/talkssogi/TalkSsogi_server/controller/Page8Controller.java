package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
page8 서버에서 가져올 데이터
-메세지 수가 가장 많았던 날
-대화를 하지 않은 날
-평균적으로 가장 활발한 시간대
=>page7Service에서 파이썬 코드를 실행해서 생성된 AnalysisResult의 basicRankingResults랑 searchRankingResults를 전달하도록 할건데
아직 파이썬 코드를 못 만들어서 임시로 각 객체의 변수에 값이 있다고 치고 전달하도록 서비스파일이랑 컨트롤러 파일을 만들기
->서비스에서 안드로 보낼 데이터를 만들고(=파이썬 돌렸다 치고 임시 대이터 만드는 과정)
->컨트롤러에서 api만들어서 매핑
=>안드로이드 스튜디오에서 만든 코드 다시 보고 어떤 형태로 데이터를 넘겨줘야하는지 고려해서 api만들기
 */
@RestController
@RequestMapping("/api/basics")
public class Page8Controller {

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;

    @Autowired
    public Page8Controller(ChattingRoomService chattingRoomService, UserService userService) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
    }

    // db에 저장된 리스트를 전달
    @GetMapping("/activityAnalysis")
    public List<String> getActivityAnalysis(@RequestParam("crnum") int crnum) {
        return chattingRoomService.getBasicActivityAnalysis(crnum);
    }
}