package com.talkssogi.TalkSsogi_server.controller;


import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.repository.AnalysisResultRepository;
import com.talkssogi.TalkSsogi_server.repository.ChattingRoomRepository;
import com.talkssogi.TalkSsogi_server.service.AnalysisResultService;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class Page3Controller {

    private final ChattingRoomService chattingRoomService;
    private final ChattingRoomRepository chattingRoomRepository;
    private final AnalysisResultRepository analysisResultRepository;

    @Autowired
    public Page3Controller(ChattingRoomService chattingRoomService,ChattingRoomRepository chattingRoomRepository, AnalysisResultRepository analysisResultRepository) {
        this.chattingRoomService = chattingRoomService;
        this.chattingRoomRepository = chattingRoomRepository;
        this.analysisResultRepository=analysisResultRepository;
    }

    @PostMapping("/uploadfile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("userId") String userId,
                                             @RequestParam("headcount") int headcount) {
        try {
            ChattingRoom chattingRoom = chattingRoomService.handleFileUpload(file, userId, headcount);

            // Map을 사용하여 JSON 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("crNum", chattingRoom.getCrNum());
            response.put("filePath", chattingRoom.getFilePath());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
        }
    }
}