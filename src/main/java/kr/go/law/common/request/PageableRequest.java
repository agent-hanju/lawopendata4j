package kr.go.law.common.request;

/**
 * 페이지네이션 지원 요청 인터페이스
 * <p>
 * 목록 조회 API에서 페이지네이션을 지원하는 요청이 구현합니다.
 * </p>
 */
public interface PageableRequest extends BaseRequest {

    /**
     * 페이지 번호 반환 (1-based)
     *
     * @return 페이지 번호, null이면 기본값 1
     */
    Integer getPage();

    /**
     * 페이지당 표시 건수 반환
     *
     * @return 표시 건수, null이면 기본값 20 (최대 100)
     */
    Integer getDisplay();
}
