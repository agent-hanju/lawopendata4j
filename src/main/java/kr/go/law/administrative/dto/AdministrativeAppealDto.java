package kr.go.law.administrative.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 행정심판례 목록 조회 응답 DTO */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAppealDto {

  /** 행정심판재결례일련번호 (PK) */
  private Integer appealSerialNumber;

  /** 사건명 */
  private String caseName;

  /** 사건번호 */
  private String caseNumber;

  /** 의결일자 (YYYYMMDD) */
  private Integer decisionDate;

  /** 처분일자 (YYYYMMDD) */
  private Integer dispositionDate;

  /** 재결청 */
  private String adjudicationAgency;

  /** 처분청 */
  private String dispositionAgency;

  /** 재결구분코드 */
  private Integer adjudicationTypeCode;

  /** 재결구분명 */
  private String adjudicationTypeName;
}
