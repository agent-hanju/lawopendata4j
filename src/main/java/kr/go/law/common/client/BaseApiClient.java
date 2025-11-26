package kr.go.law.common.client;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.request.BaseRequest;
import kr.go.law.common.request.PageableRequest;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * API 클라이언트 공통 베이스 클래스
 * <p>
 * HTTP 요청 실행, JSON 파싱, 예외 처리 등 공통 로직을 제공합니다.
 * </p>
 */
@Slf4j
public abstract class BaseApiClient {

  protected final LawOpenDataProperties properties;
  protected final ObjectMapper objectMapper;
  protected final OkHttpClient client;

  protected BaseApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.client = client;
  }

  /**
   * HTTP 요청 실행 및 응답 문자열 반환
   *
   * @param request HTTP 요청
   * @return 응답 문자열
   * @throws IOException 요청 실패 시
   */
  protected String executeRequest(Request request) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Response status=" + response.code());
      }
      return response.body() != null ? response.body().string() : null;
    }
  }

  /**
   * URL 빌드 헬퍼 메서드
   *
   * @param path API 경로
   * @param request 요청 객체
   * @return 빌드된 HttpUrl
   */
  protected HttpUrl buildUrl(String path, BaseRequest request) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(LawOpenDataProperties.BASE_URL + path).newBuilder();
    urlBuilder.addQueryParameter("OC", properties.getOc());
    request.toQueryParameters().forEach(urlBuilder::addQueryParameter);
    return urlBuilder.build();
  }

  /**
   * 목록 조회 API 공통 실행 로직
   *
   * @param <T> 항목 타입
   * @param request 페이지 가능한 요청
   * @param path API 경로
   * @param itemsParser 항목 목록 파싱 함수
   * @param totalCountParser 전체 건수 파싱 함수
   * @param apiName API 이름 (로깅용)
   * @return ListApiResult
   */
  protected <T> ListApiResult<T> executeListApi(
      PageableRequest request,
      String path,
      Function<JsonNode, java.util.List<T>> itemsParser,
      Function<JsonNode, Integer> totalCountParser,
      String apiName) {

    HttpUrl url = buildUrl(path, request);
    String responseString = null;

    try {
      responseString = executeRequest(new Request.Builder().url(url).get().build());

      final JsonNode result;
      if (responseString == null || responseString.isBlank()) {
        log.warn("Empty response body for {}: request={}, url={}", apiName, request, url);
        result = objectMapper.createObjectNode();
      } else {
        result = objectMapper.readTree(responseString);
        log.debug("{} API call successful: url={}, responseSize={}",
            apiName, url, responseString.length());
      }

      return new ListApiResult<>(
          responseString,
          itemsParser.apply(result),
          totalCountParser.apply(result),
          request.getPage() != null ? request.getPage() : 1,
          request.getDisplay() != null ? request.getDisplay() : 20);

    } catch (Exception e) {
      log.error("Failed to call {} API: url={}, error={}", apiName, url, e.getMessage());
      return ListApiResult.error(responseString);
    }
  }

  /**
   * 본문 조회 API 공통 실행 로직
   *
   * @param <T> 본문 타입
   * @param request 요청 객체
   * @param path API 경로
   * @param contentParser 본문 파싱 함수
   * @param apiName API 이름 (로깅용)
   * @return ContentApiResult
   */
  protected <T> ContentApiResult<T> executeContentApi(
      BaseRequest request,
      String path,
      Function<JsonNode, T> contentParser,
      String apiName) {

    HttpUrl url = buildUrl(path, request);
    String responseString = null;

    try {
      responseString = executeRequest(new Request.Builder().url(url).get().build());

      if (responseString == null || responseString.isBlank()) {
        log.warn("Empty response body for {}: request={}, url={}", apiName, request, url);
        return ContentApiResult.empty();
      }

      JsonNode result = objectMapper.readTree(responseString);
      log.debug("{} API call successful: url={}, responseSize={}",
          apiName, url, responseString.length());

      return ContentApiResult.of(responseString, contentParser.apply(result));

    } catch (Exception e) {
      log.error("Failed to call {} API: url={}, error={}", apiName, url, e.getMessage());
      return ContentApiResult.error(responseString);
    }
  }
}
