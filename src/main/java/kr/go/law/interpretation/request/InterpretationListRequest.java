package kr.go.law.interpretation.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.enums.SearchScope;
import kr.go.law.common.request.PageableRequest;
import kr.go.law.interpretation.enums.InterpretationSortOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 법령해석례 목록 조회 요청 (target: expc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * InterpretationListRequest request = InterpretationListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(InterpretationSortOption.INTERPRETATION_DATE_DESC)
 *     .query("개인정보")
 *     .searchScope(SearchScope.CONTENT)
 *     .registrationDateFrom(20240101)
 *     .registrationDateTo(20241231)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class InterpretationListRequest implements PageableRequest {

    private static final String TARGET = "expc";

    // ===== 페이지네이션 =====

    /** 페이지 번호 (1-based, API 기본값: 1) */
    private Integer page;

    /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
    private Integer display;

    // ===== 정렬 =====

    /** 정렬 옵션 (기본: lasc 법령해석례명 오름차순) */
    private InterpretationSortOption sort;

    // ===== 검색 조건 =====

    /**
     * 검색 범위 (기본: 1 법령해석례명, 2: 본문검색)
     * <p>API 파라미터: search</p>
     */
    private SearchScope searchScope;

    /**
     * 검색어 (검색범위에서 검색을 원하는 질의)
     * <p>API 파라미터: query</p>
     * <p>예: query="자동차"</p>
     */
    private String query;

    /**
     * 질의기관
     * <p>API 파라미터: inq</p>
     */
    private String inquiryAgency;

    /**
     * 회신기관
     * <p>API 파라미터: rpl</p>
     */
    private String replyAgency;

    /**
     * 사전식 검색 (ga, na, da 등)
     * <p>API 파라미터: gana</p>
     */
    private String dictionarySearch;

    /**
     * 안건번호 (예: 13-0217 → 130217)
     * <p>API 파라미터: itmno</p>
     */
    private String agendaNumber;

    /**
     * 등록일자 시작 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: regYd (시작~종료 형태로 전송)</p>
     */
    private Integer registrationDateFrom;

    /**
     * 등록일자 종료 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: regYd (시작~종료 형태로 전송)</p>
     */
    private Integer registrationDateTo;

    /**
     * 해석일자 시작 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: explYd (시작~종료 형태로 전송)</p>
     */
    private Integer interpretationDateFrom;

    /**
     * 해석일자 종료 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: explYd (시작~종료 형태로 전송)</p>
     */
    private Integer interpretationDateTo;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        addPaginationParams(params);
        addSortParams(params);
        addSearchParams(params);
        addDateParams(params);

        return params;
    }

    private void addPaginationParams(Map<String, String> params) {
        if (page != null) {
            params.put("page", String.valueOf(page));
        }
        if (display != null) {
            params.put("display", String.valueOf(display));
        }
    }

    private void addSortParams(Map<String, String> params) {
        if (sort != null) {
            params.put("sort", sort.getValue());
        }
    }

    private void addSearchParams(Map<String, String> params) {
        if (searchScope != null) {
            params.put("search", searchScope.getValue());
        }
        if (query != null && !query.isBlank()) {
            params.put("query", query);
        }
        if (inquiryAgency != null && !inquiryAgency.isBlank()) {
            params.put("inq", inquiryAgency);
        }
        if (replyAgency != null && !replyAgency.isBlank()) {
            params.put("rpl", replyAgency);
        }
        if (dictionarySearch != null && !dictionarySearch.isBlank()) {
            params.put("gana", dictionarySearch);
        }
        if (agendaNumber != null && !agendaNumber.isBlank()) {
            params.put("itmno", agendaNumber);
        }
    }

    private void addDateParams(Map<String, String> params) {
        if (registrationDateFrom != null || registrationDateTo != null) {
            String from = registrationDateFrom != null ? String.valueOf(registrationDateFrom) : "";
            String to = registrationDateTo != null ? String.valueOf(registrationDateTo) : "";
            params.put("regYd", from + "~" + to);
        }
        if (interpretationDateFrom != null || interpretationDateTo != null) {
            String from = interpretationDateFrom != null ? String.valueOf(interpretationDateFrom) : "";
            String to = interpretationDateTo != null ? String.valueOf(interpretationDateTo) : "";
            params.put("explYd", from + "~" + to);
        }
    }
}
