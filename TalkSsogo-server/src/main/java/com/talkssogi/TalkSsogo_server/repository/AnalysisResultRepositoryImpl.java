package com.talkssogi.TalkSsogo_server.repository;

import com.talkssogi.TalkSsogo_server.domain.AnalysisResult;

import java.util.ArrayList;
import java.util.List;

//임시 구현체
public class AnalysisResultRepositoryImpl implements AnalysisResultRepository {

    private List<AnalysisResult> analysisResultList = new ArrayList<>();

    @Override
    public AnalysisResult findByActivityAnalysisImageUrl(String activityAnalysisImageUrl) {
        for (AnalysisResult result : analysisResultList) {
            if (result.getActivityAnalysisImageUrl().equals(activityAnalysisImageUrl)) {
                return result;
            }
        }
        return null;
    }

    @Override
    public AnalysisResult findByWordCloudImageUrl(String wordCloudImageUrl) {
        for (AnalysisResult result : analysisResultList) {
            if (result.getWordCloudImageUrl().equals(wordCloudImageUrl)) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void save(AnalysisResult analysisResult) {
        analysisResultList.add(analysisResult);
    }

    @Override
    public void delete(AnalysisResult analysisResult) {
        analysisResultList.remove(analysisResult);
    }
}