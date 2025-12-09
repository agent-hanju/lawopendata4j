package kr.go.law.interpretation.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 법령해석례 본문 조회 요청 (target: expc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * InterpretationContentRequest request = InterpretationContentRequest.builder()
 *     .serialNumber(12345)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class InterpretationContentRequest implements BaseRequest {

    private static final String TARGET = "expc";

    /**
     * 법령해석례 일련번호 (필수)
     * <p>API 파라미터: ID</p>
     */
    private final Integer serialNumber;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        if (serialNumber != null) {
            params.put("ID", String.valueOf(serialNumber));
        }

        return params;
    }

    public static class InterpretationContentRequestBuilder {
        public InterpretationContentRequest build() {
            if (serialNumber == null) {
                throw new IllegalArgumentException("serialNumber (ID) is required");
            }
            return new InterpretationContentRequest(serialNumber);
        }
    }
}
