package com.secondtrade.market.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<String>> TYPE = new TypeReference<List<String>>() {};

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    try {
      if (attribute == null) return "[]";
      return MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return "[]";
    }
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.trim().isEmpty()) return new ArrayList<>();
      return MAPPER.readValue(dbData, TYPE);
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }
}

