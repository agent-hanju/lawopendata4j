package kr.go.law.common.request;

import java.util.Map;

/**
 * API 요청 기본 인터페이스
 * <p>
 * 모든 API 요청 빌더가 구현해야 하는 공통 인터페이스입니다.
 * </p>
 */
public interface BaseRequest {

  /**
   * API target 값 반환
   * <p>
   * 예: "eflaw", "law", "prec", "detc" 등
   * </p>
   *
   * @return target 문자열
   */
  String getTarget();

  /**
   * 요청 파라미터를 Map으로 변환
   * <p>
   * null이 아닌 값만 포함하며, zero-padding이 필요한 경우 변환하여 반환합니다.
   * </p>
   *
   * @return 파라미터 Map (key: 파라미터명, value: 문자열 값)
   */
  Map<String, String> toQueryParameters();
}
