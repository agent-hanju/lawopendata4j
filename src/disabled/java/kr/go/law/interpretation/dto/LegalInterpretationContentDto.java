package kr.go.law.interpretation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 법령해석례 본문 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalInterpretationContentDto {

    /**
     * 법령해석례 일련번호
     */
    @JsonProperty("법령해석례일련번호")
    private String interpretationSerialNumber;

    /**
     * 안건명
     */
    @JsonProperty("안건명")
    private String agendaName;

    /**
     * 안건번호
     */
    @JsonProperty("안건번호")
    private String agendaNumber;

    /**
     * 회신일자 (YYYYMMDD)
     */
    @JsonProperty("회신일자")
    private String replyDate;

    /**
     * 회신기관명
     */
    @JsonProperty("회신기관명")
    private String replyAgencyName;

    /**
     * 회신기관코드 (해석기관코드)
     */
    @JsonProperty("회신기관코드")
    private String replyAgencyCode;

    /**
     * 질의기관명
     */
    @JsonProperty("질의기관명")
    private String inquiryAgencyName;

    /**
     * 질의기관코드
     */
    @JsonProperty("질의기관코드")
    private String inquiryAgencyCode;

    /**
     * 등록일시 (YYYYMMDD)
     */
    @JsonProperty("등록일시")
    private String registrationDate;

    /**
     * 회답 (회신 내용 요약)
     */
    @JsonProperty("회답")
    private String reply;

    /**
     * 해석 (전문 내용, 매우 긴 텍스트)
     */
    @JsonProperty("해석")
    private String interpretation;
}
