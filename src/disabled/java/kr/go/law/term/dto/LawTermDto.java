package kr.go.law.term.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 법령용어 목록 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawTermDto {

    /**
     * 페이지 내 순서 (무시)
     */
    @JsonProperty("id")
    private String id;

    /**
     * 법령용어 일련번호 (PK)
     */
    @JsonProperty("법령용어ID")
    private String lawTermId;

    /**
     * 법령용어명
     */
    @JsonProperty("법령용어명")
    private String termName;

    /**
     * 법령종류코드
     */
    @JsonProperty("법령종류코드")
    private String lawTypeCode;

    /**
     * 사전구분코드
     * - 011401: 법령용어사전
     * - 011402: 법령정의사전
     * - 011403: 법령한영사전
     */
    @JsonProperty("사전구분코드")
    private String dictionaryTypeCode;

    /**
     * 법령용어 상세 링크 (API 경로)
     */
    @JsonProperty("법령용어상세링크")
    private String detailApiLink;

    /**
     * 법령용어 상세 검색 (웹 경로)
     */
    @JsonProperty("법령용어상세검색")
    private String detailWebLink;
}
