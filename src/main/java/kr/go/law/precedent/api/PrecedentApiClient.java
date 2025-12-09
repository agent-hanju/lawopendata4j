package kr.go.law.precedent.api;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.precedent.dto.PrecedentContentDto;
import kr.go.law.precedent.dto.PrecedentListDto;
import kr.go.law.precedent.parser.PrecedentHtmlParser;
import kr.go.law.precedent.parser.PrecedentComwelParser;
import kr.go.law.precedent.parser.PrecedentParserFactory;
import kr.go.law.precedent.request.PrecedentContentRequest;
import kr.go.law.precedent.request.PrecedentListRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** 판례 목록, 본문 조회 API를 호출 클라이언트 */
@Slf4j
public class PrecedentApiClient extends BaseApiClient {

  private static final String FALLBACK_URL = "https://www.law.go.kr/LSW/precInfoP.do";
  private static final String NTS_URL = "https://taxlaw.nts.go.kr/action.do";

  // 데이터 출처명 상수
  private static final String DATA_SOURCE_NTS = "국세법령정보시스템";
  private static final String DATA_SOURCE_COMWEL = "근로복지공단산재판례";

  // HTTP 헤더 상수
  private static final String HEADER_USER_AGENT = "User-Agent";
  private static final String USER_AGENT_VALUE = "Mozilla/5.0";

  private final PrecedentParserFactory parserFactory;

