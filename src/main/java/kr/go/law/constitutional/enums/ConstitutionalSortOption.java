package kr.go.law.constitutional.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 헌재결정례 목록 조회 정렬 옵션 (detc)
 */
@Getter
@RequiredArgsConstructor
public enum ConstitutionalSortOption {

    /** 사건명 오름차순 (기본값) */
    CASE_NAME_ASC("lasc"),

    /** 사건명 내림차순 */
    CASE_NAME_DESC("ldes"),

    /** 선고일자 오름차순 */
    DECISION_DATE_ASC("dasc"),

    /** 선고일자 내림차순 */
    DECISION_DATE_DESC("ddes"),

    /** 사건번호 오름차순 */
    CASE_NUMBER_ASC("nasc"),

    /** 사건번호 내림차순 */
    CASE_NUMBER_DESC("ndes"),

    /** 종국일자 오름차순 */
    END_DATE_ASC("efasc"),

    /** 종국일자 내림차순 */
    END_DATE_DESC("efdes");

    private final String value;
}
