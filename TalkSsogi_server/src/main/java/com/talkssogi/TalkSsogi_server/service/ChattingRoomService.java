package com.talkssogi.TalkSsogi_server.service;

import com.talkssogi.TalkSsogi_server.controller.PythonController;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.repository.ChattingRoomRepository;
import com.talkssogi.TalkSsogi_server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service
public class ChattingRoomService {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class); // 로그 출력

    private static final String UPLOAD_DIR = "C:/Users/Master/TalkSsogi_Workspace/"; //테스트용 경로

    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChattingRoomService(ChattingRoomRepository chattingRoomRepository, UserRepository userRepository) {
        this.chattingRoomRepository = chattingRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void save(ChattingRoom chattingRoom) {
        chattingRoomRepository.save(chattingRoom);
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

            // 생성된 ChattingRoom의 ID를 확인할 수 있습니다.
            Integer crNum = chattingRoom.getCrNum();
            System.out.println("Created ChattingRoom with ID: " + crNum);

            // User의 chatList에 추가
            user.addChatRoom(chattingRoom);
            userRepository.save(user);  // User 업데이트 (chatList에 추가)

            return chattingRoom;
        } catch (Exception e) {
            throw new IOException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    // 추가된 메서드
    @Transactional
    public ChattingRoom updateFile(int crnum, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("업데이트할 파일을 선택하세요.");
        }

        // 주어진 crnum에 해당하는 채팅방 검색
        ChattingRoom chattingRoom = findByCrNum(crnum);
        if (chattingRoom == null) {
            throw new IOException("존재하지 않는 채팅방입니다: " + crnum);
        }

        try {
            // 새 파일 저장 경로 설정
            Path uploadPath = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            logger.info("파일 저장을 위한 경로설정 직후(=api 쵸청 잘 받아서 예외 발생 없이 실행 시작) : ", uploadPath);

            // 파일 저장
            Files.write(uploadPath, file.getBytes());

            // 파일 경로 업데이트
            chattingRoom.setFilePath(uploadPath.toString());

            // 데이터베이스에 채팅방 업데이트
            chattingRoomRepository.save(chattingRoom);
            logger.info("파일 업데이트 성공으로 생긴 파일 경로 : ", chattingRoom.getFilePath());

            return chattingRoom;
        } catch (Exception e) {
            throw new IOException("파일 업데이트 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<String> getChattingRoomMembers(Integer crnum) {
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crnum).orElse(null);
        if (chattingRoom != null && chattingRoom.getMemberNames() != null) {
            return chattingRoom.getMemberNames();
        }
        return List.of();
    }

    @Transactional
    public void deleteChattingRoom(Integer crNum) { // 채팅방 삭제 메서드
        chattingRoomRepository.deleteById(crNum);
    }

    @Transactional
    public String findActivityAnalysisImageUrlByCrnum(Integer crnum) {
        return chattingRoomRepository.findByCrNum(crnum)
                .map(ChattingRoom::getActivityAnalysisImageUrl)
                .orElse(null);
    }

    @Transactional
    public String findWordCloudImageUrlByCrnum(Integer crnum) {
        return chattingRoomRepository.findByCrNum(crnum)
                .map(ChattingRoom::getWordCloudImageUrl)
                .orElse(null);
    }

    @Transactional
    public String findWordCloudImageUrlByCrnumAndUserId(Integer crnum, String userId) {
        return chattingRoomRepository.findByCrNumAndUser_UserId(crnum, userId)
                .map(ChattingRoom::getWordCloudImageUrl)
                .orElse(null);
    }
}