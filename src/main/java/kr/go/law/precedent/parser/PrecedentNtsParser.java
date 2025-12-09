package kr.go.law.precedent.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.precedent.dto.PrecedentContentDto;
import kr.go.law.util.HtmlParserUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 국세법령정보시스템(NTS) API 응답 파서
 */
@Slf4j
public class PrecedentNtsParser extends BaseParser<PrecedentContentDto> {

  // NTS API 구조 필드 (API 고유 구조)
  private static final String STATUS = "status";
  private static final String DATA = "data";
  private static final String ACTION_DATA = "ASIQTB002PR01";
  private static final String DCM_DVO = "dcmDVO";
  private static final String DCM_HWP_EDITOR_DVO_LIST = "dcmHwpEditorDVOList";

  // dcmDVO 내 필드 - DTO 필드명과 매칭
  private static final String CASE_NAME = "ntstDcmTtl";
  private static final String CASE_NUMBER = "ntstDcmDscmCntn";
  private static final String SUMMARY = "ntstDcmGistCntn";
  private static final String CONTENT = "ntstDcmCntn";
  private static final String DECISION_SUMMARY = "ntstDcmMatrCntn";
  private static final String DECISION_DATE = "ntstDcmRgtDt";
  private static final String ARTICLE_REFERENCES = "ntstDcmRelLgltCntn";
  private static final String FILE_TYPE = "dcmFleTy";
  private static final String FILE_CONTENT = "dcmFleByte";

  private static final Set<String> KNOWN_FIELDS = Set.of(
      CASE_NAME,
      CASE_NUMBER,
      SUMMARY,
      CONTENT,
      DECISION_SUMMARY,
      DECISION_DATE,
      ARTICLE_REFERENCES,
      FILE_TYPE,
      FILE_CONTENT);

  public PrecedentNtsParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public PrecedentContentDto parse(JsonNode ntsResponse) {
    if (ntsResponse == null || ntsResponse.isMissingNode()) {
      return null;
    }

    // 구조 필드 검증 - 없으면 수집 대상이 아님
    if (!ntsResponse.has(STATUS) || !"SUCCESS".equals(ntsResponse.get(STATUS).asText())) {
      log.warn("NTS API returned non-success status");
      return null;
    }

    if (!ntsResponse.has(DATA) || !ntsResponse.get(DATA).isObject()) {
      log.warn("NTS API response missing 'data' field");
      return null;
    }
    JsonNode data = ntsResponse.get(DATA);

    if (!data.has(ACTION_DATA) || !data.get(ACTION_DATA).isObject()) {
      log.warn("NTS API response missing '{}' field", ACTION_DATA);
      return null;
    }
    JsonNode actionData = data.get(ACTION_DATA);

    if (!actionData.has(DCM_DVO) || !actionData.get(DCM_DVO).isObject()) {
      log.warn("NTS API response missing '{}' field", DCM_DVO);
      return null;
    }
    JsonNode dcmDVO = actionData.get(DCM_DVO);

    // 수집 대상 내에서 예상치 못한 필드 추적
    final Map<String, String> unexpected = new HashMap<>();
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);
    trackUnexpectedFields(dcmDVO, KNOWN_FIELDS, unexpected);

    // HTML 콘텐츠 추출 (content가 placeholder인 경우 대비)
    String htmlContent = extractHtmlContent(actionData);

    return PrecedentContentDto.builder()
        .caseName(getString(dcmDVO, CASE_NAME, onTypeMismatch))
        .caseNumber(getString(dcmDVO, CASE_NUMBER, onTypeMismatch))
        .summary(getString(dcmDVO, SUMMARY, onTypeMismatch))
        .content(parseContent(dcmDVO, htmlContent, onTypeMismatch))
        .decisionSummary(getString(dcmDVO, DECISION_SUMMARY, onTypeMismatch))
        .decisionDate(getInt(dcmDVO, DECISION_DATE, onTypeMismatch))
        .articleReferences(getString(dcmDVO, ARTICLE_REFERENCES, onTypeMismatch))
        .unexpected(unexpected.isEmpty() ? null : unexpected)
        .build();
  }

  private String parseContent(JsonNode dcmDVO, String htmlContent, BiConsumer<String, JsonNode> onTypeMismatch) {
    String content = getString(dcmDVO, CONTENT, onTypeMismatch);
    if (content != null) {
      content = HtmlParserUtil.cleanHtml(content);
    }

    // content가 placeholder이거나 비어있으면 HTML에서 추출 시도
    boolean needsHtmlFallback = content == null || content.trim().isEmpty()
        || content.contains("붙임과 같습니다") || content.contains("이하참조");
    if (needsHtmlFallback && htmlContent != null && !htmlContent.isEmpty()) {
      return HtmlParserUtil.cleanHtml(htmlContent);
    }

    return content;
  }

  /**
   * dcmHwpEditorDVOList에서 HTML 콘텐츠 추출
   */
  private String extractHtmlContent(JsonNode actionData) {
    if (!actionData.has(DCM_HWP_EDITOR_DVO_LIST)) {
      return null;
    }

    JsonNode editorList = actionData.get(DCM_HWP_EDITOR_DVO_LIST);
    if (!editorList.isArray() || editorList.isEmpty()) {
      return null;
    }

    for (JsonNode element : editorList) {
      if (!element.isObject() || !element.has(FILE_TYPE) || !element.has(FILE_CONTENT)) {
        continue;
      }
      if ("html".equals(element.get(FILE_TYPE).asText())) {
        String htmlContent = element.get(FILE_CONTENT).asText();
        if (htmlContent != null && !htmlContent.isEmpty()) {
          return htmlContent;
        }
      }
    }

    return null;
  }
}
