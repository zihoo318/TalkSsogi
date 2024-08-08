package com.talkssogi.TalkSsogi_server.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

// Map<String, Map<String, String>> 타입의 데이터를 JSON 문자열로 변환하고,
// 다시 Map<String, Map<String, String>> 타입으로 변환하는 역할

@Converter
public class StringMapConverter implements AttributeConverter<Map<String, Map<String, String>>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Map<String, String>> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}"; // 빈 맵을 저장할 때 빈 JSON 객체
        }
        try {
            return objectMapper.writeValueAsString(attribute); // JSON 문자열로 직렬화
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert map to JSON string.", e);
        }
    }

    @Override
    public Map<String, Map<String, String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>(); // 빈 JSON을 빈 맵으로 변환
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert JSON string to map.", e);
        }
    }
}
