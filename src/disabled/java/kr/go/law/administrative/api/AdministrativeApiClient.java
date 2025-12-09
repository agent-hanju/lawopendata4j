package kr.go.law.administrative.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.administrative.dto.AdministrativeAppealContentDto;
import kr.go.law.administrative.dto.AdministrativeAppealDto;
import kr.go.law.administrative.request.AdministrativeContentRequest;
import kr.go.law.administrative.request.AdministrativeListRequest;
import kr.go.law.common.client.BaseApiClient;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.util.JsonParserUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/** 행정심판례 Open 목록, 본문 조회 클라이언트 */
@Slf4j
public class AdministrativeApiClient extends BaseApiClient {

  /**
   * AdministrativeApiClient 생성자
   * <p>
   * <strong>권장하지 않음:</strong> 직접 생성보다는 {@link kr.go.law.LawOpenDataClient}를
   * 사용하세요.
   * </p>
   *
   * @param properties   API 설정 프로퍼티
   * @param objectMapper Jackson ObjectMapper
   * @param client       OkHttp 클라이언트
   */
  public AdministrativeApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
  }

  /**
   * 행정심판례 목록 조회
   *
   * @param request 행정심판례 목록 조회 요청
   * @return ListApiResult
   */
  public ListApiResult<AdministrativeAppealDto> search(AdministrativeListRequest request) {
    return executeListApi(
        request,
        LawOpenDataProperties.LIST_PATH,
        this::parseAppeals,
        result -> result.path("Decc").path("totalCnt").asInt(0),
        "Administrative Appeal List");
  }

  /**
   * 행정심판례 본문 조회
   *
   * @param request 행정심판례 본문 조회 요청
   * @return ContentApiResult
   */
  public ContentApiResult<AdministrativeAppealContentDto> getContent(AdministrativeContentRequest request) {
    return executeContentApi(
        request,
        LawOpenDataProperties.CONTENT_PATH,
        this::parseContent,
        "Administrative Appeal Content");
  }

  private List<AdministrativeAppealDto> parseAppeals(JsonNode root) {
    List<AdministrativeAppealDto> appeals = new ArrayList<>();
    JsonNode deccNode = root.path("Decc").path("decc");

    if (!deccNode.isMissingNode() && !deccNode.isNull()) {
      try {
        for (JsonNode node : JsonParserUtil.normalizeToArray(deccNode)) {
          appeals.add(new AdministrativeAppealDto(
              JsonParserUtil.getInt(node, "행정심판재결례일련번호"),
              JsonParserUtil.getString(node, "사건명"),
              JsonParserUtil.getString(node, "사건번호"),
              JsonParserUtil.getInt(node, "의결일자"),
              JsonParserUtil.getInt(node, "처분일자"),
              JsonParserUtil.getString(node, "재결청"),
              JsonParserUtil.getString(node, "처분청"),
              JsonParserUtil.getInt(node, "재결구분코드"),
              JsonParserUtil.getString(node, "재결구분명")));
        }
      } catch (Exception e) {
        log.error("Failed to parse appeals", e);
      }
    }
    return appeals;
  }

  private AdministrativeAppealContentDto parseContent(JsonNode root) {
    try {
      JsonNode serviceNode = root.path("PrecService");
      if (serviceNode.isMissingNode() || serviceNode.isNull()) {
        return null;
      }
      return new AdministrativeAppealContentDto(
          JsonParserUtil.getInt(serviceNode, "행정심판례일련번호"),
          JsonParserUtil.getString(serviceNode, "사건명"),
          JsonParserUtil.getString(serviceNode, "사건번호"),
          JsonParserUtil.getInt(serviceNode, "재결례유형코드"),
          JsonParserUtil.getString(serviceNode, "재결례유형명"),
          JsonParserUtil.getString(serviceNode, "재결청"),
          JsonParserUtil.getInt(serviceNode, "의결일자"),
          JsonParserUtil.getString(serviceNode, "처분청"),
          JsonParserUtil.getInt(serviceNode, "처분일자"),
          JsonParserUtil.getString(serviceNode, "청구취지"),
          JsonParserUtil.getString(serviceNode, "재결요지"),
          JsonParserUtil.getString(serviceNode, "주문"),
          JsonParserUtil.getString(serviceNode, "이유"));
    } catch (Exception e) {
      log.error("Failed to parse content", e);
      return null;
    }
  }
}
