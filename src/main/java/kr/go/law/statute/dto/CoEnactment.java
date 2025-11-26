package kr.go.law.statute.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record CoEnactment(
    Integer no,
    Integer 공포번호,
    /** 공동부령구분 - content */
    String 공동부령구분,
    /** 공동부령구분 - 구분코드 */
    String 공동부령구분코드,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {

}
