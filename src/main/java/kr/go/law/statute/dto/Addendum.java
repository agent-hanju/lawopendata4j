package kr.go.law.statute.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 부칙 단위 DTO */
public record Addendum(
    Long 부칙키,
    Integer 부칙공포일자,
    String 부칙내용,
    Integer 부칙공포번호,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {
}
