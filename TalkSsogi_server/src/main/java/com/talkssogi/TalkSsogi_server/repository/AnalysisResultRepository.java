package com.talkssogi.TalkSsogi_server.repository;

import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, ChattingRoom>{
    AnalysisResult findByActivityAnalysisImageUrl(String activityAnalysisImageUrl);

    AnalysisResult findByWordCloudImageUrl(String wordCloudImageUrl);

    AnalysisResult findByChattingRoom_CrNumAndChattingRoom_User_UserId(Integer chatRoomId, String userId);

}

