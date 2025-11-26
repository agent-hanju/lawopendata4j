package kr.go.law.term.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.PageableRequest;
import kr.go.law.term.enums.TermSortOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 법령용어 목록 조회 요청 (target: lstrm)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * TermListRequest request = TermListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(TermSortOption.RELEVANCE_DESC)
 *     .query("청원")
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class TermListRequest implements PageableRequest {

    private static final String TARGET = "lstrm";

    // ===== 페이지네이션 =====

    /** 페이지 번호 (1-based, API 기본값: 1) */
    private Integer page;

    /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
    private Integer display;

    // ===== 정렬 =====

    /** 정렬 옵션 */
    private TermSortOption sort;

    // ===== 검색 조건 =====

    /** 검색어 (query) */
    private String query;

    /**
     * 등록일자 시작 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: regDt (시작~종료 형태로 전송)</p>
     */
    private Integer registrationDateFrom;

    /**
     * 등록일자 종료 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: regDt (시작~종료 형태로 전송)</p>
     */
    private Integer registrationDateTo;

    /**
     * 사전식 검색 (ga, na, da 등)
     * <p>API 파라미터: gana</p>
     */
    private String dictionarySearch;

    /**
     * 법령 종류 코드
     * <p>API 파라미터: dicKndCd</p>
     * <p>예: 010101(법령), 010102(행정규칙)</p>
     */
    private String dictionaryKindCode;

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
        if (registrationDateFrom != null || registrationDateTo != null) {
            String from = registrationDateFrom != null ? String.valueOf(registrationDateFrom) : "";
            String to = registrationDateTo != null ? String.valueOf(registrationDateTo) : "";
            params.put("regDt", from + "~" + to);
        }
        if (dictionarySearch != null && !dictionarySearch.isBlank()) {
            params.put("gana", dictionarySearch);
        }
        if (dictionaryKindCode != null && !dictionaryKindCode.isBlank()) {
            params.put("dicKndCd", dictionaryKindCode);
        }

        return params;
    }
}