  /**
   * <strong>권장하지 않음:</strong> 직접 생성보다는 {@link kr.go.law.LawOpenDataClient}를
   * 사용하세요.
   *
   * @param properties   API 설정 프로퍼티
   * @param objectMapper Jackson ObjectMapper
   * @param client       OkHttp 클라이언트
   */
  public PrecedentApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
    this.parserFactory = new PrecedentParserFactory(objectMapper);
  }

  /**
   * 판례 목록 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * PrecedentListRequest request = PrecedentListRequest.builder()
   *     .page(1)
   *     .display(100)
   *     .sort(PrecedentSortOption.DECISION_DATE_DESC)
   *     .query("손해배상")
   *     .decisionDateFrom(20240101)
   *     .decisionDateTo(20241231)
   *     .build();
   *
   * ListApiResult result = client.search(request);
   * }
   * </pre>
   *
   * @param request 판례 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<PrecedentListDto> search(PrecedentListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        parserFactory.getPrecedentListParser()::parseList,
        parserFactory.getPrecedentListParser()::parseTotalCount,
        "Precedent List");
  }

  /**
   * 판례 본문 조회 (기본 API만 사용, dataSource 무시)
   *
   * @param request 판례 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<PrecedentContentDto> getContent(PrecedentContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        parserFactory.getPrecedentContentParser()::parseContent,
        "Precedent Content");
  }

  /**
   * 판례 본문 조회 (dataSource 기반 분기)
   *
   * @param precId     판례 일련번호
   * @param dataSource 데이터 출처명 (목록 API에서 전달받음)
   * @return ContentApiResult
   */
  public ContentApiResult<PrecedentContentDto> getContent(Integer precId, String dataSource) {
    if (DATA_SOURCE_NTS.equals(dataSource)) {
      return getContentFromNts(precId);
    } else if (DATA_SOURCE_COMWEL.equals(dataSource)) {
      return getContentFromComwel(precId);
    } else {
      return getContentWithFallback(precId);
    }
  }

  /**
   * 기본 API 호출 후 실패 시 fallback
   */
  private ContentApiResult<PrecedentContentDto> getContentWithFallback(Integer precId) {
    PrecedentContentRequest request = PrecedentContentRequest.builder()
        .id(precId)
        .build();

    ContentApiResult<PrecedentContentDto> result = getContent(request);

    if (result.content().isEmpty()) {
      log.info("Empty content from API, falling back to HTML scraping: precId={}", precId);
      return getContentFromFallback(precId);
    }

    return result;
  }

  /**
   * NTS(국세법령정보시스템) 본문 조회
   */
  private ContentApiResult<PrecedentContentDto> getContentFromNts(Integer precId) {
    log.info("NTS dataSource detected, using fallback flow: precId={}", precId);
    return getContentFromFallback(precId);
  }

  /**
   * COMWEL(근로복지공단산재판례) 본문 조회
   */
  private ContentApiResult<PrecedentContentDto> getContentFromComwel(Integer precId) {
    ContentApiResult<PrecedentContentDto> result = getContentWithFallback(precId);

    if (result.content().isEmpty()) {
      return result;
    }

    PrecedentContentDto dto = result.content().get();
    String caseNumber = dto.getCaseNumber();
    String courtName = dto.getCourtName();

    if (caseNumber == null || courtName == null) {
      return result;
    }

    // COMWEL 사이트에서 메타데이터 조회 후 보완
    try {
      String html = callComwelApi(caseNumber, courtName);
      if (html != null) {
        Map<String, String> metadata = PrecedentComwelParser.parseMetadata(html);
        PrecedentComwelParser.mergeMetadata(dto, metadata);
        log.debug("COMWEL metadata merged: precId={}", dto.getPrecId());
      }
    } catch (Exception e) {
      log.warn("Failed to supplement from COMWEL: precId={}, error={}", precId, e.getMessage());
    }

    return result;
  }

  /**
   * Fallback 경로로 본문 조회 (HTML 또는 NTS 리다이렉트)
   */
  private ContentApiResult<PrecedentContentDto> getContentFromFallback(Integer precId) {
    try {
      FallbackResponse fallback = callFallbackApi(precId);

      if (fallback.ntsDcmId != null) {
        // NTS 리다이렉트
        String ntsResponse = callNtsApi(fallback.ntsDcmId);
        PrecedentContentDto dto = parserFactory.getPrecedentNtsParser()
            .parse(objectMapper.readTree(ntsResponse));
        return ContentApiResult.of(ntsResponse, dto);
      } else {
        // HTML 파싱
        PrecedentContentDto dto = PrecedentHtmlParser.parseHtmlContent(fallback.html);
        return ContentApiResult.of(fallback.html, dto);
      }
    } catch (IOException e) {
      log.error("Failed to get content from fallback: precId={}, error={}", precId, e.getMessage());
      return ContentApiResult.error(null);
    }
  }

  // ========== HTTP API 호출 메서드 ==========

  /**
   * Fallback API 호출 (law.go.kr HTML 페이지)
   *
   * @return FallbackResponse (HTML 또는 NTS 리다이렉트 정보)
   */
  private FallbackResponse callFallbackApi(Integer precId) throws IOException {
    HttpUrl url = HttpUrl.parse(FALLBACK_URL)
        .newBuilder()
        .addQueryParameter("precSeq", String.valueOf(precId))
        .addQueryParameter("mode", "print")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .header(HEADER_USER_AGENT, USER_AGENT_VALUE)
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      int code = response.code();

      // 리다이렉트 체크 (NTS)
      if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
        String redirectUrl = response.header("Location");
        String ntsDcmId = null;
        if (redirectUrl != null && redirectUrl.contains("ntstDcmId=")) {
          HttpUrl parsed = HttpUrl.parse(redirectUrl);
          ntsDcmId = parsed != null ? parsed.queryParameter("ntstDcmId") : null;
        }
        return new FallbackResponse(null, ntsDcmId);
      } else {
        String html = response.body() != null ? response.body().string() : "";
        return new FallbackResponse(html, null);
      }
    }
  }

  /**
   * NTS API 호출 (국세법령정보시스템)
   *
   * @param ntsDcmId NTS 문서 ID
   * @return JSON 응답 문자열
   */
  private String callNtsApi(String ntsDcmId) throws IOException {
    ObjectNode dcmDVO = objectMapper.createObjectNode();
    dcmDVO.put("ntstDcmId", ntsDcmId);
    ObjectNode paramDataJson = objectMapper.createObjectNode();
    paramDataJson.set("dcmDVO", dcmDVO);

    RequestBody formBody = new FormBody.Builder()
        .add("actionId", "ASIQTB002PR01")
        .add("paramData", paramDataJson.toString())
        .build();

    Request request = new Request.Builder()
        .url(NTS_URL)
        .post(formBody)
        .header(HEADER_USER_AGENT, USER_AGENT_VALUE)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build();

    return executeRequest(request);
  }

  /**
   * COMWEL API 호출 (근로복지공단 산재판례)
   *
   * @param caseNumber 사건번호
   * @param courtName  법원명
   * @return HTML 응답 문자열 (실패 시 null)
   */
  private String callComwelApi(String caseNumber, String courtName) throws IOException {
    String url = PrecedentComwelParser.buildUrl(caseNumber, courtName);
    if (url == null) {
      return null;
    }

    Request request = new Request.Builder()
        .url(url)
        .header(HEADER_USER_AGENT, USER_AGENT_VALUE)
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.warn("COMWEL request failed: url={}, code={}", url, response.code());
        return null;
      }
      return response.body() != null ? response.body().string() : null;
    }
  }

  /** Fallback API 응답 래퍼 */
  private record FallbackResponse(String html, String ntsDcmId) {}

}
