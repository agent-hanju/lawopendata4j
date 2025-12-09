package kr.go.law.committee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 위원회 결정문 목록 조회 응답 DTO
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
public class CommitteeDecisionDto {

    /**
     * 결정문 일련번호 (PK)
     * <p>
     * 필드명은 위원회마다 다를 수 있으나 동일한 역할
     * </p>
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
     * 상세 링크 (API 경로)
     */
    @JsonProperty("상세링크")
    private String detailLink;
}
