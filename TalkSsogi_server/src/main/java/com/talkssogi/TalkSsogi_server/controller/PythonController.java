package com.talkssogi.TalkSsogi_server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkssogi.TalkSsogi_server.StorageUploader.S3DownLoader;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import com.talkssogi.TalkSsogi_server.processor.PythonResultProcessor;
import com.talkssogi.TalkSsogi_server.service.ChattingRoomService;
import com.talkssogi.TalkSsogi_server.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/analysis")
public class PythonController {

    private static final Logger logger = LoggerFactory.getLogger(PythonController.class);

    private final ChattingRoomService chattingRoomService;
    private final UserService userService;
    private final S3DownLoader s3DownLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PythonResultProcessor pythonResultProcessor;  // 추가

    // Python 인터프리터와 스크립트의 경로를 상수로 선언
    private static final String PYTHON_FILE_PATH = "C:/Talkssogi_Workspace/TalkSsogi"; // workspace 밑에 저장된 파이썬 파일 경로
    private static final String PYTHON_INTERPRETER_PATH = "C:/Users/LG/AppData/Local/Programs/Python/Python312/python.exe";
    private static final String PYTHON_SCRIPT_basic_PATH = "C:/Talkssogi_Workspace/TalkSsogi/basic-python.py";
    private static final String PYTHON_SCRIPT_PAGE9_PATH = "C:/Talkssogi_Workspace/TalkSsogi/page9python.py";
    private static final String PYTHON_SCRIPT_PAGE8_PATH = "C:/Talkssogi_Workspace/TalkSsogi/page8python.py";
    private static final String PYTHON_SCRIPT_PAGE6_PATH = "C:/Talkssogi_Workspace/TalkSsogi/page6python.py";
    private static final String PYTHON_BASIC_RESULT_FILE_PATH = "C:/Talkssogi_Workspace/TalkSsogi/"; // basic-python후에 생길 분석을 위한 파일들을 찾기 위한 경로


    @Autowired
    public PythonController(ChattingRoomService chattingRoomService, UserService userService, PythonResultProcessor pythonResultProcessor, S3DownLoader s3DownLoader) {
        this.chattingRoomService = chattingRoomService;
        this.userService = userService;
        this.s3DownLoader = s3DownLoader;
        this.pythonResultProcessor = pythonResultProcessor;  // 추가
    }

