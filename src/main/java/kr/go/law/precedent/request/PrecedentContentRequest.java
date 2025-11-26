package kr.go.law.precedent.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 판례 본문 조회 요청 (target: prec)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * PrecedentContentRequest request = PrecedentContentRequest.builder()
 *     .precedentSerialNumber(608799)
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class PrecedentContentRequest implements BaseRequest {

    private static final String TARGET = "prec";

    /**
     * 판례일련번호 (필수)
     * <p>API 파라미터: ID</p>
     * <p>판례 목록 조회 응답의 '판례일련번호' 필드 값</p>
     */
    private Integer precedentSerialNumber;

    /**
     * 판례명
     * <p>API 파라미터: LM</p>
     */
    private String precedentName;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        // 판례일련번호 (필수)
        if (precedentSerialNumber != null) {
            params.put("ID", String.valueOf(precedentSerialNumber));
        }

        // 판례명
        if (precedentName != null && !precedentName.isBlank()) {
            params.put("LM", precedentName);
        }

        return params;
    }
}
