package kr.go.law.statute.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record Hang(
    String 항번호,
    String 항가지번호,
    String 항내용,
    List<Ho> 호,
    String 항제개정유형,
    String 항제개정일자,
    String 항제개정일자문자열,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {
}
