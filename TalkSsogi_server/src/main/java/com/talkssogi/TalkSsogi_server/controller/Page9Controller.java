package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.service.AnalysisResultService;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Page9Controller {
    private final ChattingRoomService chattingRoomService;
    private final UserService userService;
    private final AnalysisResultService analysisResultService;

    @Autowired
    public Page9Controller(ChattingRoomService chattingRoomService, UserService userService, AnalysisResultService analysisResultService) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
        this.analysisResultService = analysisResultService;
    }

    // 1. 채팅방의 대화 참여자 이름 목록
    //     @GetMapping("/members/{crnum}")
    //    public ResponseEntity<List<String>> getChattingRoomMembers(@PathVariable Integer crnum) {
    //        List<String> members = chattingRoomService.getChattingRoomMembers(crnum);
    //        if (members.isEmpty()) {
    //            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //        }
    //        return new ResponseEntity<>(members, HttpStatus.OK);
    //    }
    // Page6Controller의 api재사용

    // 2. 검색 결과 이미지 url
    // PythonController에 있음
    // @PostMapping("/personalActivityAnalysisImage")
    //    public ResponseEntity<String> getActivityAnalysisImage(
    //            @RequestParam("startDate") String startDate,
    //            @RequestParam("endDate") String endDate,
    //            @RequestParam("searchWho") String searchWho,
    //            @RequestParam("resultsItem") String resultsItem,
    //            @RequestParam("crnum") Integer crnum) {
    //        try {

}
