package kr.go.law.statute.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 부서 DTO */
public record Department(
    String 부서키,
    Integer 소관부처코드,
    String 소관부처명,
    /** 부서명 (단일 문자열, 설명 포함 가능) */
    String 부서명,
    /** 부서연락처 (단일 문자열, 전화번호 범위 표기 예: "044-203-5124, 5128") */
    String 부서연락처,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> unexpectedFieldMap) {
}
