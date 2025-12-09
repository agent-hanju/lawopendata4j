package kr.go.law.statute.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.statute.dto.StatuteContentDto;
import kr.go.law.statute.dto.StatuteHistoryDto;
import kr.go.law.statute.dto.StatuteListDto;
import kr.go.law.statute.parser.StatuteParserFactory;
import kr.go.law.statute.request.EfYdLawContentRequest;
import kr.go.law.statute.request.StatuteContentRequest;
import kr.go.law.statute.request.StatuteHistoryRequest;
import kr.go.law.statute.request.StatuteListRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/** 법령 목록, 연혁, 본문 조회 API 호출 클라이언트 */
@Slf4j
public class StatuteApiClient extends BaseApiClient {

  private final StatuteParserFactory parserFactory;

  /**
   * <strong>권장하지 않음:</strong> 직접 생성보다는 {@link kr.go.law.LawOpenDataClient}를
   * 사용하세요.
   *
   * @param properties   API 설정 프로퍼티
   * @param objectMapper Jackson ObjectMapper
   * @param client       OkHttp 클라이언트
   */
  public StatuteApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
    this.parserFactory = new StatuteParserFactory(objectMapper);
  }

  /**
   * 법령 목록 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * StatuteListRequest request = StatuteListRequest.builder()
   *     .page(1)
   *     .display(100)
   *     .sort(StatuteSortOption.PROMULGATION_DATE_DESC)
   *     .query("개인정보")
   *     .promulgationDateFrom(20240101)
   *     .promulgationDateTo(20241231)
   *     .build();
   *
   * ListApiResult result = client.search(request);
   * }
   * </pre>
   *
   * @param request 법령 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<StatuteListDto> search(StatuteListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        parserFactory.getStatuteListParser()::parseList,
        result -> parserFactory.getStatuteListParser().parseTotalCount(result),
        "Statute List");
  }

  /**
   * 조문 개정 이력 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * StatuteHistoryRequest request = StatuteHistoryRequest.builder()
   *     .revisionDate(20240101)
   *     .lawId(2132)
   *     .build();
   *
   * ListApiResult<StatuteHistoryDto> result = client.searchHistory(request);
   * }
   * </pre>
   *
   * @param request 조문 개정 이력 조회 요청
   * @return ListApiResult&lt;StatuteHistoryDto&gt;
   */
  public ListApiResult<StatuteHistoryDto> searchHistory(StatuteHistoryRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        parserFactory.getStatuteHistoryParser()::parseList,
        result -> parserFactory.getStatuteHistoryParser().parseTotalCount(result),
        "Statute History");
  }

  /**
   * 법령 본문 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * StatuteContentRequest request = StatuteContentRequest.builder()
   *     .mst(253527)
   *     .build();
   *
   * ContentApiResult result = client.getContent(request);
   * }
   * </pre>
   *
   * @param request 법령 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<StatuteContentDto> getContent(StatuteContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        parserFactory.getStatuteContentParser()::parse,
        "Statute Content");
  }

  /**
   * 시행일자 기준 법령 본문 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * EfYdLawContentRequest request = EfYdLawContentRequest.builder()
   *     .mst(253527)
   *     .efYd(20240101)
   *     .build();
   *
   * ContentApiResult result = client.getContentByEfYd(request);
   * }
   * </pre>
   *
   * @param request 시행일자 기준 법령 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<StatuteContentDto> getContentByEfYd(EfYdLawContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        parserFactory.getStatuteContentParser()::parse,
        "Statute Content (EfYd)");
  }
}
