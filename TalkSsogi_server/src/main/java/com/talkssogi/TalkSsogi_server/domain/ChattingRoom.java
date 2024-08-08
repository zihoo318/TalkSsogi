package com.talkssogi.TalkSsogi_server.domain;

import com.talkssogi.TalkSsogi_server.Converter.MapStringListConverter;
import com.talkssogi.TalkSsogi_server.Converter.MapStringMapConverter;
import com.talkssogi.TalkSsogi_server.Converter.StringListConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "chattingroom")
public class ChattingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cr_num")
    private Integer crNum;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "headcount")
    private int headcount;

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 컬럼 이름과 매핑

    private User user;

    @Column(name = "chatroom_name", length = 255)
    private String chatroomName = ""; // 빈 문자열로 초기화

    @Convert(converter = StringListConverter.class)
    @Column(name = "member_names") // Column name updated to match MySQL schema
    private List<String> memberNames = new ArrayList<>(); // 빈 리스트로 초기화

    @Convert(converter = StringListConverter.class)
    @Column(name = "basic_activity_analysis") // Column name updated to match MySQL schema
    private List<String> basicActivityAnalysis = new ArrayList<>(); // 빈 맵으로 초기화

    @Column(name = "activity_analysis_image_url", length = 255)
    private String activityAnalysisImageUrl = ""; // 빈 문자열로 초기화

    @Column(name = "word_cloud_image_url", length = 255)
    private String wordCloudImageUrl = ""; // 빈 문자열로 초기화

    @Convert(converter = MapStringMapConverter.class)
    @Column(name = "basic_ranking_results", columnDefinition = "LONGTEXT", length = 10000) // Column name updated to match MySQL schema
    private Map<String, Map<String, String>> basicRankingResults = new HashMap<>(); // 빈 맵으로 초기화

    @Convert(converter = MapStringMapConverter.class)
    @Column(name = "search_ranking_results") // Column name updated to match MySQL schema
    private Map<String, Map<String, String>> searchRankingResults = new HashMap<>(); // 빈 맵으로 초기화

    @Column(name = "caller_prediction") // 발신자 예측 결과값 저장
    private String callerPrediction;


    // Getters and setters
    public Integer getCrNum() {
        return crNum;
    }

    public void setCrNum(Integer crNum) {
        this.crNum = crNum;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getHeadcount() {
        return headcount;
    }

    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public void setChatroomName(String chatroomName) {
        this.chatroomName = chatroomName != null ? chatroomName : "";
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames != null ? memberNames : new ArrayList<>();
    }

    public List<String> getBasicActivityAnalysis() {
        return basicActivityAnalysis;
    }

    public void setBasicActivityAnalysis(List<String> basicActivityAnalysis) {
        this.basicActivityAnalysis = basicActivityAnalysis != null ? basicActivityAnalysis : new ArrayList<>();
    }

    public String getActivityAnalysisImageUrl() {
        return activityAnalysisImageUrl;
    }

    public void setActivityAnalysisImageUrl(String activityAnalysisImageUrl) {
        this.activityAnalysisImageUrl = activityAnalysisImageUrl != null ? activityAnalysisImageUrl : "";
    }

    public String getWordCloudImageUrl() {
        return wordCloudImageUrl;
    }

    public void setWordCloudImageUrl(String wordCloudImageUrl) {
        this.wordCloudImageUrl = wordCloudImageUrl != null ? wordCloudImageUrl : "";
    }

    public Map<String, Map<String, String>> getBasicRankingResults() {
        return basicRankingResults;
    }

    public void setBasicRankingResults(Map<String, Map<String, String>> basicRankingResults) {
        this.basicRankingResults = basicRankingResults;
    }

    public Map<String, Map<String, String>> getSearchRankingResults() {
        return searchRankingResults;
    }

    public void setSearchRankingResults(Map<String, Map<String, String>> searchRankingResults) {
        this.searchRankingResults = searchRankingResults != null ? searchRankingResults : new HashMap<>();
    }


    public String getCallerPrediction() {
        return callerPrediction;
    }

    public void setCallerPrediction(String callerPrediction) {
        this.callerPrediction = callerPrediction;
    }

}