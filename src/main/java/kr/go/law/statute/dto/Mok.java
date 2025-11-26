package kr.go.law.statute.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record Mok(
    String 목번호,
    String 목가지번호,
    String 목내용,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {

}
