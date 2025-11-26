package kr.go.law.common.response;

import java.util.List;

/**
 * 페이지네이션 응답 인터페이스
 * <p>
 * 목록 조회 API의 페이지네이션된 응답이 구현합니다.
 * </p>
 *
 * @param <T> 목록 항목 타입
 */
public interface PaginatedResponse<T> {
  /**
   * 항목 목록 반환
   *
   * @return 항목 목록
   */
  List<T> items();

  /**
   * 전체 항목 수 반환
   *
   * @return 전체 건수
   */
  int totalCount();

  /**
   * 현재 페이지 번호 반환
   *
   * @return 페이지 번호 (1-based)
   */
  int page();

  /**
   * 페이지당 표시 건수 반환
   *
   * @return 표시 건수
   */
  int display();

  /**
   * 다음 페이지 존재 여부 확인
   *
   * @return 다음 페이지가 있으면 true
   */
  default boolean hasNextPage() {
    return (long) page() * display() < totalCount();
  }

  /**
   * 전체 페이지 수 계산
   *
   * @return 전체 페이지 수
   */
  default int totalPages() {
    if (display() <= 0) {
      return 0;
    }
    return (int) Math.ceil((double) totalCount() / display());
  }
}
