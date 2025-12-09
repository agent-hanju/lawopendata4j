package kr.go.law.administrative.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.administrative.enums.AdministrativeSortOption;
import kr.go.law.common.enums.SearchScope;
import kr.go.law.common.request.PageableRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 행정심판례 목록 조회 요청 (target: decc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * AdministrativeListRequest request = AdministrativeListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(AdministrativeSortOption.RESOLUTION_DATE_DESC)
 *     .query("자동차")
 *     .searchScope(SearchScope.CONTENT)
 *     .resolutionDateFrom(20240101)
 *     .resolutionDateTo(20241231)
 *     .build();
 * }
 * </pre>
 */
@Builder
@Getter
public class AdministrativeListRequest implements PageableRequest {

  private static final String TARGET = "decc";

  // ===== 페이지네이션 =====

  /** 페이지 번호 (1-based, API 기본값: 1) */
  private Integer page;

  /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
  private Integer display;

  // ===== 정렬 =====

  /** 정렬 옵션 (기본: lasc 재결례명 오름차순) */
  private AdministrativeSortOption sort;

  // ===== 검색 조건 =====

  /**
   * 검색 범위 (기본: 1 행정심판례명, 2: 본문검색)
   * <p>
   * API 파라미터: search
   * </p>
   */
  private SearchScope searchScope;

  /**
   * 검색어 (검색범위에서 검색을 원하는 질의)
   * <p>
   * API 파라미터: query
   * </p>
   * <p>
   * 예: query="자동차"
   * </p>
   */
  private String query;

  /**
   * 재결례유형 (재결구분코드)
   * <p>
   * API 파라미터: cls
   * </p>
   */
  private String decisionType;

  /**
   * 사전식 검색 (ga, na, da 등)
   * <p>
   * API 파라미터: gana
   * </p>
   */
  private String dictionarySearch;

  /**
   * 의결일자 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: date
   * </p>
   * <p>
   * 단일 일자 검색 시 사용
   * </p>
   */
  private Integer resolutionDate;

  /**
   * 처분일자 시작 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: dpaYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer dispositionDateFrom;

  /**
   * 처분일자 종료 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: dpaYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer dispositionDateTo;

  /**
   * 의결일자 시작 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: rslYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer resolutionDateFrom;

  /**
   * 의결일자 종료 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: rslYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer resolutionDateTo;

  @Override
  public String getTarget() {
    return TARGET;
  }

  @Override
  public Map<String, String> toQueryParameters() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("target", TARGET);
    params.put("type", "JSON");

    if (page != null) {
      params.put("page", String.valueOf(page));
    }
    if (display != null) {
      params.put("display", String.valueOf(display));
    }
    if (sort != null) {
      params.put("sort", sort.getValue());
    }
    if (searchScope != null) {
      params.put("search", searchScope.getValue());
    }
    if (query != null && !query.isBlank()) {
      params.put("query", query);
    }
    if (decisionType != null && !decisionType.isBlank()) {
      params.put("cls", decisionType);
    }
    if (dictionarySearch != null && !dictionarySearch.isBlank()) {
      params.put("gana", dictionarySearch);
    }
    if (resolutionDate != null) {
      params.put("date", String.valueOf(resolutionDate));
    }
    if (dispositionDateFrom != null || dispositionDateTo != null) {
      String from = dispositionDateFrom != null ? String.valueOf(dispositionDateFrom) : "";
      String to = dispositionDateTo != null ? String.valueOf(dispositionDateTo) : "";
      params.put("dpaYd", from + "~" + to);
    }
    if (resolutionDateFrom != null || resolutionDateTo != null) {
      String from = resolutionDateFrom != null ? String.valueOf(resolutionDateFrom) : "";
      String to = resolutionDateTo != null ? String.valueOf(resolutionDateTo) : "";
      params.put("rslYd", from + "~" + to);
    }

    return params;
  }
}
