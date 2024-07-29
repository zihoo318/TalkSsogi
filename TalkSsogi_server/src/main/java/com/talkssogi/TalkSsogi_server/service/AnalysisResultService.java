package com.talkssogi.TalkSsogi_server.service;

import com.talkssogi.TalkSsogi_server.domain.AnalysisResult;
import com.talkssogi.TalkSsogi_server.repository.AnalysisResultRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Transactional
    public void save(AnalysisResult analysisResult) {
        analysisResultRepository.save(analysisResult);
    }

    @Transactional
    public void saveAnalysisResult(AnalysisResult analysisResult) {
        if (analysisResult.getChattingRoom() == null || analysisResult.getChattingRoom().getCrNum() == null) {
            throw new IllegalArgumentException("ChattingRoom or chattingRoomNum must not be null");
        }
        analysisResultRepository.save(analysisResult);
    }

    @Transactional
    public void addAnalysisResult(AnalysisResult result) {
        analysisResultRepository.save(result);
    }

    @Transactional
    public AnalysisResult findAnalysisResultByActivityAnalysisImageUrl(String activityAnalysisImageUrl) {
        return analysisResultRepository.findByActivityAnalysisImageUrl(activityAnalysisImageUrl);
    }

    @Transactional
    public AnalysisResult findAnalysisResultByWordCloudImageUrl(String wordCloudImageUrl) {
        return analysisResultRepository.findByWordCloudImageUrl(wordCloudImageUrl);
    }

    @Transactional
    public String findWordCloudImageUrlByChatRoomIdAndUserId(Integer chatRoomId, String userId) {
        AnalysisResult analysisResult = analysisResultRepository.findByChattingRoom_CrNumAndChattingRoom_User_UserId(chatRoomId, userId);
        if (analysisResult != null) {
            return analysisResult.getWordCloudImageUrl();
        }
        return null;
    }
}
