package com.altong.altong_backend.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            if (attribute == null) return null;
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
            throw new IllegalArgumentException("JSON 직렬화 실패", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return null;
            return objectMapper.readValue(dbData, new TypeReference<Object>() {});
        } catch (Exception e) {
            log.error("JSON 역직렬화 실패: {}", e.getMessage());
            throw new IllegalArgumentException("JSON 역직렬화 실패", e);
        }
    }
}