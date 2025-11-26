package kr.go.law.statute.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 현행법령(공포일) 본문 조회 요청 (target: law)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * // MST로 조회
 * StatuteContentRequest request = StatuteContentRequest.builder()
 *     .statuteSerialNumber(253527)
 *     .build();
 *
 * // ID로 조회
 * StatuteContentRequest request = StatuteContentRequest.builder()
 *     .lawId(1233)
 *     .build();
 *
 * // 법령명으로 조회
 * StatuteContentRequest request = StatuteContentRequest.builder()
 *     .lawName("개인정보 보호법")
 *     .build();
 *
 * // 특정 조문만 조회
 * StatuteContentRequest request = StatuteContentRequest.builder()
 *     .lawId(1233)
 *     .articleNumber(400)  // 제4조만 조회
 *     .build();
 * }
 * </pre>
 */
@Getter
@Builder
public class StatuteContentRequest implements BaseRequest {

    private static final String TARGET = "law";

    /**
     * 법령ID (6자리, 예: 1233 → "001233")
     * <p>API 파라미터: ID</p>
     * <p>ID 또는 MST 중 하나는 반드시 입력</p>
     */
    private Integer lawId;

    /**
     * 법령일련번호 (법령테이블의 lsi_seq 값)
     * <p>API 파라미터: MST</p>
     * <p>ID 또는 MST 중 하나는 반드시 입력</p>
     */
    private Integer statuteSerialNumber;

    /**
     * 법령명
     * <p>API 파라미터: LM</p>
     * <p>법령명 입력시 해당 법령 링크</p>
     */
    private String lawName;

    /**
     * 공포일자 (YYYYMMDD 형태의 정수)
     * <p>API 파라미터: LD</p>
     */
    private Integer promulgationDate;

    /**
     * 공포번호
     * <p>API 파라미터: LN</p>
     */
    private Integer promulgationNumber;

    /**
     * 조번호 (제N조 → N*100, 제N조의M → N*100+M)
     * <p>API 파라미터: JO (6자리 zero-padding)</p>
     * <p>생략(기본값): 모든 조를 표시</p>
     * <p>예: 400 → "000400" (제4조), 1002 → "001002" (제10조의2)</p>
     */
    private Integer articleNumber;

    /**
     * 원문/한글 여부
     * <p>API 파라미터: LANG</p>
     * <p>생략(기본값): 한글</p>
     * <p>KO: 한글, ORI: 원문</p>
     */
    private String language;

    @Override
    public String getTarget() {
        return TARGET;
    }

    @Override
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("target", TARGET);
        params.put("type", "JSON");

        // 법령ID (6자리 zero-padding)
        if (lawId != null) {
            params.put("ID", String.format("%06d", lawId));
        }

        // 법령일련번호
        if (statuteSerialNumber != null) {
            params.put("MST", String.valueOf(statuteSerialNumber));
        }

        // 법령명
        if (lawName != null && !lawName.isBlank()) {
            params.put("LM", lawName);
        }

        // 공포일자
        if (promulgationDate != null) {
            params.put("LD", String.valueOf(promulgationDate));
        }

        // 공포번호
        if (promulgationNumber != null) {
            params.put("LN", String.valueOf(promulgationNumber));
        }

        // 조번호 (6자리 zero-padding)
        if (articleNumber != null) {
            params.put("JO", String.format("%06d", articleNumber));
        }

        // 원문/한글 여부
        if (language != null && !language.isBlank()) {
            params.put("LANG", language);
        }

        return params;
    }
}
