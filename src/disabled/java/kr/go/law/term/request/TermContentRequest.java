package kr.go.law.term.request;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 법령용어 본문 조회 요청 (target: lstrm)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * TermContentRequest request = TermContentRequest.builder()
 *     .query("청원")
 *     .termSerialNumbers(List.of("12345", "67890"))
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class TermContentRequest implements BaseRequest {

    private static final String TARGET = "lstrm";

    /**
     * 법령용어 검색어 (필수)
     * <p>API 파라미터: query</p>
     */
    private final String query;

    /**
     * 법령용어 일련번호 목록 (선택)
     * <p>API 파라미터: trmSeqs (콤마로 join하여 전송)</p>
     */
    private List<String> termSerialNumbers;

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
        if (termSerialNumbers != null && !termSerialNumbers.isEmpty()) {
            params.put("trmSeqs", String.join(",", termSerialNumbers));
        }

        return params;
    }

    public static class TermContentRequestBuilder {
        public TermContentRequest build() {
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException("query is required");
            }
            return new TermContentRequest(query, termSerialNumbers);
        }
    }
}
