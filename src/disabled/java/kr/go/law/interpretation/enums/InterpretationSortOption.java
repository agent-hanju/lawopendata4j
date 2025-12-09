package kr.go.law.interpretation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법령해석례 목록 조회 정렬 옵션 (expc)
 */
@Getter
@RequiredArgsConstructor
public enum InterpretationSortOption {

    /** 법령해석례명 오름차순 (기본값) */
    NAME_ASC("lasc"),

    /** 법령해석례명 내림차순 */
    NAME_DESC("ldes"),

    /** 해석일자 오름차순 */
    INTERPRETATION_DATE_ASC("dasc"),

    /** 해석일자 내림차순 */
    INTERPRETATION_DATE_DESC("ddes"),

    /** 안건번호 오름차순 */
    AGENDA_NUMBER_ASC("nasc"),

    /** 안건번호 내림차순 */
    AGENDA_NUMBER_DESC("ndes");

    private final String value;
}
