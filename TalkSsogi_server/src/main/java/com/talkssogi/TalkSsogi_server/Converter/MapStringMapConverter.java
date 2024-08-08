package com.talkssogi.TalkSsogi_server.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.Map;

//Map<String, List<String>> 타입의 데이터를 데이터베이스에 저장할 수 있도록 JSON 문자열로 변환하고,
// 다시 Map<String, List<String>> 타입으로 변환하는 역할

@Converter
public class MapStringMapConverter implements AttributeConverter<Map<String, Map<String, String>>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Map<String, String>> attribute) {
        // ObjectMapper를 사용하여 Map<String, List<String>> 객체를 JSON 문자열로 직렬화
        // ex) attribute = Map.of("key1", List.of("value1", "value2"), "key2", List.of("value3"))
        // {"key1":["value1","value2"],"key2":["value3"]}라는 JSON 문자열을 반환합니다.
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert map to JSON string.", e);
        }
    }

    @Override
    public Map<String, Map<String, String>> convertToEntityAttribute(String dbData) {
        // ObjectMapper를 사용하여 JSON 문자열을 Map<String, List<String>> 객체로 역직렬화
        // ex) dbData = {"key1":["value1","value2"],"key2":["value3"]}라는 JSON 문자열
        //  Map.of("key1", List.of("value1", "value2"), "key2", List.of("value3"))라는 Map<String, List<String>> 객체를 반환
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert JSON string to map.", e);
        }
    }

}

