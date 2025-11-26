package kr.go.law.term.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import kr.go.law.term.enums.TermRelationType;
import lombok.Builder;
import lombok.Getter;

/**
 * 법령용어-일상용어 연계 조회 요청 (target: lstrmRlt)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * TermRelationRequest request = TermRelationRequest.builder()
 *     .query("청원")
 *     .relationType(TermRelationType.SYNONYM)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class TermRelationRequest implements BaseRequest {

    private static final String TARGET = "lstrmRlt";

    /**
     * 법령용어 검색어
     * <p>API 파라미터: query</p>
     */
    private String query;

    /**
     * 법령용어 일련번호
     * <p>API 파라미터: MST</p>
     */
    private Integer termSerialNumber;

    /**
     * 용어관계 유형
     * <p>API 파라미터: trmRltCd</p>
     */
    private TermRelationType relationType;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        if (query != null && !query.isBlank()) {
            params.put("query", query);
        }
        if (termSerialNumber != null) {
            params.put("MST", String.valueOf(termSerialNumber));
        }
        if (relationType != null) {
            params.put("trmRltCd", relationType.getCode());
        }

        return params;
    }
}
