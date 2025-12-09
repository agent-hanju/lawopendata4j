package kr.go.law.term.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법령용어 목록 조회 정렬 옵션 (lstrm)
 */
@Getter
@RequiredArgsConstructor
public enum TermSortOption {

    /** 법령용어명 오름차순 (기본값) */
    TERM_NAME_ASC("lasc"),

    /** 법령용어명 내림차순 */
    TERM_NAME_DESC("ldes"),

    /** 등록일자 오름차순 */
    REGISTRATION_DATE_ASC("rasc"),

    /** 등록일자 내림차순 */
    REGISTRATION_DATE_DESC("rdes");

    private final String value;
}
