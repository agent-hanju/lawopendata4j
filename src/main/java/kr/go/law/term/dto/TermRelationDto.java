package kr.go.law.term.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 법령용어-일상용어 연계 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermRelationDto {

    /**
     * 페이지 내 순서 (무시)
     */
    @JsonProperty("id")
    private String id;

    /**
     * 법령용어명
     */
    @JsonProperty("법령용어명")
    private String lawTermName;

    /**
     * 비고
     */
    @JsonProperty("비고")
    private String note;

    /**
     * 연계용어 목록
     */
    @JsonProperty("연계용어")
    private Object linkedDailyTerms; // DailyTermLink 또는 List<DailyTermLink>

    /**
     * 일상용어 연계 항목
     */
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyTermLink {

        /**
         * 페이지 내 순서 (무시)
         */
        @JsonProperty("id")
        private String id;

        /**
         * 일상용어명
         */
        @JsonProperty("일상용어명")
        private String dailyTermName;

        /**
         * 용어관계
         * - 동의어, 반의어, 상위어, 하위어, 연관어
         */
        @JsonProperty("용어관계")
        private String relation;

        /**
         * 용어관계코드
         * - 140301: 동의어
         * - 140302: 반의어
         * - 140303: 상위어
         * - 140304: 하위어
         * - 140305: 연관어
         */
        @JsonProperty("용어관계코드")
        private String relationCode;

        /**
         * 용어간관계링크 (API 경로)
         */
        @JsonProperty("용어간관계링크")
        private String relationLink;

        /**
         * 일상용어조회링크 (API 경로)
         */
        @JsonProperty("일상용어조회링크")
        private String dailyTermLink;
    }
}
