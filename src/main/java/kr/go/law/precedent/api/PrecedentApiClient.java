package kr.go.law.precedent.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.precedent.dto.PrecedentDto;
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

  private final PrecedentParser parser;

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
    this.parser = new PrecedentParser(objectMapper);
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
  public ListApiResult<PrecedentDto> search(PrecedentListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        parser::parseList,
        parser::parseTotalCount,
        "Precedent List");
  }

  /**
   * 판례 본문 조회
   *
   * <pre>
   * 사용 예시:
   * {@code
   * PrecedentContentRequest request = PrecedentContentRequest.builder()
   *     .precedentSerialNumber(608799)
   *     .build();
   *
   * ContentApiResult result = client.getContent(request);
   * }
   * </pre>
   *
   * @param request 판례 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<PrecedentDto> getContent(PrecedentContentRequest request) {
    try {
      ContentApiResult<PrecedentDto> result = executeContentApi(
          request,
          LawOpenDataProperties.CONTENT_PATH,
          r -> parser.parseContent(r, request.getId()),
          "Precedent Content");

      // 정상적이지 않은 응답이면 fallback으로 이어감
      if (result.content().isEmpty()) {
        log.info("Empty content from API, falling back to HTML scraping: precId={}",
            request.getId());
        return getContentByFallback(request.getId());
      }

      return result;
    } catch (Exception e) {
      log.info("API call failed, falling back to HTML scraping: precId={}, error={}",
          request.getId(), e.getMessage());
      return getContentByFallback(request.getId());
    }
  }

  /**
   * 판례 본문 조회 - Fallback API 직접 호출 (HTML 스크래핑)
   *
   * <pre>
   * 사용 예시:
   * {@code
   * ContentApiResult result = client.getContentByFallback(608799);
   * }
   * </pre>
   *
   * @param precId 판례 일련번호
   * @return ContentApiResult
   */
  public ContentApiResult<PrecedentDto> getContentByFallback(Integer precId) {
    HttpUrl url = HttpUrl.parse(FALLBACK_URL)
        .newBuilder()
        .addQueryParameter("precSeq", String.valueOf(precId))
        .addQueryParameter("mode", "print")
        .build();

    Request request = new Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0")
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      int code = response.code();

      // 리다이렉트 체크(NTS 여부)
      if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
        String redirectUrl = response.header("Location");
        String ntsDcmId;
        if (redirectUrl == null || !redirectUrl.contains("ntstDcmId=")) {
          ntsDcmId = null;
        } else {
          HttpUrl url2 = HttpUrl.parse(redirectUrl);
          ntsDcmId = url2 != null ? url2.queryParameter("ntstDcmId") : null;
        }
        return callNtsApi(ntsDcmId);
      } else {
        // HTML 파싱
        String responseBody = response.body() != null ? response.body().string() : "";
        PrecedentDto dto = PrecedentHtmlParser.parseHtmlContent(responseBody);
        return ContentApiResult.of(responseBody, dto);
      }
    } catch (IOException e) {
      log.error("Failed to call Fallback API for precId={}, url={}: {}", precId, url, e.getMessage());
      return ContentApiResult.error(null);
    }
  }

  /**
   * NTS API 호출 (국세법령정보시스템)
   *
   * @param ntsDcmId NTS 문서 ID
   * @return ContentApiResult
   */
  private ContentApiResult<PrecedentDto> callNtsApi(String ntsDcmId) throws IOException {
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
        .header("User-Agent", "Mozilla/5.0")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build();

    String ntsResult = executeRequest(request);
    PrecedentDto dto = PrecedentNtsParser.parseNtsContent(objectMapper.readTree(ntsResult));
    return ContentApiResult.of(ntsResult, dto);
  }

}
