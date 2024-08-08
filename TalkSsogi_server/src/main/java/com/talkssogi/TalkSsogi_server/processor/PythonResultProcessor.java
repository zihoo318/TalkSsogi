package com.talkssogi.TalkSsogi_server.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class PythonResultProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Map<String, String>> extractRankingResults(String jsonString) {
        // BOM 제거
        if (jsonString.startsWith("\uFEFF")) {
            jsonString = jsonString.substring(1);
        }

        try {
            // JSON 문자열을 Map으로 변환
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
