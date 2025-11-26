package kr.go.law.term.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법령용어-일상용어 관계 유형 (trmRltCd 파라미터)
 */
@Getter
@RequiredArgsConstructor
public enum TermRelationType {

    /**
     * 동의어
     */
    SYNONYM("140301"),

    /**
     * 반의어
     */
    ANTONYM("140302"),

    /**
     * 상위어
     */
    HYPERNYM("140303"),

    /**
     * 하위어
     */
    HYPONYM("140304"),

    /**
     * 연관어
     */
    RELATED("140305");

    private final String code;
}
