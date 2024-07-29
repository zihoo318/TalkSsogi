package com.talkssogi.TalkSsogi_server.domain;

import com.talkssogi.TalkSsogi_server.Converter.MapStringListConverter;
import com.talkssogi.TalkSsogi_server.Converter.StringListConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "analysisresult")
public class AnalysisResult {

    @Id
    @Column(name = "cr_num") // 스키마와 일치하도록 컬럼 이름 설정
    private Integer chattingRoomNum=0;

    @OneToOne
    @MapsId // 외래 키와 기본 키가 같은 값을 가지도록 설정
    @JoinColumn(name = "cr_num") // AnalysisResult 테이블에서 외래 키로 사용될 컬럼의 이름
    private ChattingRoom chattingRoom; // ChattingRoom 엔티티와의 일대일 관계

    @Column(name = "chatroom_name", length = 255)
    private String chatroomName = ""; // 빈 문자열로 초기화

    @Convert(converter = StringListConverter.class)
    @Column(name = "member_names") // Column name updated to match MySQL schema
    private List<String> memberNames = new ArrayList<>(); // 빈 리스트로 초기화

    @Convert(converter = MapStringListConverter.class)
    @Column(name = "basic_activity_analysis") // Column name updated to match MySQL schema
    private Map<String, List<String>> basicActivityAnalysis = new HashMap<>(); // 빈 맵으로 초기화

    @Column(name = "activity_analysis_image_url", length = 255)
    private String activityAnalysisImageUrl = ""; // 빈 문자열로 초기화

    @Column(name = "word_cloud_image_url", length = 255)
    private String wordCloudImageUrl = ""; // 빈 문자열로 초기화

    @Convert(converter = MapStringListConverter.class)
    @Column(name = "basic_ranking_results") // Column name updated to match MySQL schema
    private Map<String, List<String>> basicRankingResults = new HashMap<>(); // 빈 맵으로 초기화

    @Convert(converter = MapStringListConverter.class)
    @Column(name = "search_ranking_results") // Column name updated to match MySQL schema
    private Map<String, List<String>> searchRankingResults = new HashMap<>(); // 빈 맵으로 초기화

    // 기본 생성자
    public AnalysisResult() {
    }

    // 명시적 생성자
    public AnalysisResult(ChattingRoom chattingRoom, String chatroomName, List<String> memberNames) {
        this.chattingRoom = chattingRoom;
        this.chattingRoomNum = chattingRoom != null ? chattingRoom.getCrNum() : null; // Set chattingRoomNum from ChattingRoom
        this.chatroomName = chatroomName != null ? chatroomName : ""; // null 체크 후 빈 문자열로 초기화
        this.memberNames = memberNames != null ? memberNames : new ArrayList<>(); // null 체크 후 빈 리스트로 초기화
    }
    // Getter 및 Setter
    public Integer getChattingRoomNum() {
        return chattingRoomNum;
    }

    public void setChattingRoomNum(Integer chattingRoomNum) {
        this.chattingRoomNum = chattingRoomNum;
    }

    public ChattingRoom getChattingRoom() {
        return chattingRoom;
    }

    public void setChattingRoom(ChattingRoom chattingRoom) {
        this.chattingRoom = chattingRoom;
        this.chattingRoomNum = (chattingRoom != null && chattingRoom.getCrNum() != null) ? chattingRoom.getCrNum() : null;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public void setChatroomName(String chatroomName) {
        this.chatroomName = chatroomName != null ? chatroomName : ""; // null 체크 후 빈 문자열로 설정
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames != null ? memberNames : new ArrayList<>(); // null 체크 후 빈 리스트로 설정
    }

    public Map<String, List<String>> getBasicActivityAnalysis() {
        return basicActivityAnalysis;
    }

    public void setBasicActivityAnalysis(Map<String, List<String>> basicActivityAnalysis) {
        this.basicActivityAnalysis = basicActivityAnalysis != null ? basicActivityAnalysis : new HashMap<>(); // null 체크 후 빈 맵으로 설정
    }

    public String getActivityAnalysisImageUrl() {
        return activityAnalysisImageUrl;
    }

    public void setActivityAnalysisImageUrl(String activityAnalysisImageUrl) {
        this.activityAnalysisImageUrl = activityAnalysisImageUrl != null ? activityAnalysisImageUrl : ""; // null 체크 후 빈 문자열로 설정
    }

    public String getWordCloudImageUrl() {
        return wordCloudImageUrl;
    }

    public void setWordCloudImageUrl(String wordCloudImageUrl) {
        this.wordCloudImageUrl = wordCloudImageUrl != null ? wordCloudImageUrl : ""; // null 체크 후 빈 문자열로 설정
    }

    public Map<String, List<String>> getBasicRankingResults() {
        return basicRankingResults;
    }

    public void setBasicRankingResults(Map<String, List<String>> basicRankingResults) {
        this.basicRankingResults = basicRankingResults != null ? basicRankingResults : new HashMap<>(); // null 체크 후 빈 맵으로 설정
    }

    public Map<String, List<String>> getSearchRankingResults() {
        return searchRankingResults;
    }

    public void setSearchRankingResults(Map<String, List<String>> searchRankingResults) {
        this.searchRankingResults = searchRankingResults != null ? searchRankingResults : new HashMap<>(); // null 체크 후 빈 맵으로 설정
    }

}
