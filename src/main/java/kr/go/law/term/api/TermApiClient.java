package kr.go.law.term.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.term.dto.ArticleTermLinkDto;
import kr.go.law.term.dto.LawTermContentDto;
import kr.go.law.term.dto.LawTermDto;
import kr.go.law.term.dto.TermRelationDto;
import kr.go.law.term.request.ArticleTermLinkRequest;
import kr.go.law.term.request.TermContentRequest;
import kr.go.law.term.request.TermListRequest;
import kr.go.law.term.request.TermRelationRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * 법령용어 Open API 클라이언트
 * <p>
 * 법령용어 목록, 본문, 연계 조회 API를 호출합니다.
 * </p>
 */
@Slf4j
public class TermApiClient extends BaseApiClient {

  /**
   * TermApiClient 생성자
   * <p>
   * <strong>권장하지 않음:</strong> 직접 생성보다는 {@link kr.go.law.LawOpenDataClient}를
   * 사용하세요.
   * </p>
   *
   * @param properties   API 설정 프로퍼티
   * @param objectMapper Jackson ObjectMapper
   * @param client       OkHttp 클라이언트
   */
  public TermApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
  }

  // ===== Request Builder 기반 API 메서드 =====

  /**
   * 법령용어 목록 조회 (Request 빌더 사용)
   *
   * @param request 법령용어 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<LawTermDto> search(TermListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        this::parseTerms,
        result -> result.path("LsTrmSearch").path("totalCnt").asInt(0),
        "Term List");
  }

  /**
   * 법령용어 본문 조회 (Request 빌더 사용)
   *
   * @param request 법령용어 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<LawTermContentDto> getContent(TermContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseTermContent,
        "Term Content");
  }

  /**
   * 조문-법령용어 연계 조회 (Request 빌더 사용)
   *
   * @param request 조문-법령용어 연계 조회 요청
   * @return ContentApiResult&lt;ArticleTermLinkDto&gt;
   */
  public ContentApiResult<ArticleTermLinkDto> getArticleTermLink(ArticleTermLinkRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseArticleTermLink,
        "Article Term Link");
  }

  /**
   * 법령용어-일상용어 연계 조회 (Request 빌더 사용)
   *
   * @param request 법령용어-일상용어 연계 조회 요청
   * @return ContentApiResult&lt;TermRelationDto&gt;
   */
  public ContentApiResult<TermRelationDto> getTermRelation(TermRelationRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseTermRelation,
        "Term Relation");
  }

  private List<LawTermDto> parseTerms(JsonNode root) {
    List<LawTermDto> terms = new ArrayList<>();
    JsonNode searchNode = root.path("LsTrmSearch");
    JsonNode lstrmNode = searchNode.path("lstrm");

    if (!lstrmNode.isMissingNode() && !lstrmNode.isNull()) {
      try {
        if (lstrmNode.isArray()) {
          for (JsonNode node : lstrmNode) {
            terms.add(objectMapper.treeToValue(node, LawTermDto.class));
          }
        } else {
          terms.add(objectMapper.treeToValue(lstrmNode, LawTermDto.class));
        }
      } catch (Exception e) {
        log.error("Failed to parse terms", e);
      }
    }
    return terms;
  }

  private LawTermContentDto parseTermContent(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("LsTrmService");
      if (serviceNode.isMissingNode() || serviceNode.isNull()) {
        return null;
      }
      return objectMapper.treeToValue(serviceNode, LawTermContentDto.class);
    } catch (Exception e) {
      log.error("Failed to parse term content", e);
      return null;
    }
  }

  private ArticleTermLinkDto parseArticleTermLink(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("joRltLstrmService");
      JsonNode articleNode = serviceNode.path("법령조문");
      if (articleNode.isMissingNode() || articleNode.isNull()) {
        return null;
      }
      return objectMapper.treeToValue(articleNode, ArticleTermLinkDto.class);
    } catch (Exception e) {
      log.error("Failed to parse article term link", e);
      return null;
    }
  }

  private TermRelationDto parseTermRelation(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("lstrmRltService");
      JsonNode termNode = serviceNode.path("법령용어");
      if (termNode.isMissingNode() || termNode.isNull()) {
        return null;
      }
      return objectMapper.treeToValue(termNode, TermRelationDto.class);
    } catch (Exception e) {
      log.error("Failed to parse term relation", e);
      return null;
    }
  }
}
