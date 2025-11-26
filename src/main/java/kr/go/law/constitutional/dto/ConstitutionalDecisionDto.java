package kr.go.law.constitutional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 헌재결정례 목록 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstitutionalDecisionDto {

    /**
     * 헌재결정례 일련번호 (PK)
     */
    @JsonProperty("헌재결정례일련번호")
    private String decisionSerialNumber;

    /**
     * 사건명
     */
    @JsonProperty("사건명")
    private String caseName;

    /**
     * 사건번호
     */
    @JsonProperty("사건번호")
    private String caseNumber;

    /**
     * 종국일자 (YYYYMMDD or "0" for 미종결)
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
     * 헌재결정례 상세 링크 (API 경로)
     */
    @JsonProperty("헌재결정례상세링크")
    private String detailLink;
}
