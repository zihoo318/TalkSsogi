package com.talkssogi.TalkSsogi_server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkssogi.TalkSsogi_server.controller.PythonController;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.domain.User;
import com.talkssogi.TalkSsogi_server.processor.PythonResultProcessor;
import com.talkssogi.TalkSsogi_server.repository.ChattingRoomRepository;
import com.talkssogi.TalkSsogi_server.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ChattingRoomService {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class); // 로그 출력
    private static final String UPLOAD_DIR = "C:/Talkssogi_Workspace/TalkSsogi/"; // "/"까지 해야됨!
    //테스트용 경로

    @Autowired
    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;

    @Autowired
    private PythonResultProcessor pythonResultProcessor;

    @Autowired
    public ChattingRoomService(ChattingRoomRepository chattingRoomRepository, UserRepository userRepository) {
        this.chattingRoomRepository = chattingRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void save(ChattingRoom chattingRoom) {
        chattingRoomRepository.save(chattingRoom);
    }

    public void saveRankingResults(Integer crNum, String jsonResults) {
        Map<String, Map<String, String>> rankingResults = pythonResultProcessor.extractRankingResults(jsonResults);

        if (rankingResults == null) {
            System.out.println("Error processing ranking results");
            return;
        }

        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crNum).orElse(null);

        if (chattingRoom != null) {
            chattingRoom.setBasicRankingResults(rankingResults);
            chattingRoomRepository.save(chattingRoom);
        } else {
            System.out.println("ChattingRoom with ID " + crNum + " not found.");
        }
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
        try {
            ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crnum).orElse(null);
            if (chattingRoom != null && chattingRoom.getMemberNames() != null) {
                return chattingRoom.getMemberNames();
            }
            return List.of();
        } catch (DataAccessException e) {
            logger.error("채팅방 멤버 조회 중 데이터베이스 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("채팅방 멤버 조회에 실패했습니다.", e);
        }
    }

    public Map<String, Map<String, String>> getBasicRankingResults(Integer crNum) {
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crNum).orElse(null);
        if (chattingRoom != null) {
            return chattingRoom.getBasicRankingResults();
        }
        return null;
    }

    public Map<String, Map<String, String>> getSearchRankingResults(int crnum, String keyword) {
        // 파이썬 스크립트를 실행하여 결과를 가져오는 로직을 추가
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crnum).orElse(null);
        if (chattingRoom == null) {
            logger.error("ChattingRoom with ID " + crnum + " not found.");
            return Collections.emptyMap();
        }

        String filePath = chattingRoom.getFilePath(); // chat file path를 설정해야 함
        String searchResultsFilePath = UPLOAD_DIR + "search_ranking_results.json"; // 파이썬 스크립트가 생성하는 파일 경로
        String pythonFilePath = UPLOAD_DIR + "search_ranking_result.py";

        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFilePath , filePath, keyword);
        processBuilder.redirectErrorStream(true);

        // 환경 변수 설정
        Map<String, String> env = processBuilder.environment();
        env.put("PYTHONIOENCODING", "utf-8");

        try {
            // 기존 결과 파일 삭제
            Files.deleteIfExists(Paths.get(searchResultsFilePath));

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                logger.info(line); // 파이썬 스크립트 실행 로그 출력
                result.append(line);
            }
            reader.close();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Python script execution failed with exit code 실패 " + exitCode);
                return Collections.emptyMap();
            }

            Path searchResultsPath = Paths.get(searchResultsFilePath);
            if (!Files.exists(searchResultsPath)) {
                logger.error("Python script did not generate expected output file.");
                return Collections.emptyMap();
            }

            String jsonString = new String(Files.readAllBytes(searchResultsPath));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Map<String, String>> rankingResultsMap = mapper.readValue(jsonString, new TypeReference<Map<String, Map<String, String>>>(){});

            chattingRoom.setSearchRankingResults(rankingResultsMap);
            this.save(chattingRoom); // chattingRoomService.save(chattingRoom) 대신 this.save(chattingRoom) 사용

            return rankingResultsMap;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    @Transactional
    public boolean deleteChattingRoom(Integer crNum) {
        try {
            if (chattingRoomRepository.existsById(crNum)) {
                chattingRoomRepository.deleteById(crNum);
                logger.info("채팅방 삭제 성공, ID: {}", crNum);
                return true;
            } else {
                logger.warn("채팅방 삭제 실패, 채팅방 ID가 존재하지 않음: {}", crNum);
                return false;
            }
        } catch (DataAccessException e) {
            logger.error("채팅방 삭제 중 데이터베이스 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("채팅방 삭제에 실패했습니다.", e);
        }
    }

    public List<String> getBasicActivityAnalysis(int crnum) {
        ChattingRoom chattingRoom = chattingRoomRepository.findById(crnum).orElse(null);

        // chattingroom 있는지 확인
        if (chattingRoom != null) {
            return chattingRoom.getBasicActivityAnalysis();
        }

        // chattingroom 없으면 빈 리스트 반환
        return List.of();
    }

    @Transactional
    public String findWordCloudImageUrlByCrnum(Integer crnum) {
        try {
            return chattingRoomRepository.findByCrNum(crnum)
                    .map(ChattingRoom::getWordCloudImageUrl)
                    .orElse(null);
        } catch (DataAccessException e) {
            logger.error("워드 클라우드 이미지 URL 조회 중 데이터베이스 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("워드 클라우드 이미지 URL 조회에 실패했습니다.", e);
        }
    }

    @Transactional
    public String findWordCloudImageUrlByCrnumAndUserId(Integer crnum, String userId) {
        try {
            return chattingRoomRepository.findByCrNumAndUser_UserId(crnum, userId)
                    .map(ChattingRoom::getWordCloudImageUrl)
                    .orElse(null);
        } catch (DataAccessException e) {
            logger.error("워드 클라우드 이미지 URL 조회 중 데이터베이스 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("워드 클라우드 이미지 URL 조회에 실패했습니다.", e);
        }
    }

    // 인덱스를 이용해 멤버 이름을 가져오는 메서드
    public String getMemberNameByIndex(int crnum, int index) {
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crnum).orElse(null);
        List<String> memberNames = chattingRoom.getMemberNames();
        if (index >= 0 && index < memberNames.size()) {
            return memberNames.get(index);
        } else {
            return null;
        }
    }

    // 멤버 이름을 이용해 인덱스를 가져오는 메서드
    public Integer getMemberIndexByName(int crnum, String name) {
        ChattingRoom chattingRoom = chattingRoomRepository.findByCrNum(crnum).orElse(null);
        List<String> memberNames = chattingRoom.getMemberNames();
        int index = memberNames.indexOf(name);
        return index >= 0 ? index : null;
    }

}