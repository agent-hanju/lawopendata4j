package kr.go.law.precedent.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.precedent.dto.PrecedentContentDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 판례 본문 API 응답 파서
 */
@Slf4j
public class PrecedentContentParser extends BaseParser<PrecedentContentDto> {
  private static final String ROOT_FIELD = "PrecService";

  // 원본 API 순서
  private static final String SUMMARY = "판시사항";
  private static final String PRECEDENT_REFERENCES = "참조판례";
  private static final String CASE_TYPE_NAME = "사건종류명";
  private static final String DECISION_SUMMARY = "판결요지";
  private static final String ARTICLE_REFERENCES = "참조조문";
  private static final String DECISION_DATE = "선고일자";
  private static final String COURT_NAME = "법원명";
  private static final String CASE_NAME = "사건명";
  private static final String CONTENT = "판례내용";
  private static final String CASE_NUMBER = "사건번호";
  private static final String CASE_TYPE_CODE = "사건종류코드";
  private static final String PREC_ID = "판례정보일련번호";
  private static final String DECISION = "선고";
  private static final String DECISION_TYPE = "판결유형";
  private static final String COURT_CODE = "법원종류코드";

  private static final Set<String> KNOWN_FIELDS = Set.of(
      SUMMARY, PRECEDENT_REFERENCES, CASE_TYPE_NAME, DECISION_SUMMARY, ARTICLE_REFERENCES,
      DECISION_DATE, COURT_NAME, CASE_NAME, CONTENT, CASE_NUMBER, CASE_TYPE_CODE,
      PREC_ID, DECISION, DECISION_TYPE, COURT_CODE);

  public PrecedentContentParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  /**
   * Content API 응답을 파싱하여 PrecedentContentDto를 반환합니다.
   *
   * @param contentApiResponse Content API 응답
   * @return PrecedentContentDto (응답이 유효하지 않으면 null)
   */
  public PrecedentContentDto parseContent(JsonNode contentApiResponse) {
    if (contentApiResponse == null) {
      return null;
    }

    if (!contentApiResponse.has(ROOT_FIELD)) {
      log.warn("No {} in content response", ROOT_FIELD);
      return null;
    }

    JsonNode contentNode = contentApiResponse.get(ROOT_FIELD);
    return parse(contentNode);
  }

  /** 개별 판례 본문 데이터 파싱 */
  @Override
  public PrecedentContentDto parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return PrecedentContentDto.builder()
        .summary(getString(node, SUMMARY, onTypeMismatch))
        .precedentReferences(getString(node, PRECEDENT_REFERENCES, onTypeMismatch))
        .caseTypeName(getString(node, CASE_TYPE_NAME, onTypeMismatch))
        .decisionSummary(getString(node, DECISION_SUMMARY, onTypeMismatch))
        .articleReferences(getString(node, ARTICLE_REFERENCES, onTypeMismatch))
        .decisionDate(getInt(node, DECISION_DATE, onTypeMismatch))
        .courtName(getString(node, COURT_NAME, onTypeMismatch))
        .caseName(getString(node, CASE_NAME, onTypeMismatch))
        .content(getString(node, CONTENT, onTypeMismatch))
        .caseNumber(getString(node, CASE_NUMBER, onTypeMismatch))
        .caseTypeCode(getString(node, CASE_TYPE_CODE, onTypeMismatch))
        .precId(getInt(node, PREC_ID, onTypeMismatch))
        .decision(getString(node, DECISION, onTypeMismatch))
        .decisionType(getString(node, DECISION_TYPE, onTypeMismatch))
        .courtCode(getString(node, COURT_CODE, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }
}
