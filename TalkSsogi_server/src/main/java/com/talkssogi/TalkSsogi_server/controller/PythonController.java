package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.repository.AnalysisResultRepository;
import com.talkssogi.TalkSsogi_server.service.AnalysisResultService;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class PythonController {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class);

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;
    private final AnalysisResultService analysisResultService;
    //private final S3Uploader s3Uploader;

    @Autowired
    public PythonController(ChattingRoomService chattingRoomService, UserService userService, AnalysisResultService analysisResultService) { //, S3Uploader s3Uploader 추가
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
        this.analysisResultService = analysisResultService;
        //this.s3Uploader = s3Uploader;
    }

    // 처음 파일 업로드할 때 진행되는 기본 데이터 분석
    @GetMapping("/basic-python")
    public ResponseEntity<String> runBasicPythonAnalysis(@RequestParam(value = "crnum") int crnum) {
        try {
            // ChattingRoom을 찾아서 파일 경로를 가져온다
            ChattingRoom chattingRoom = chattingRoomService.findByCrNum(crnum);
            if (chattingRoom == null || chattingRoom.getCrNum() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ChattingRoom not found or invalid.");
            }
            String filePath = chattingRoom.getFilePath(); // 파일 경로 가져오기
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File path is not set.");
            }
            int headcount = chattingRoom.getHeadcount(); // headcount 가져오기

            // 파이썬 인터프리터의 절대 경로 설정
            String pythonInterpreterPath = "C:/Users/Master/AppData/Local/Programs/Python/Python312/python.exe";  // Python 3.12 인터프리터의 경로

            // 파이썬 스크립트의 절대 경로 설정
            String pythonScriptPath = "C:/Users/Master/TalkSsogi_Workspace/testpy.py";  // 실행할 Python 스크립트의 경로

            // 명령어 설정
            String command = String.format("%s %s %s %d", pythonInterpreterPath, pythonScriptPath, filePath, headcount);

            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);

            // 프로세스 시작
            Process process = processBuilder.start();

            // 프로세스의 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 프로세스 종료 대기
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Script execution failed.");
            }

            // 결과 처리
            String[] resultLines = result.toString().split("\n");
            if (resultLines.length < 2) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected script output.");
            }

            // 분석 결과를 저장
            String chatroomName = resultLines[0];
            List<String> memberNames = List.of(resultLines[1].split(","));

            // AnalysisResult 객체 생성
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setChattingRoom(chattingRoom);
            analysisResult.setChattingRoomNum(chattingRoom.getCrNum());
            analysisResult.setChatroomName(chatroomName);
            analysisResult.setActivityAnalysisImageUrl("");
            analysisResult.setWordCloudImageUrl("");
            analysisResult.setBasicActivityAnalysis(new HashMap<>());
            analysisResult.setBasicRankingResults(new HashMap<>());
            analysisResult.setSearchRankingResults(new HashMap<>());

            // 엔티티 상태를 디버깅
            logger.debug("AnalysisResult crnum: {}", analysisResult.getChattingRoomNum());

            // AnalysisResult 객체를 데이터베이스에 저장
            logger.info("여기여여여여여여겨고ㅑ 객체 파이썬컨트롤러에서 분석결과 데베 저장하기 직전 Saving AnalysisResult for ChattingRoom: {}", crnum);
            analysisResultService.save(analysisResult);
            logger.info("여기여여여여여여겨고ㅑ 객체 파이썬컨트롤러에서 분석결과 데베 저장하기 직후 Saving AnalysisResult for ChattingRoom: {}", crnum);
            logger.info("여기여여여여여여겨고ㅑ 객체 파이썬컨트롤러에서 저장되었는가 : ", analysisResult);



            return ResponseEntity.ok("Success");
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entity not found: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }


    // 5페이지에서 7,8로 갈 때 실행될 기본 제공 데이터
    @GetMapping("/commom-python") // 7,8의 기본 제공 데이터 생성
    public ResponseEntity<String> commonPythonScript(@RequestParam(value = "number") int number) {
        try {
            // 파이썬 스크립트를 실행할 명령어를 설정
            String command = "python script.py " + number;

            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" ")); //ProcessBuilder가 명령어와 인수를 올바르게 인식하게 나누기
            processBuilder.redirectErrorStream(true); // 에러 스크림을 풀력 스트림으로 합쳐서 읽울 수 있게 하기

            // 프로세스 시작
            Process process = processBuilder.start(); //command넣어서 만든 processBuilder 실행시작

            // 프로세스의 출력 읽기 (파이썬 결과인 출력 결과 읽기)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); // BufferedReader로 감싸서 한 줄씩 읽음
            StringBuilder result = new StringBuilder(); // 읽은 줄 넣기
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }


            //line을 가공하는 단계





            // 프로세스 종료 대기
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return ResponseEntity.ok("Success");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Script execution failed.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // 페이지9에서 결과 이미지 생성 및 전달
    @PostMapping("/personalActivityAnalysisImage")
    public ResponseEntity<String> getActivityAnalysisImage(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("searchWho") String searchWho,
            @RequestParam("resultsItem") String resultsItem,
            @RequestParam("crnum") Integer crnum) {
        try {

            // 파이썬 스크립트의 절대 경로 설정
            String pythonScriptPath = "C:/Users/Master/TalkSsogi_Workspace/testpy.py";

            // 파이썬 스크립트를 실행할 명령어를 설정
            String command = String.format("python %s %s %s %s %s %s",
                    pythonScriptPath, startDate, endDate, searchWho, resultsItem, crnum); // userId가 파일 이름

            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);

            // 프로세스 시작
            Process process = processBuilder.start();

            // 프로세스의 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 프로세스 종료 대기
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Script execution failed.");
            }

            // 결과 처리
            String[] resultLines = result.toString().split("\n");
            if (resultLines.length < 1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected script output.");
            }

            // 분석 결과 파일 (첫 번째 줄에 이미지 url 출력)
            String resultFilePath = resultLines[0];  // "http://192.168.45.232:8080/"+resultFilePath로 할 수 있도록 파이썬 만들기
            //File resultFile = new File(resultFilePath); //s3에 보낼 파일 객체

            // 파일을 AWS S3에 업로드하고 URL을 반환 (userId가 파일 이름)
            //String resultUrl = s3Uploader.upload(resultFile, crnum);
            //aws 연결 전까지 쓸 테스트 코드
            String resultUrl="http://192.168.45.232:8080/"+resultFilePath;


            return ResponseEntity.ok(resultUrl);
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}
