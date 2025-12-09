package kr.go.law.precedent.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.common.parser.TypeMismatchException;
import kr.go.law.precedent.dto.PrecedentListDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 판례 목록 API 응답 파서
 */
@Slf4j
public class PrecedentListParser extends BaseParser<PrecedentListDto> {
  private static final String ROOT_FIELD = "PrecSearch";
  private static final String ITEMS_FIELD = "prec";
  private static final String TOTAL_CNT = "totalCnt";

  private static final String CASE_NUMBER = "사건번호";

  public PrecedentListParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  private static final String DATA_SOURCE = "데이터출처명";
  private static final String CASE_TYPE_CODE = "사건종류코드";
  private static final String CASE_TYPE_NAME = "사건종류명";
  private static final String DECISION = "선고";
  private static final String DECISION_DATE = "선고일자";
  private static final String PREC_ID = "판례일련번호";
  private static final String DECISION_TYPE = "판결유형";
  private static final String COURT_CODE = "법원종류코드";
  private static final String COURT_NAME = "법원명";
  private static final String CASE_NAME = "사건명";

  private static final Set<String> KNOWN_FIELDS = Set.of(
      "id",
      CASE_NUMBER,
      DATA_SOURCE,
      CASE_TYPE_CODE,
      CASE_TYPE_NAME,
      DECISION,
      DECISION_DATE,
      PREC_ID,
      DECISION_TYPE,
      COURT_CODE,
      COURT_NAME,
      "판례상세링크",
      CASE_NAME);

  /**
   * List API 응답에서 PrecedentListDto 목록을 파싱합니다.
   *
   * @param listApiResponse List API 응답
   * @return List<PrecedentListDto>
   */
  public List<PrecedentListDto> parseList(final JsonNode listApiResponse) {
    if (listApiResponse == null || !listApiResponse.has(ROOT_FIELD)) {
      log.warn("Invalid list response");
      return Collections.emptyList();
    }

    final JsonNode rootNode = listApiResponse.get(ROOT_FIELD);
    if (!rootNode.has(ITEMS_FIELD)) {
      log.debug("No data in response");
      return Collections.emptyList();
    }

    final ArrayNode items;
    try {
      items = normalizeToArray(rootNode, ITEMS_FIELD);
    } catch (TypeMismatchException e) {
      log.warn("Invalid list response");
      return Collections.emptyList();
    }

    final List<PrecedentListDto> precedents = new ArrayList<>();
    for (JsonNode itemData : items) {
      PrecedentListDto dto = parse(itemData);
      if (dto != null) {
        precedents.add(dto);
      }
    }

    return precedents;
  }

  /**
   * API 응답에서 totalCnt를 파싱합니다.
   *
   * @param listApiResponse List API 응답
   * @return 파싱된 totalCnt. totalCnt가 없거나 유효한 integer가 아닐 경우 -1
   */
  public int parseTotalCount(final JsonNode listApiResponse) {
    if (listApiResponse == null || !listApiResponse.has(ROOT_FIELD)) {
      log.warn("Invalid list response");
      return -1;
    }

    final JsonNode rootNode = listApiResponse.get(ROOT_FIELD);
    final Integer totalCnt = getInt(rootNode, TOTAL_CNT,
        (fieldName, jsonNode) -> log.warn("{} is not integer: {}", fieldName, jsonNode));
    if (totalCnt == null) {
      return -1;
    }
    return totalCnt;
  }

  /** 개별 판례 데이터 파싱 */
  @Override
  public PrecedentListDto parse(final JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return PrecedentListDto.builder()
        .caseNumber(getString(node, CASE_NUMBER, onTypeMismatch))
        .dataSource(getString(node, DATA_SOURCE, onTypeMismatch))
        .caseTypeCode(getString(node, CASE_TYPE_CODE, onTypeMismatch))
        .caseTypeName(getString(node, CASE_TYPE_NAME, onTypeMismatch))
        .decision(getString(node, DECISION, onTypeMismatch))
        .decisionDate(getDateInString(node, DECISION_DATE, onTypeMismatch))
        .precId(getInt(node, PREC_ID, onTypeMismatch))
        .decisionType(getString(node, DECISION_TYPE, onTypeMismatch))
        .courtCode(getString(node, COURT_CODE, onTypeMismatch))
        .courtName(getString(node, COURT_NAME, onTypeMismatch))
        .caseName(getString(node, CASE_NAME, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }
}
