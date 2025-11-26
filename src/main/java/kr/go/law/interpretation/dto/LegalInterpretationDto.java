package kr.go.law.interpretation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 법령해석례 목록 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalInterpretationDto {

    /**
     * 법령해석례 일련번호 (PK)
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
     * 회신일자 (YYYYMMDD or "" for 미회신)
     */
    @JsonProperty("회신일자")
    private String replyDate;

    /**
     * 회신기관명
     */
    @JsonProperty("회신기관명")
    private String replyAgencyName;

    /**
     * 회신기관코드
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
     * 법령해석례 상세 링크 (API 경로)
     */
    @JsonProperty("법령해석례상세링크")
    private String detailLink;
}
