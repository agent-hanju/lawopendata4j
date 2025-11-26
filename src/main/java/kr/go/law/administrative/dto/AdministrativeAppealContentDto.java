package kr.go.law.administrative.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 행정심판례 본문 조회 응답 DTO */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrativeAppealContentDto {

  /** 행정심판례 일련번호 */
  private Integer appealSerialNumber;

  /** 사건명 */
  private String caseName;

  /** 사건번호 */
  private String caseNumber;

  /** 재결례유형코드 */
  private Integer adjudicationTypeCode;

  /** 재결례유형명 */
  private String adjudicationTypeName;

  /** 재결청 */
  private String adjudicationAgency;

  /** 의결일자 (YYYYMMDD) */
  private Integer decisionDate;

  /** 처분청 */
  private String dispositionAgency;

  /** 처분일자 (YYYYMMDD) */
  private Integer dispositionDate;

  /** 청구취지 */
  private String claimPurpose;

  /** 재결요지 */
  private String adjudicationSummary;

  /** 주문 */
  private String decree;

  /** 이유 */
  private String reason;
}
