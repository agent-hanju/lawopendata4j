package kr.go.law.statute.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.PageableRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 일자별 조문 개정 이력 목록 조회 요청 (target: lsJoHstInf)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * // 특정 일자의 조문 개정 이력 조회
 * StatuteHistoryRequest request = StatuteHistoryRequest.builder()
 *     .revisionDate(20240101)
 *     .build();
 *
 * // 특정 법령의 특정 조문 개정 이력 조회
 * StatuteHistoryRequest request = StatuteHistoryRequest.builder()
 *     .id(2132)
 *     .jo(400) // 제4조 → 000400
 *     .build();
 *
 * // 기간 범위로 조회
 * StatuteHistoryRequest request = StatuteHistoryRequest.builder()
 *     .revisionDateFrom(20240101)
 *     .revisionDateTo(20241231)
 *     .build();
 * }
 * </pre>
 */
@Builder
@Getter
public class StatuteHistoryRequest implements PageableRequest {

  private static final String TARGET = "lsJoHstInf";

  // ===== 페이지네이션 =====

  /** 페이지 번호 (1-based, API 기본값: 1) */
  private Integer page;

  /** display 미지원, 20 고정 */
  @Override
  public Integer getDisplay() {
    return 20;
  }

  // ===== 검색 조건 =====

  /**
   * 조문 개정일 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: regDt
   * </p>
   * <p>
   * 특정 일자의 개정 이력 조회
   * </p>
   */
  private Integer regDt;

  /**
   * 조회기간 시작일 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: fromRegDt
   * </p>
   */
  private Integer fromRegDt;

  /**
   * 조회기간 종료일 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: toRegDt
   * </p>
   */
  private Integer toRegDt;

  /**
   * 법령ID (6자리, 예: 2132 → "002132")
   * <p>
   * API 파라미터: ID
   * </p>
   */
  private Integer id;

  /**
   * 조문번호 (제N조 → N*100, 제N조의M → N*100+M)
   * <p>
   * API 파라미터: JO (6자리 zero-padding)
   * </p>
   * <p>
   * 예: 400 → "000400" (제4조), 202 → "000202" (제2조의2)
   * </p>
   */
  private Integer jo;

  /**
   * 소관부처코드 (7자리, 예: 1371000)
   * <p>
   * API 파라미터: org
   * </p>
   */
  private Integer org;

  @Override
  public String getTarget() {
    return TARGET;
  }

  @Override
  public Map<String, String> toQueryParameters() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("target", TARGET);
    params.put("type", "JSON");

    // 페이지네이션
    if (page != null) {
      params.put("page", String.valueOf(page));
    }
    // Note: lsJoHstInf API는 display 파라미터 미지원

    // 특정 개정일
    if (regDt != null) {
      params.put("regDt", String.valueOf(regDt));
    }

    // 기간 범위
    if (fromRegDt != null) {
      params.put("fromRegDt", String.valueOf(fromRegDt));
    }
    if (toRegDt != null) {
      params.put("toRegDt", String.valueOf(toRegDt));
    }

    // 법령ID (6자리 zero-padding)
    if (id != null) {
      params.put("ID", String.format("%06d", id));
    }

    // 조문번호 (6자리 zero-padding)
    if (jo != null) {
      params.put("JO", String.format("%06d", jo));
    }

    // 소관부처코드 (7자리 zero-padding)
    if (org != null) {
      params.put("org", String.format("%07d", org));
    }

    return params;
  }
}
