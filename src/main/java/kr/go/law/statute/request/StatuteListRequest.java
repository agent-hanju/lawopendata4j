package kr.go.law.statute.request;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kr.go.law.common.enums.SearchScope;
import kr.go.law.common.request.PageableRequest;
import kr.go.law.statute.enums.StatuteSortOption;
import kr.go.law.statute.enums.StatuteStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 현행법령(시행일) 목록 조회 요청 (target: eflaw)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawSearch.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * StatuteListRequest request = StatuteListRequest.builder()
 *     .page(1)
 *     .display(100)
 *     .sort(StatuteSortOption.PROMULGATION_DATE_DESC)
 *     .query("개인정보")
 *     .promulgationDateFrom(20240101)
 *     .promulgationDateTo(20241231)
 *     .statuses(List.of(StatuteStatus.CURRENT))
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class StatuteListRequest implements PageableRequest {

    private static final String TARGET = "eflaw";

    // ===== 페이지네이션 =====

    /** 페이지 번호 (1-based, API 기본값: 1) */
    private Integer page;

    /** 페이지당 표시 건수 (API 기본값: 20, 최대: 100) */
    private Integer display;

    // ===== 정렬 =====

    /** 정렬 옵션 */
    private StatuteSortOption sort;

    // ===== 검색 조건 =====

    /**
     * 검색 범위 (기본: 1 법령명, 2: 본문검색)
     * <p>API 파라미터: search</p>
     */
    private SearchScope searchScope;

    /** 법령명 검색어 (query) */
    private String query;

    /**
     * 공포일자 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: date</p>
     * <p>단일 일자 검색 시 사용</p>
     */
    private Integer promulgationDate;

    /**
     * 공포일자 시작 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: ancYd (시작~종료 형태로 전송)</p>
     */
    private Integer promulgationDateFrom;

    /**
     * 공포일자 종료 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: ancYd (시작~종료 형태로 전송)</p>
     */
    private Integer promulgationDateTo;

    /**
     * 시행일자 시작 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: efYd (시작~종료 형태로 전송)</p>
     */
    private Integer effectiveDateFrom;

    /**
     * 시행일자 종료 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: efYd (시작~종료 형태로 전송)</p>
     */
    private Integer effectiveDateTo;

    /**
     * 법령ID (6자리, 예: 10719 → "010719")
     * <p>API 파라미터: LID</p>
     */
    private Integer lawId;

    /**
     * 법령 상태 목록 (현행/연혁/시행예정)
     * <p>API 파라미터: nw (콤마 구분)</p>
     */
    private List<StatuteStatus> statuses;

    /**
     * 소관부처코드 (7자리, 예: 1371000)
     * <p>API 파라미터: org</p>
     */
    private Integer organizationCode;

    /**
     * 법령종류코드
     * <p>API 파라미터: knd</p>
     * <p>예: A0101(헌법), A0102(법률), A0103(대통령령), A0104(국무총리령), A0105(부령)</p>
     */
    private String kindCode;

    /**
     * 공포번호 시작 (범위 검색)
     * <p>API 파라미터: ancNo (시작~종료 형태로 전송)</p>
     */
    private Integer promulgationNumberFrom;

    /**
     * 공포번호 종료 (범위 검색)
     * <p>API 파라미터: ancNo (시작~종료 형태로 전송)</p>
     */
    private Integer promulgationNumberTo;

    /**
     * 법령 제개정 종류 코드
     * <p>API 파라미터: rrClsCd</p>
     * <p>예: 300201(제정), 300202(일부개정), 300203(전부개정), 300204(폐지), 300205(폐지제정), 300206(일괄개정), 300207(일괄폐지), 300209(타법개정), 300210(타법폐지), 300208(기타)</p>
     */
    private String revisionTypeCode;

    /**
     * 법령의 공포번호
     * <p>API 파라미터: nb</p>
     */
    private Integer promulgationNumber;

    /**
     * 사전식 검색 (ga, na, da 등)
     * <p>API 파라미터: gana</p>
     */
    private String dictionarySearch;

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
        if (display != null) {
            params.put("display", String.valueOf(display));
        }

        // 정렬
        if (sort != null) {
            params.put("sort", sort.getValue());
        }

        // 검색 조건
        if (searchScope != null) {
            params.put("search", searchScope.getValue());
        }
        if (query != null && !query.isBlank()) {
            params.put("query", query);
        }

        // 공포일자 (date)
        if (promulgationDate != null) {
            params.put("date", String.valueOf(promulgationDate));
        }

        // 공포일자 범위 (ancYd)
        if (promulgationDateFrom != null || promulgationDateTo != null) {
            String from = promulgationDateFrom != null ? String.valueOf(promulgationDateFrom) : "";
            String to = promulgationDateTo != null ? String.valueOf(promulgationDateTo) : "";
            params.put("ancYd", from + "~" + to);
        }

        // 시행일자 범위 (efYd)
        if (effectiveDateFrom != null || effectiveDateTo != null) {
            String from = effectiveDateFrom != null ? String.valueOf(effectiveDateFrom) : "";
            String to = effectiveDateTo != null ? String.valueOf(effectiveDateTo) : "";
            params.put("efYd", from + "~" + to);
        }

        // 법령ID (6자리 zero-padding)
        if (lawId != null) {
            params.put("LID", String.format("%06d", lawId));
        }

        // 법령 상태 (콤마 구분)
        if (statuses != null && !statuses.isEmpty()) {
            String nw = statuses.stream()
                    .map(StatuteStatus::getValue)
                    .collect(Collectors.joining(","));
            params.put("nw", nw);
        }

        // 소관부처코드 (7자리 zero-padding)
        if (organizationCode != null) {
            params.put("org", String.format("%07d", organizationCode));
        }

        // 법령종류코드 (String 그대로 - 알파벳 포함)
        if (kindCode != null && !kindCode.isBlank()) {
            params.put("knd", kindCode);
        }

        // 공포번호 범위 (ancNo)
        if (promulgationNumberFrom != null || promulgationNumberTo != null) {
            String from = promulgationNumberFrom != null ? String.valueOf(promulgationNumberFrom) : "";
            String to = promulgationNumberTo != null ? String.valueOf(promulgationNumberTo) : "";
            params.put("ancNo", from + "~" + to);
        }

        // 법령 제개정 종류 코드
        if (revisionTypeCode != null && !revisionTypeCode.isBlank()) {
            params.put("rrClsCd", revisionTypeCode);
        }

        // 법령의 공포번호
        if (promulgationNumber != null) {
            params.put("nb", String.valueOf(promulgationNumber));
        }

        // 사전식 검색
        if (dictionarySearch != null && !dictionarySearch.isBlank()) {
            params.put("gana", dictionarySearch);
        }

        return params;
    }
}
