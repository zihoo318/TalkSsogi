package com.talkssogi.TalkSsogi_server.service;

import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.repository.AnalysisResultRepository;
import com.talkssogi.TalkSsogi_server.repository.ChattingRoomRepository;
import com.talkssogi.TalkSsogi_server.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service
public class ChattingRoomService {

    private static final String UPLOAD_DIR = "C:/Users/KYJ/TalkSsogi_Workspace/"; //테스트용 경로

    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;
    private final AnalysisResultRepository analysisResultRepository;

    @Autowired
    public ChattingRoomService(ChattingRoomRepository chattingRoomRepository, UserRepository userRepository, AnalysisResultRepository analysisResultRepository) {
        this.chattingRoomRepository = chattingRoomRepository;
        this.userRepository = userRepository;
        this.analysisResultRepository=analysisResultRepository;
    }

    @Transactional
    public ChattingRoom findByCrNum(int crnum) {
        return chattingRoomRepository.findByCrNum(crnum).orElse(null); // 채팅방을 찾고 없으면 null 반환
    }

    @Transactional
    public ChattingRoom handleFileUpload(MultipartFile file, String userId, int headcount) throws IOException {
        // MultipartFile을 받아 파일 업로드 처리
        // 파일 업로드 성공 여부에 따라 적절한 응답을 반환
        if (file.isEmpty()) {
            throw new IOException("업로드할 파일을 선택하세요.");
        }

        try {
            // 파일 저장 경로 설정
            Path uploadPath = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            // 파일 저장
            Files.write(uploadPath, file.getBytes());
            // 사용자 확인 및 생성
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                user = new User(userId);
                userRepository.save(user);
            }

            // ChattingRoom 생성 및 사용자와 연결 (db처리)
            ChattingRoom chattingRoom = new ChattingRoom();
            chattingRoom.setFilePath(uploadPath.toString());
            chattingRoom.setHeadcount(headcount);
            chattingRoom.setUser(user);  // User와 연결
            chattingRoomRepository.save(chattingRoom);
            // User의 chatList에 추가
            user.addChatRoom(chattingRoom);
            userRepository.save(user);  // User 업데이트 (chatList에 추가)

            return chattingRoom;
        } catch (Exception e) {
            throw new IOException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<String> getChattingRoomMembers(Integer chatRoomId) {
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(chatRoomId).orElse(null);
        if (chattingRoom != null && chattingRoom.getAnalysisResult() != null) {
            return chattingRoom.getAnalysisResult().getMemberNames();
        }
        return List.of();
    }

    public void deleteChattingRoom(Integer crNum) { // 채팅방 삭제 메서드
        chattingRoomRepository.deleteById(crNum);
    }
}