package kr.go.law.common.response;

import java.util.Optional;

/**
 * 본문 조회 API 공통 응답
 *
 * @param <T> 본문 타입
 */
public record ContentApiResult<T>(
        String rawData,
        Optional<T> content) {

    /**
     * 빈 결과 생성
     */
    public static <T> ContentApiResult<T> empty() {
        return new ContentApiResult<>(null, Optional.empty());
    }

    /**
     * 에러 결과 생성 (rawData만 포함)
     */
    public static <T> ContentApiResult<T> error(String rawData) {
        return new ContentApiResult<>(rawData, Optional.empty());
    }

    /**
     * 성공 결과 생성
     */
    public static <T> ContentApiResult<T> of(String rawData, T content) {
        return new ContentApiResult<>(rawData, Optional.ofNullable(content));
    }
}
