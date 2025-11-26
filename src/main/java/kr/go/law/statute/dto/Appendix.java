package kr.go.law.statute.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 별표 단위 DTO */
public record Appendix(
    String 별표키,
    String 별표구분,
    Integer 별표번호,
    Integer 별표가지번호,
    String 별표제목문자열,
    String 별표내용,
    String 별표서식파일링크,
    String 별표서식PDF파일링크,
    Integer 별표시행일자,
    String 별표제목,
    String 별표PDF파일명,
    String 별표HWP파일명,
    List<String> 별표이미지파일명,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {
}
