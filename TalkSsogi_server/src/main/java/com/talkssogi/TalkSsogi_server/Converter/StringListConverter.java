package com.talkssogi.TalkSsogi_server.Converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

//List<String> 타입의 데이터를 데이터베이스에 저장할 수 있도록 문자열로 변환하고, 다시 List<String> 타입으로 변환하는 역할


@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    //리스트의 모든 문자열을 콤마(,)로 연결하여 하나의 문자열로 변환
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute != null ? String.join(",", attribute) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return dbData != null ? Arrays.asList(dbData.split(",")) : List.of();
    }
}
