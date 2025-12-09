package kr.go.law.term.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 조문-법령용어 연계 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleTermLinkDto {

    /**
     * 페이지 내 순서 (무시)
     */
    @JsonProperty("id")
    private String id;

    /**
     * 조가지번호
     */
    @JsonProperty("조가지번호")
    private String articleBranchNumber;

    /**
     * 법령명
     */
    @JsonProperty("법령명")
    private String lawName;

    /**
     * 조번호
     */
    @JsonProperty("조번호")
    private String articleNumber;

    /**
     * 조문내용
     */
    @JsonProperty("조문내용")
    private String articleContent;

    /**
     * 연계용어 목록
     */
    @JsonProperty("연계용어")
    private Object linkedTerms; // TermLink 또는 List<TermLink>

    /**
     * 연계용어 항목
     */
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TermLink {

        /**
         * 페이지 내 순서 (무시)
         */
        @JsonProperty("id")
        private String id;

        /**
         * 용어구분
         * - 핵심용어
         * - 선정용어
         */
        @JsonProperty("용어구분")
        private String termType;

        /**
         * 용어구분코드
         * - 140101: 핵심용어
         * - 140102: 선정용어
         */
        @JsonProperty("용어구분코드")
        private String termTypeCode;

        /**
         * 법령용어명
         */
        @JsonProperty("법령용어명")
        private String termName;

        /**
         * 비고
         */
        @JsonProperty("비고")
        private String note;

        /**
         * 용어간관계링크 (API 경로)
         */
        @JsonProperty("용어간관계링크")
        private String termRelationLink;

        /**
         * 용어연계조문링크 (API 경로)
         */
        @JsonProperty("용어연계조문링크")
        private String termArticleLink;
    }
}
