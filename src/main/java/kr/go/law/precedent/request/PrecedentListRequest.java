package kr.go.law.precedent.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.enums.SearchScope;
import kr.go.law.common.request.PageableRequest;
import kr.go.law.precedent.enums.CourtType;
import kr.go.law.precedent.enums.PrecedentSortOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 판례 목록 조회 요청 (target: prec)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * PrecedentListRequest request = PrecedentListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(PrecedentSortOption.DECISION_DATE_DESC)
 *     .query("손해배상")
 *     .searchScope(SearchScope.CASE_NAME)
 *     .decisionDateFrom(20240101)
 *     .decisionDateTo(20241231)
 *     .courtType(CourtType.SUPREME_COURT)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class PrecedentListRequest implements PageableRequest {

  private static final String TARGET = "prec";

  // ===== 페이지네이션 =====

  /** 페이지 번호 (1-based, API 기본값: 1) */
  private Integer page;

  /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
  private Integer display;

  // ===== 정렬 =====

  /** 정렬 옵션 */
  private PrecedentSortOption sort;

  // ===== 검색 조건 =====

  /** 검색어 (query) */
  private String query;

  /**
   * 검색 범위 (1: 판례명, 2: 본문)
   * <p>
   * API 파라미터: search
   * </p>
   */
  private SearchScope searchScope;

  /**
   * 선고일자 시작 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: prncYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer decisionDateFrom;

  /**
   * 선고일자 종료 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: prncYd (시작~종료 형태로 전송)
   * </p>
   */
  private Integer decisionDateTo;

  /**
   * 특정 선고일자 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: date
   * </p>
   * <p>
   * 단일 일자 검색 시 사용
   * </p>
   */
  private Integer decisionDate;

  /**
   * 법원 종류
   * <p>
   * API 파라미터: org
   * </p>
   */
  private CourtType courtType;

  /**
   * 법원명
   * <p>
   * API 파라미터: curt
   * </p>
   */
  private String courtName;

  /**
   * 사건번호
   * <p>
   * API 파라미터: nb
   * </p>
   * <p>
   * 예: "2024다12345"
   * </p>
   */
  private String nb;

  /**
   * 참조법령명
   * <p>
   * API 파라미터: JO
   * </p>
   */
  private String refJo;

  /**
   * 사전식 검색 (ga, na, da 등)
   * <p>
   * API 파라미터: gana
   * </p>
   */
  private String dictionarySearch;

  /**
   * 데이터출처명
   * <p>
   * API 파라미터: datSrcNm
   * </p>
   */
  private String dataSource;

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
    if (query != null && !query.isBlank()) {
      params.put("query", query);
    }
    if (searchScope != null) {
      params.put("search", searchScope.getValue());
    }
    if (nb != null && !nb.isBlank()) {
      params.put("nb", nb);
    }
    if (refJo != null && !refJo.isBlank()) {
      params.put("JO", refJo);
    }
    if (dictionarySearch != null && !dictionarySearch.isBlank()) {
      params.put("gana", dictionarySearch);
    }
    if (dataSource != null && !dataSource.isBlank()) {
      params.put("datSrcNm", dataSource);
    }
    if (decisionDateFrom != null || decisionDateTo != null) {
      String from = decisionDateFrom != null ? String.valueOf(decisionDateFrom) : "";
      String to = decisionDateTo != null ? String.valueOf(decisionDateTo) : "";
      params.put("prncYd", from + "~" + to);
    }
    if (decisionDate != null) {
      params.put("date", String.valueOf(decisionDate));
    }
    if (courtType != null) {
      params.put("org", courtType.getCode());
    }
    if (courtName != null && !courtName.isBlank()) {
      params.put("curt", courtName);
    }

    return params;
  }
}
