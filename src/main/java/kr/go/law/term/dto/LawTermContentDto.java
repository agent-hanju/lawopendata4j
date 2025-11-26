package kr.go.law.term.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 법령용어 본문 조회 응답 DTO
 *
 * 각 배열의 동일한 인덱스 요소가 하나의 사전 항목을 구성
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawTermContentDto {

    /**
     * 법령용어코드명 배열
     * 예: ["법령용어사전", "법령정의사전", "법령한영사전"]
     */
    @JsonProperty("법령용어코드명")
    private List<String> termCodeNames;

    /**
     * 법령용어일련번호 배열 (MST)
     */
    @JsonProperty("법령용어일련번호")
    private List<String> termSerialNumbers;

    /**
     * 법령용어명 한자 배열
     */
    @JsonProperty("법령용어명_한자")
    private List<String> termNamesHanja;

    /**
     * 법령용어코드 배열
     * - 011401: 법령용어사전
     * - 011402: 법령정의사전
     * - 011403: 법령한영사전
     */
    @JsonProperty("법령용어코드")
    private List<String> termCodes;

    /**
     * 법령용어정의 배열 (매우 긴 텍스트)
     */
    @JsonProperty("법령용어정의")
    private List<String> termDefinitions;

    /**
     * 법령용어명 한글 배열
     */
    @JsonProperty("법령용어명_한글")
    private List<String> termNamesHangul;

    /**
     * 출처 배열
     */
    @JsonProperty("출처")
    private List<String> sources;
}
