package kr.go.law.constitutional.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.constitutional.dto.ConstitutionalDecisionContentDto;
import kr.go.law.constitutional.dto.ConstitutionalDecisionDto;
import kr.go.law.constitutional.request.ConstitutionalContentRequest;
import kr.go.law.constitutional.request.ConstitutionalListRequest;
import kr.go.law.util.JsonParserUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * 헌재결정례 Open API 클라이언트
 * <p>
 * 헌재결정례 목록, 본문 조회 API를 호출합니다.
 * </p>
 */
@Slf4j
public class ConstitutionalApiClient extends BaseApiClient {

  public ConstitutionalApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
  }

  // ===== Request Builder 기반 API 메서드 =====

  /**
   * 헌재결정례 목록 조회 (Request 빌더 사용)
   *
   * @param request 헌재결정례 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<ConstitutionalDecisionDto> search(ConstitutionalListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        this::parseDecisions,
        result -> result.path("DetcSearch").path("totalCnt").asInt(0),
        "Constitutional Decision List");
  }

  /**
   * 헌재결정례 본문 조회 (Request 빌더 사용)
   *
   * @param request 헌재결정례 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<ConstitutionalDecisionContentDto> getContent(ConstitutionalContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseContent,
        "Constitutional Decision Content");
  }

  private List<ConstitutionalDecisionDto> parseDecisions(JsonNode root) {
    List<ConstitutionalDecisionDto> decisions = new ArrayList<>();
    JsonNode detcSearchNode = root.path("DetcSearch");
    JsonNode detcNode = detcSearchNode.path("Detc");

    log.info("=== Constitutional Parser Debug ===");
    log.info("Root keys: {}", root.fieldNames().hasNext() ? root.fieldNames().next() : "none");
    log.info("DetcSearch isMissing: {}, isNull: {}", detcSearchNode.isMissingNode(), detcSearchNode.isNull());
    log.info("Detc isMissing: {}, isNull: {}, isArray: {}, isObject: {}",
        detcNode.isMissingNode(), detcNode.isNull(), detcNode.isArray(), detcNode.isObject());

    if (!detcNode.isMissingNode() && !detcNode.isNull()) {
      try {
        // normalizeToArray를 사용하여 단일 객체 또는 배열을 배열로 변환
        var arrayNode = JsonParserUtil.normalizeToArray(detcNode);
        log.info("After normalizeToArray: size={}", arrayNode.size());
        for (JsonNode node : arrayNode) {
          try {
            ConstitutionalDecisionDto dto = objectMapper.treeToValue(node, ConstitutionalDecisionDto.class);
            decisions.add(dto);
            log.debug("Successfully parsed decision: {}", dto.getDecisionSerialNumber());
          } catch (Exception e) {
            log.error("Failed to parse single decision node: {}, error: {}", node, e.getMessage(), e);
          }
        }
      } catch (Exception e) {
        log.error("Failed to parse decisions array", e);
      }
    }
    log.info("Parsed {} decisions", decisions.size());
    return decisions;
  }

  private ConstitutionalDecisionContentDto parseContent(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("PrecService");
      if (serviceNode.isMissingNode() || serviceNode.isNull()) {
        return null;
      }
      return objectMapper.treeToValue(serviceNode, ConstitutionalDecisionContentDto.class);
    } catch (Exception e) {
      log.error("Failed to parse content", e);
      return null;
    }
  }
}
