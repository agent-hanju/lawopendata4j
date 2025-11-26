package kr.go.law.administrative.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 행정심판례(decc) 목록 조회 정렬 옵션 */
@Getter
@RequiredArgsConstructor
public enum AdministrativeSortOption {

  /** 재결례명 오름차순 (기본값) */
  NAME_ASC("lasc"),

  /** 재결례명 내림차순 */
  NAME_DESC("ldes"),

  /** 의결일자 오름차순 */
  RESOLUTION_DATE_ASC("dasc"),

  /** 의결일자 내림차순 */
  RESOLUTION_DATE_DESC("ddes"),

  /** 사건번호 오름차순 */
  CASE_NUMBER_ASC("nasc"),

  /** 사건번호 내림차순 */
  CASE_NUMBER_DESC("ndes");

  private final String value;
}
