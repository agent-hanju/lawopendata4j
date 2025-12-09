package kr.go.law.interpretation.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.interpretation.dto.LegalInterpretationContentDto;
import kr.go.law.util.JsonParserUtil;
import kr.go.law.interpretation.dto.LegalInterpretationDto;
import kr.go.law.interpretation.request.InterpretationContentRequest;
import kr.go.law.interpretation.request.InterpretationListRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * 법령해석례 Open API 클라이언트
 * <p>
 * 법령해석례 목록, 본문 조회 API를 호출합니다.
 * </p>
 */
@Slf4j
public class InterpretationApiClient extends BaseApiClient {

  public InterpretationApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
  }

  // ===== Request Builder 기반 API 메서드 =====

  /**
   * 법령해석례 목록 조회 (Request 빌더 사용)
   *
   * @param request 법령해석례 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<LegalInterpretationDto> search(InterpretationListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        this::parseInterpretations,
        result -> result.path("Expc").path("totalCnt").asInt(0),
        "Legal Interpretation List");
  }

  /**
   * 법령해석례 본문 조회 (Request 빌더 사용)
   *
   * @param request 법령해석례 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<LegalInterpretationContentDto> getContent(InterpretationContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseContent,
        "Legal Interpretation Content");
  }

  private List<LegalInterpretationDto> parseInterpretations(JsonNode root) {
    List<LegalInterpretationDto> interpretations = new ArrayList<>();
    JsonNode expcNode = root.path("Expc").path("expc");

    if (!expcNode.isMissingNode() && !expcNode.isNull()) {
      try {
        // normalizeToArray를 사용하여 단일 객체 또는 배열을 배열로 변환
        for (JsonNode node : JsonParserUtil.normalizeToArray(expcNode)) {
          interpretations.add(objectMapper.treeToValue(node, LegalInterpretationDto.class));
        }
      } catch (Exception e) {
        log.error("Failed to parse interpretations", e);
      }
    }
    return interpretations;
  }

  private LegalInterpretationContentDto parseContent(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("PrecService");
      if (serviceNode.isMissingNode() || serviceNode.isNull()) {
        return null;
      }
      return objectMapper.treeToValue(serviceNode, LegalInterpretationContentDto.class);
    } catch (Exception e) {
      log.error("Failed to parse content", e);
      return null;
    }
  }
}
