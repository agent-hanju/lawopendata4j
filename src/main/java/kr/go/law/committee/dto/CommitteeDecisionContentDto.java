package kr.go.law.committee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 위원회 결정문 본문 조회 응답 DTO
 * <p>
 * 개인정보보호위원회(ppc), 공정거래위원회(ftc), 노동위원회(nlrc), 국가인권위원회(nhrck) 등에 공통 사용
 * </p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitteeDecisionContentDto {

    /**
     * 결정문 일련번호
     */
    @JsonProperty("일련번호")
    private String serialNumber;

    /**
     * 사건명/제목
     */
    @JsonProperty("사건명")
    private String caseName;

    /**
     * 사건번호/문서번호
     */
    @JsonProperty("사건번호")
    private String caseNumber;

    /**
     * 의결일자/결정일자 (YYYYMMDD)
     */
    @JsonProperty("의결일자")
    private String decisionDate;

    /**
     * 사건종류/결정종류
     */
    @JsonProperty("사건종류")
    private String caseType;

    /**
     * 사건종류코드
     */
    @JsonProperty("사건종류코드")
    private String caseTypeCode;

    /**
     * 주문
     */
    @JsonProperty("주문")
    private String decree;

    /**
     * 이유/판단
     */
    @JsonProperty("이유")
    private String reason;

    /**
     * 주요내용/요지
     */
    @JsonProperty("주요내용")
    private String summary;

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
