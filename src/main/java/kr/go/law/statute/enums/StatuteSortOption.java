package kr.go.law.statute.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법령 목록 조회 정렬 옵션 (eflaw)
 */
@Getter
@RequiredArgsConstructor
public enum StatuteSortOption {

    /**
     * 법령명 오름차순 (가나다순)
     */
    LAW_NAME_ASC("lasc"),

    /**
     * 법령명 내림차순
     */
    LAW_NAME_DESC("ldes"),

    /**
     * 공포일자 오름차순 (오래된 순)
     */
    PROMULGATION_DATE_ASC("dasc"),

    /**
     * 공포일자 내림차순 (최신 순)
     */
    PROMULGATION_DATE_DESC("ddes"),

    /**
     * 공포번호 오름차순
     */
    PROMULGATION_NUMBER_ASC("nasc"),

    /**
     * 공포번호 내림차순
     */
    PROMULGATION_NUMBER_DESC("ndes"),

    /**
     * 시행일자 오름차순 (오래된 순)
     */
    EFFECTIVE_DATE_ASC("efasc"),

    /**
     * 시행일자 내림차순 (최신 순)
     */
    EFFECTIVE_DATE_DESC("efdes");

    private final String value;
}
