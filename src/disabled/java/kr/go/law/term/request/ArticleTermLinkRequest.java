package kr.go.law.term.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 조문-법령용어 연계 조회 요청 (target: joRltLstrm)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * ArticleTermLinkRequest request = ArticleTermLinkRequest.builder()
 *     .lawId(1233)
 *     .articleNumber(400)  // 제4조 → 000400 (필수)
 *     .query("개인정보")   // query 또는 ID 중 하나는 필수
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class ArticleTermLinkRequest implements BaseRequest {

    private static final String TARGET = "joRltLstrm";

    /**
     * 법령명 검색어
     * <p>API 파라미터: query</p>
     * <p>query 또는 ID 중 하나는 반드시 입력</p>
     */
    private String query;

    /**
     * 법령ID (6자리, 예: 1233 → "001233")
     * <p>API 파라미터: ID</p>
     * <p>query 또는 ID 중 하나는 반드시 입력</p>
     */
    private Integer lawId;

    /**
     * 조문번호 (필수) (제N조 → N*100, 제N조의M → N*100+M)
     * <p>API 파라미터: JO (6자리 zero-padding)</p>
     * <p>예: 400 → "000400" (제4조)</p>
     * <p>조번호 4자리 + 조가지번호 2자리</p>
     */
    private Integer articleNumber;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        if (lawId != null) {
            params.put("ID", String.format("%06d", lawId));
        }
        if (articleNumber != null) {
            params.put("JO", String.format("%06d", articleNumber));
        }
        if (query != null && !query.isBlank()) {
            params.put("query", query);
        }

        return params;
    }
}