    // 기본분석으로 생긴 텍스트 파일들 s3에 업로드하는 함수(api호출해야해서 분리)
    private void uploadFilesToS3(String[] resultLines, int headcount, int crnum) throws IOException {
        logger.info("Starting uploadFilesToS3 with resultLines length: " + resultLines.length);

        List<File> filesToUpload = new ArrayList<>();

        // group 파일 경로 추가
        filesToUpload.add(new File(resultLines[3]));
        filesToUpload.add(new File(resultLines[4]));
        filesToUpload.add(new File(resultLines[5]));

        // 사용자별 파일 경로 추가
        int startIndex = 6;
        int endIndex = startIndex + headcount * 3;
        for (int i = startIndex; i < endIndex; i++) {
            File file = new File(resultLines[i]);
            filesToUpload.add(file);
            logger.info("File to upload: " + file.getAbsolutePath());
        }

        // S3에 파일 업로드(파일 타입을 가지고 프리픽스(=폴더 같은 개념의 파일이름 앞의 경로이름) 설정함)
        for (File file : filesToUpload) {
            try (InputStream fileStream = new FileInputStream(file)) {
                String contentType = Files.probeContentType(file.toPath());
                String prefix;
                if (contentType.startsWith("text/")) {
                    prefix = "text-files/";
                } else if (contentType.startsWith("image/")) {
                    prefix = "image-files/";
                } else {
                    prefix = "other-files/";
                }
                String key = prefix + crnum + "_" + file.getName();
                s3DownLoader.uploadFile(key, fileStream, contentType); // 프리픽스 포함하여 업로드
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 업로드 후 파일 삭제
        for (File file : filesToUpload) {
            if (file.delete()) {
                logger.info("Successfully deleted file: " + file.getName());
            } else {
                logger.warn("Failed to delete file: " + file.getName());
            }
        }
    }

    // 처음 파일 업로드할 때 진행되는 기본 데이터 분석
    @GetMapping(value = "/basic-python", produces = "application/json; charset=utf8")
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

            // 명령어 설정
            String command = String.format("%s %s %s", PYTHON_INTERPRETER_PATH, PYTHON_SCRIPT_basic_PATH, filePath);
            logger.info("제대로 파이썬 명령어를 사용하고 있는가???? Executing command: " + command);


            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");

            // 프로세스 시작
            Process process = processBuilder.start();

            // 프로세스의 출력 읽기
            // InputStreamReader에 UTF-8 인코딩을 명시적으로 설정
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
            // 디버깅 로그 추가
            logger.info("Result lines length: " + resultLines.length);
            for (int i = 0; i < resultLines.length; i++) {
                logger.info("Result line " + i + ": " + resultLines[i]);
            }

            if (resultLines.length < 2) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected script output.");
            }


            // JSON 파일에서 결과를 읽어오기
            String jsonFilePath = PYTHON_BASIC_RESULT_FILE_PATH + "/ranking_results.json";
            File jsonFile = new File(jsonFilePath); // JSON 파일 경로 설정
            String jsonString = readFileToString(jsonFile);  // 파일을 문자열로 읽기

            // JSON 문자열을 Map으로 변환
            Map<String, Map<String, String>> rankingResultsMap = pythonResultProcessor.extractRankingResults(jsonString);

            // 분석 결과를 ChattingRoom 엔티티에 저장
            chattingRoom.setBasicRankingResults(rankingResultsMap);
            chattingRoomService.save(chattingRoom);

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

            uploadFilesToS3(resultLines, headcount, chattingRoom.getCrNum());

            return ResponseEntity.ok("Success");
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entity not found: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    // 페이지6 워드클라우드 이미지 url전달
    @GetMapping("/wordCloudImageUrl/{crnum}/{userId}")
    public ResponseEntity<String> getWordCloudImageUrl(@PathVariable("crnum") Integer crnum, @PathVariable("userId") String userId) {
        try {
            // userId 값 처리
            String searchWho = "전체".equals(userId) ? "group" : userId;
            Integer searchWhoIndex = chattingRoomService.getMemberIndexByName(crnum,searchWho);

            // file_path 설정
            // S3에서 가져오기
            String filePath = s3DownLoader.getFileUrl(String.format("text-files/"+crnum + (searchWho.equals("group") ? "_group.txt" : "_" + searchWhoIndex + "_personal.txt")));
            //String filePath = PYTHON_newimage_PATH + crnum + (searchWho.equals("group") ? "_group.txt" : "_" + searchWho + "_personal.txt"); // 테스트용

            // 명령어 설정
            String command = String.format(
                    "%s %s %s",
                    PYTHON_INTERPRETER_PATH, PYTHON_SCRIPT_PAGE6_PATH, filePath
            );
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
            logger.error("Python script error output: " + errorResult.toString());

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Script execution failed.");
            }

            // 결과 처리
            String imgName = result.toString().trim();
            if (imgName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected script output.");
            }

            // 분석 결과 파일 경로 추출
            String resultFilePath = imgName;  // 결과 파일 경로는 Python 스크립트에서 생성된 파일 경로
            File resultFile = new File(PYTHON_BASIC_RESULT_FILE_PATH + resultFilePath);

            // 파일을 S3로 업로드
            try (InputStream fileStream = new FileInputStream(resultFile)) {
                String s3Key = "image-files/" + resultFile.getName();
                String contentType = Files.probeContentType(resultFile.toPath());
                s3DownLoader.uploadFile(s3Key, fileStream, contentType);

                // S3 URL 생성
                String wordCloudImageUrl = s3DownLoader.getFileUrl(s3Key);
                logger.info("Generated S3 URL: " + wordCloudImageUrl);

                // 로컬 파일 삭제
                if (resultFile.delete()) {
                    logger.info("Successfully deleted local file: " + resultFile.getName());
                } else {
                    logger.warn("Failed to delete local file: " + resultFile.getName());
                }

                return new ResponseEntity<>(wordCloudImageUrl, HttpStatus.OK);
            } catch (IOException e) {
                logger.error("워드클라우드 이미지 s3에 업로드 실패 IO Exception occurred: " + e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("워드클라우드 이미지 s3에 업로드 실패");
            }

        } catch (IOException e) {
            logger.error("IO Exception occurred: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An IO exception occurred.");
        } catch (InterruptedException e) {
            logger.error("Process interrupted: " + e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Process was interrupted.");
        }
    }

    private String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }


    // 미리 db에 저장할 8에 필요한 데이터 만들기
    @GetMapping("/basicActivityAnalysis")
    public ResponseEntity<List<String>> commonPythonScript(@RequestParam(value = "crnum") int crnum) {
        String dailyFilePath = s3DownLoader.getFileUrl(String.format("%d_group_daily_message_count.txt", crnum));
        String hourlyFilePath = s3DownLoader.getFileUrl(String.format("%d_group_daily_hourly_message_count.txt", crnum));
        try {
            logger.info("for page6 basic ActivityAnalysis 기본 제공 분석 시작!!!!!!!!!!!!!");

            // S3 파일 URL 가져오기
            String dailyFileUrl = s3DownLoader.getFileUrl(String.format("%d_group_daily_message_count.txt", crnum));
            String hourlyFileUrl = s3DownLoader.getFileUrl(String.format("%d_group_daily_hourly_message_count.txt", crnum));

            // URL 객체 생성
            URL dailyFileURL = new URL(dailyFileUrl);
            URL hourlyFileURL = new URL(hourlyFileUrl);

            //String dailyFilePath = PYTHON_newimage_PATH + String.format("%d_group_daily_message_count.txt", crnum);
            //String hourlyFilePath = PYTHON_newimage_PATH + String.format("%d_group_daily_hourly_message_count.txt", crnum);

            // URL 인코딩
//            String encodedDailyFilePath = URLEncoder.encode(dailyFilePath, "UTF-8");
//            String encodedHourlyFilePath = URLEncoder.encode(hourlyFilePath, "UTF-8");

            // 명령어 설정
            //String command = String.format("%s %s %s %s", PYTHON_INTERPRETER_PATH, PYTHON_SCRIPT_PAGE8_PATH, dailyFilePath, hourlyFilePath);
            String command = String.format("%s %s %s %s", PYTHON_INTERPRETER_PATH, PYTHON_SCRIPT_PAGE8_PATH, dailyFileURL, hourlyFileURL);
            logger.info("제대로 파이썬 명령어를 사용하고 있는가???? Executing command: " + command);

            // S3에서 파일 다운로드
            //String dailyFilePath = s3DownLoader.getFileUrl(String.format("%d/group_daily_message_count.txt", crnum));
            //String hourlyFilePath = s3DownLoader.getFileUrl(String.format("%d/group_daily_hourly_message_count.txt", crnum));
            //String dailyFilePath = PYTHON_newimage_PATH + String.format("%d_group_daily_message_count.txt", crnum);
            //String hourlyFilePath = PYTHON_newimage_PATH + String.format("%d_group_daily_hourly_message_count.txt", crnum);


            // ProcessBuilder를 사용하여 프로세스 생성
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);

            // 프로세스 시작
            Process process = processBuilder.start();

            // 프로세스의 출력 읽기
            // InputStreamReader에 UTF-8 인코딩을 명시적으로 설정
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            List<String> resultLines = new ArrayList<>();
            String outputLine;
            while ((outputLine = reader.readLine()) != null) {
                resultLines.add(outputLine);
            }

            // 프로세스의 오류 출력 읽기
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine); // 오류 확인
            }

