package kr.go.law.constitutional.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 헌재결정례 본문 조회 요청 (target: detc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * ConstitutionalContentRequest request = ConstitutionalContentRequest.builder()
 *     .serialNumber(12345)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class ConstitutionalContentRequest implements BaseRequest {

    private static final String TARGET = "detc";

    /**
     * 헌재결정례 일련번호 (필수)
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

    public static class ConstitutionalContentRequestBuilder {
        public ConstitutionalContentRequest build() {
            if (serialNumber == null) {
                throw new IllegalArgumentException("serialNumber (ID) is required");
            }
            return new ConstitutionalContentRequest(serialNumber);
        }
    }
}
