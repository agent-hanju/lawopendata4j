package kr.go.law.statute.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record Ho(
    String 호번호,
    String 호가지번호,
    String 호내용,
    List<Mok> 목,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {
}
