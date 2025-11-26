package kr.go.law.config;

import java.time.Duration;

import lombok.Builder;
import lombok.Getter;

/** Law Open Data API 클라이언트 설정 */
@Builder
@Getter
public class LawOpenDataProperties {
  public static final String BASE_URL = "http://www.law.go.kr/DRF";
  public static final String LIST_PATH = "/lawSearch.do";
  public static final String CONTENT_PATH = "/lawService.do";

  private final String oc;

  @Builder.Default
  private final Duration connectionTimeout = Duration.ofSeconds(10);
  @Builder.Default
  private final Duration readTimeout = Duration.ofSeconds(30);
  @Builder.Default
  private final int maxRetries = 3;
  @Builder.Default
  private final Duration retryDelay = Duration.ofSeconds(1);
  @Builder.Default
  private final int maxConnection = 5;
  @Builder.Default
  private final Duration keepAliveDuration = Duration.ofMinutes(5);

}
