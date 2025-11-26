package kr.go.law.precedent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 판례 검색 범위 (search 파라미터)
 */
@Getter
@RequiredArgsConstructor
public enum SearchScope {

    /**
     * 판례명에서 검색
     */
    CASE_NAME("1"),

    /**
     * 본문에서 검색
     */
    CONTENT("2");

    private final String value;
}
