package kr.go.law.administrative.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 행정심판례 본문 조회 요청 (target: decc)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * AdministrativeContentRequest request = AdministrativeContentRequest.builder()
 *     .serialNumber(12345)
 *     .build();
 * }
 * </pre>
 */

@Builder
@Getter
public class AdministrativeContentRequest implements BaseRequest {

  private static final String TARGET = "decc";

  /**
   * 행정심판재결례 일련번호 (필수)
   * <p>
   * API 파라미터: ID
   * </p>
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

  public static class AdministrativeContentRequestBuilder {
    public AdministrativeContentRequest build() {
      if (serialNumber == null) {
        throw new IllegalArgumentException("serialNumber (ID) is required");
      }
      return new AdministrativeContentRequest(serialNumber);
    }
  }
}
