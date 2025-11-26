package kr.go.law.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 검색 범위 (search 파라미터)
 * <p>
 * 판례, 행정심판례 등에서 공통으로 사용
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum SearchScope {

    /**
     * 1: 제목/명칭에서 검색 (판례명, 행정심판례명 등)
     */
    TITLE("1"),

    /**
     * 2: 본문에서 검색
     */
    CONTENT("2");

    private final String value;
}
