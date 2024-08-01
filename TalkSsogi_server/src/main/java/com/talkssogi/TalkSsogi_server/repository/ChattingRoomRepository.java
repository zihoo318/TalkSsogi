package com.talkssogi.TalkSsogi_server.repository;

import com.talkssogi.TalkSsogi_server.domain.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Integer> {
    ChattingRoom findByFilePath(String filePath);

    Optional<ChattingRoom> findByCrNum(Integer crNum);

    void deleteById(Integer crNum);

    Optional<ChattingRoom> findByActivityAnalysisImageUrl(String activityAnalysisImageUrl);

    Optional<ChattingRoom> findByWordCloudImageUrl(String wordCloudImageUrl);

    Optional<ChattingRoom> findByCrNumAndUser_UserId(Integer crNum, String userId);
}