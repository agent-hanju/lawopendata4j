package kr.go.law.precedent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 법원 종류 코드 (org 파라미터)
 */
@Getter
@RequiredArgsConstructor
public enum CourtType {

    /**
     * 대법원
     */
    SUPREME_COURT("400201", "대법원"),

    /**
     * 하급심 (고등법원, 지방법원 등)
     */
    LOWER_COURT("400202", "하급심");

    private final String code;
    private final String description;
}