            /// 프로세스 종료 대기
            int exitCode = process.waitFor();
            logger.info("Process exited with code: " + exitCode);


//            // 다운로드한 파일 삭제
//            Files.deleteIfExists(Paths.get(dailyFilePath.toString()));
//            Files.deleteIfExists(Paths.get(hourlyFilePath.toString()));

            if (exitCode == 0) {
                // 기본 값 설정
                // 최대 날짜 및 메시지 수를 저장할 변수 초기화
                String maxDateAndCount = "잠시후에 다시 시도 해주세요.";
                // 메시지가 없는 날짜 저장할 변수 초기화
                String zeroCountDates = "잠시후에 다시 시도 해주세요.";
                // 시간대 저장할 변수 초기화
                String maxTimeSlotAndCount = "잠시후에 다시 시도 해주세요.";

                // 최대 날짜 및 메시지 수와 최대 시간대 및 메시지 수를 찾았는지 여부를 추적하는 변수 초기화
                boolean foundMaxDateAndCount = false;
                boolean foundMaxTimeSlotAndCount = false;

                // resultLines의 모든 줄을 순회하며 적절한 변수에 값을 할당
                List<String> zeroCountList = new ArrayList<>();
                for (String resultLine : resultLines) {
                    if (resultLine.matches("\\d{4}-\\d{2}-\\d{2}\\(\\d+건\\)")) {
                        maxDateAndCount = resultLine;
                        foundMaxDateAndCount = true;
                    } else if (resultLine.matches(".*\\(\\d+건\\)")) {
                        if (foundMaxDateAndCount) {
                            zeroCountList.add(resultLine);
                        } else {
                            maxTimeSlotAndCount = resultLine;
                            foundMaxTimeSlotAndCount = true;
                        }
                    } else {
                        zeroCountList.add(resultLine);
                    }
                }

                // zeroCountList에서 마지막에 추가된 줄바꿈 문자를 제거
                StringBuilder zeroCountDatesBuilder = new StringBuilder();
                if (!zeroCountList.isEmpty()) {
                    int size = zeroCountList.size();
                    if (size > 5) { // 대화를 안한 날이 5일이 넘어가는 경우
                        for (int i = 0; i < 5; i++) {
                            zeroCountDatesBuilder.append(zeroCountList.get(i)).append("\n");
                        }
                        zeroCountDatesBuilder.append("외 ").append(size - 5).append("일");
                    } else {
                        for (String date : zeroCountList) {
                            zeroCountDatesBuilder.append(date).append("\n");
                        }
                    }
                }

                zeroCountDates = zeroCountDatesBuilder.toString().trim();

                // 리스트 생성 및 값 추가
                List<String> outputList = new ArrayList<>();
                outputList.add(maxDateAndCount);
                outputList.add(zeroCountDates);
                outputList.add(maxTimeSlotAndCount);

                // 데이터베이스에서 ChattingRoom 가져오기
                ChattingRoom chattingRoom = chattingRoomService.findByCrNum(crnum);
                if (chattingRoom != null) {
                    chattingRoom.setBasicActivityAnalysis(outputList);
                    chattingRoomService.save(chattingRoom);
                    return ResponseEntity.ok(outputList);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            List.of("ChattingRoom not found")
                    );
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        List.of("Script execution failed with exit code: " + exitCode)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    List.of("Error: " + e.getMessage())
            );
        }
    }


    // 날짜 형식을 바꿔주는 메서드
    private String convertDateFormat(String dateStr) throws ParseException {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = originalFormat.parse(dateStr);
        return targetFormat.format(date);
    }

    // 페이지9에서 결과 이미지 생성 및 전달
    @PostMapping(value = "/personalActivityAnalysisImage", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> getActivityAnalysisImage(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("searchWho") String searchWho,
            @RequestParam("resultsItem") String resultsItem,
            @RequestParam("crnum") Integer crnum) {
        try {
            // 날짜 형식 변환
            String startDateFormatted = convertDateFormat(startDate);
            String endDateFormatted = convertDateFormat(endDate);

            // SearchWho 값 처리
            String SearchWho = "전체".equals(searchWho) ? "group" : searchWho;
            Integer searchWhoIndex = "group".equals(SearchWho) ? null : chattingRoomService.getMemberIndexByName(crnum,searchWho);

            // URL 디코딩
            resultsItem = URLDecoder.decode(resultsItem, StandardCharsets.UTF_8.name());
            // 디코딩 후 로그 확인
            logger.info("제대로 resultsItem이 넘어왔는가 resultsItem: " + resultsItem);

            // crnum을 가지고 s3에서 파일 찾고 가져오는 과정
            String filePath = null; // 검색할 파일
            // resultsItem에 따라 파일 경로를 설정
            switch (resultsItem) {
                case "보낸 메시지 수 그래프":
                    filePath = "group".equals(SearchWho) ?
                            s3DownLoader.getFileUrl(String.format("%d_group_daily_message_count.txt", crnum)) :
                            s3DownLoader.getFileUrl(String.format("%d_%d_daily_message_count.txt", crnum, searchWhoIndex));
                    break;
                case "활발한 시간대 그래프":
                    filePath = "group".equals(SearchWho) ?
                            s3DownLoader.getFileUrl(String.format("%d_group_daily_hourly_message_count.txt", crnum)) :
                            s3DownLoader.getFileUrl(String.format("%d_%d_daily_hourly_message_count.txt", crnum, searchWhoIndex));
                    break;
                case "대화를 보내지 않은 날짜":
                    filePath = "group".equals(SearchWho) ?
                            s3DownLoader.getFileUrl(String.format("%d_group_daily_message_count.txt", crnum)) :
                            s3DownLoader.getFileUrl(String.format("%d_%d_daily_message_count.txt", crnum, searchWhoIndex));
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("resultsItem이 이상함 Unknown resultsItem: " + resultsItem);
            }

            // 명령어 설정
            String command = String.format(
                    "%s %s \"%s\" \"%s\" \"%s\" \"%s\" %s %d",
                    PYTHON_INTERPRETER_PATH, PYTHON_SCRIPT_PAGE9_PATH, startDateFormatted, endDateFormatted, SearchWho, resultsItem, filePath, crnum
            );
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
            logger.error("Python script error output: " + errorResult.toString());

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Script execution failed.");
            }

            // 결과 처리
            String imgName = result.toString().trim();
            if (imgName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected script output.");
            }

            // 분석 결과 파일 경로 추출
            File localResultFile = new File(PYTHON_BASIC_RESULT_FILE_PATH + imgName);

            // S3에 파일 업로드
            try (InputStream fileStream = new FileInputStream(localResultFile)) {
                String key = "image-files/" + imgName;
                String contentType = Files.probeContentType(localResultFile.toPath());
                s3DownLoader.uploadFile(key, fileStream, contentType);
                // S3 URL 생성
                String resultUrl = s3DownLoader.getFileUrl(key);
                logger.info("Generated S3 URL: " + resultUrl);

                // 로컬 파일 삭제
                if (localResultFile.exists() && !localResultFile.delete()) {
                    logger.warn("Failed to delete local file: " + localResultFile.getAbsolutePath());
                }
                return ResponseEntity.ok(resultUrl);
            }
        } catch (IOException e) {
            logger.error("IO Exception occurred: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An IO exception occurred.");
        } catch (InterruptedException e) {
            logger.error("Process interrupted: " + e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Process was interrupted.");
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }


    public ResponseEntity<String> getCallerPrediction(@RequestParam int crnum, String keyword) {
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

            String predictPythonPath = PYTHON_FILE_PATH + "/caller_prediction.py";

            // 명령어 설정
            String command = String.format("%s %s %s %s", PYTHON_INTERPRETER_PATH, predictPythonPath, filePath, keyword);
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
            String callerPrediction = result.toString().trim();

            logger.info(keyword + "의Caller Prediction 결과: " + callerPrediction);


            // ChattingRoom에 callerPrediction 저장
            chattingRoom.setCallerPrediction(callerPrediction);
            chattingRoomService.save(chattingRoom);

            return ResponseEntity.ok("Success");
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entity not found: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

}