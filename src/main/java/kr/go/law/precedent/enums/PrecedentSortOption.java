package kr.go.law.precedent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 판례 목록 조회 정렬 옵션 (prec)
 */
@Getter
@RequiredArgsConstructor
public enum PrecedentSortOption {

    /** 사건명 오름차순 */
    CASE_NAME_ASC("lasc"),

    /** 사건명 내림차순 */
    CASE_NAME_DESC("ldes"),

    /** 선고일자 오름차순 */
    DECISION_DATE_ASC("dasc"),

    /** 선고일자 내림차순 (기본값) */
    DECISION_DATE_DESC("ddes"),

    /** 법원명 오름차순 */
    COURT_NAME_ASC("nasc"),

    /** 법원명 내림차순 */
    COURT_NAME_DESC("ndes");

    private final String value;
}
