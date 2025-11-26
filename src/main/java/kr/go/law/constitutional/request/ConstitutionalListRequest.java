package kr.go.law.constitutional.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.enums.SearchScope;
import kr.go.law.common.request.PageableRequest;
import kr.go.law.constitutional.enums.ConstitutionalSortOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 헌재결정례 목록 조회 요청 (target: detc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * ConstitutionalListRequest request = ConstitutionalListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(ConstitutionalSortOption.END_DATE_DESC)
 *     .query("평등권")
 *     .searchScope(SearchScope.CONTENT)
 *     .endDateFrom(20240101)
 *     .endDateTo(20241231)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class ConstitutionalListRequest implements PageableRequest {

  private static final String TARGET = "detc";

  // ===== 페이지네이션 =====

  /** 페이지 번호 (1-based, API 기본값: 1) */
  private Integer page;

  /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
  private Integer display;

  // ===== 정렬 =====

  /** 정렬 옵션 (기본: lasc 사건명 오름차순) */
  private ConstitutionalSortOption sort;

  // ===== 검색 조건 =====

  /**
   * 검색 범위 (기본: 1 헌재결정례명, 2: 본문검색)
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
   * 사전식 검색 (ga, na, da 등)
   * <p>
   * API 파라미터: gana
   * </p>
   */
  private String dictionarySearch;

  /**
   * 종국일자 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: date
   * </p>
   * <p>
   * 단일 일자 검색 시 사용
   * </p>
   */
  private Integer endDate;

  /**
   * 종국일자 시작 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: edYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer endDateFrom;

  /**
   * 종국일자 종료 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: edYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer endDateTo;

  /**
   * 사건번호
   * <p>
   * API 파라미터: nb
   * </p>
   */
  private String caseNumber;

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
    if (dictionarySearch != null && !dictionarySearch.isBlank()) {
      params.put("gana", dictionarySearch);
    }
    if (caseNumber != null && !caseNumber.isBlank()) {
      params.put("nb", caseNumber);
    }
    if (endDate != null) {
      params.put("date", String.valueOf(endDate));
    }
    if (endDateFrom != null || endDateTo != null) {
      String from = endDateFrom != null ? String.valueOf(endDateFrom) : "";
      String to = endDateTo != null ? String.valueOf(endDateTo) : "";
      params.put("edYd", from + "~" + to);
    }

    return params;
  }
}
