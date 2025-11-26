package kr.go.law.statute.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법령 상태 구분 (nw 파라미터)
 * <p>
 * 복수 선택 시 콤마로 구분 (예: "1,3" = 연혁 + 현행)
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum StatuteStatus {

    /**
     * 연혁 법령 (폐지/개정된 과거 법령)
     */
    HISTORY("1"),

    /**
     * 시행예정 법령 (공포되었으나 아직 시행되지 않은 법령)
     */
    SCHEDULED("2"),

    /**
     * 현행 법령 (현재 시행 중인 법령)
     */
    CURRENT("3");

    private final String value;
}
