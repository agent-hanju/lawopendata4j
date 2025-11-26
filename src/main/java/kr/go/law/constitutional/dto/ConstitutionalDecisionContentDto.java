package kr.go.law.constitutional.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 헌재결정례 본문 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstitutionalDecisionContentDto {

    /**
     * 헌재결정례 일련번호
     */
    @JsonProperty("헌재결정례일련번호")
    private String decisionSerialNumber;

    /**
     * 사건번호
     */
    @JsonProperty("사건번호")
    private String caseNumber;

    /**
     * 사건명
     */
    @JsonProperty("사건명")
    private String caseName;

    /**
     * 종국일자 (YYYYMMDD)
     */
    @JsonProperty("종국일자")
    private String finalDate;

    /**
     * 사건종류명
     */
    @JsonProperty("사건종류명")
    private String caseTypeName;

    /**
     * 사건종류코드
     */
    @JsonProperty("사건종류코드")
    private String caseTypeCode;

    /**
     * 재판부구분코드
     */
    @JsonProperty("재판부구분코드")
    private String courtDivisionCode;

    /**
     * 판시사항 (실제로는 "사건 법률조항" + 판시사항 혼합)
     */
    @JsonProperty("판시사항")
    private String judgmentMatters;

    /**
     * 참조조문
     */
    @JsonProperty("참조조문")
    private String referredArticles;

    /**
     * 참조판례
     */
    @JsonProperty("참조판례")
    private String referredPrecedents;
}
