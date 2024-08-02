package com.talkssogi.TalkSsogi_server.controller;
//가을 추가
import com.talkssogi.TalkSsogi_server.domain.PredictionResponse;
import com.talkssogi.TalkSsogi_server.domain.PredictionRequest;
//가을 추가 끝
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
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
import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class PythonController {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class);

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;
    //private final S3Uploader s3Uploader;

    @Autowired
    public PythonController(ChattingRoomService chattingRoomService, UserService userService) { //, S3Uploader s3Uploader 추가
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
        //this.s3Uploader = s3Uploader;
    }

    // 처음 파일 업로드할 때 진행되는 기본 데이터 분석
    @GetMapping(value = "/basic-python", produces="application/json; charset=utf8")
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
            String pythonInterpreterPath = "C:/Users/apf_temp_admin/AppData/Local/Microsoft/WindowsApps/python.exe";  // Python 3.12 인터프리터의 경로

            // 파이썬 스크립트의 절대 경로 설정
            String pythonScriptPath = "C:/Users/apf_temp_admin/TalkSsogi_Workspace/basic-python.py";  // 실행할 Python 스크립트의 경로

            // 명령어 설정
            String command = String.format("%s %s %s", pythonInterpreterPath, pythonScriptPath, filePath);
            logger.info("제대로 파이썬 명령어를 사용하고 있는가???? Executing command: " + command);


            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");

            // 프로세스 시작
            Process process = processBuilder.start();

            // 프로세스의 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream())); // 표준 오류 스트림 읽기
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 표준 오류 스트림 읽기
            StringBuilder errorResult = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorResult.append(line).append("\n");
            }

            // 프로세스 종료 대기
            int exitCode = process.waitFor();
            logger.error("파이썬 에러 메세지!!! Python script error output: " + errorResult.toString());

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
            logger.info("제대로 파이썬 결과를 받았는가???? chatroomName: " + chatroomName);
            logger.info("제대로 파이썬 결과를 받았는가???? memberNames: " + memberNames);

            // ChattingRoom 업데이트
            chattingRoom.setChatroomName(chatroomName);
            chattingRoom.setMemberNames(memberNames);
            chattingRoomService.save(chattingRoom);

            logger.info("여기여여여여여여겨고ㅑ 분석한 결과부터 이상하게 저장되었는가 : {}", chattingRoom.getChatroomName());

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
            String pythonScriptPath = "C:/Users/apf_temp_admin/TalkSsogi_Workspace/testpy.py";

            // 파이썬 스크립트를 실행할 명령어를 설정
            String command = String.format("python %s %s %s %s %s %s",
                    pythonScriptPath, startDate, endDate, searchWho, resultsItem, crnum);

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

            // 분석 결과 파일 (첫 번째 줄에 이미지 URL 출력)
            String resultFilePath = resultLines[0];
            String resultUrl = "http://192.168.219.106:8080/" + resultFilePath;

            return ResponseEntity.ok(resultUrl);
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
     //가을 추가
    @PostMapping("/sender")
    public ResponseEntity<PredictionResponse> predictSender(@RequestBody PredictionRequest request) {
        try {
            String query = request.getQuery();

            // 파이썬 스크립트의 절대 경로 설정
            String pythonScriptPath = "C:/Users/apf_temp_admin/TalkSsogi_Workspace/script.py"; // 경로 설정

            // 명령어 설정
            String command = String.format("python %s %s", pythonScriptPath, query);
            logger.info("Executing Python script with command: " + command);

            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PredictionResponse("Error executing script"));
            }

            // 분석 결과 반환
            String sender = result.toString().trim(); // 결과에서 공백 제거
            return ResponseEntity.ok(new PredictionResponse(sender));

        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PredictionResponse("Unexpected error"));
        }
    }

}