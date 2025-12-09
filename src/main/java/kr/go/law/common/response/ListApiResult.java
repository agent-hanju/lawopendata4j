package kr.go.law.common.response;

import java.util.List;

/**
 * 목록 조회 API 공통 응답
 *
 * @param <T> 목록 항목 타입
 */
public record ListApiResult<T>(
    String rawData,
    List<T> items,
    int totalCount,
    int page,
    int display,
    boolean hasError) implements PaginatedResponse<T> {

  /**
   * 정상 결과 생성
   */
  public static <T> ListApiResult<T> of(String rawData, List<T> items, int totalCount, int page, int display) {
    return new ListApiResult<>(rawData, items, totalCount, page, display, false);
  }

  /**
   * 빈 결과 생성
   */
  public static <T> ListApiResult<T> empty() {
    return new ListApiResult<>(null, List.of(), 0, 1, 0, false);
  }

  /**
   * 에러 결과 생성 (rawData만 포함)
   */
  public static <T> ListApiResult<T> error(String rawData) {
    return new ListApiResult<>(rawData, List.of(), 0, 1, 0, true);
  }
}
