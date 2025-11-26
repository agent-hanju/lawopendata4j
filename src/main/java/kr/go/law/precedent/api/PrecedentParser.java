package kr.go.law.precedent.api;

import static kr.go.law.util.JsonParserUtil.getInt;
import static kr.go.law.util.JsonParserUtil.getString;
import static kr.go.law.util.JsonParserUtil.normalizeToArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.go.law.precedent.dto.ArticleReferenceDto;
import kr.go.law.precedent.dto.PrecedentDto;
import kr.go.law.precedent.dto.PrecedentReferenceDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 판례정보 API 응답 파서
 */
@Slf4j
public class PrecedentParser {
    private final ObjectMapper objectMapper;

    private static final String LIST_ROOT_KEY = "PrecSearch";
    private static final String LIST_ARRAY_KEY = "prec";
    private static final String CONTENT_ROOT_KEY = "PrecService";

    public PrecedentParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * API 응답에서 totalCnt를 파싱합니다.
     */
    public int parseTotalCount(JsonNode response) {
        if (response == null || !response.has(LIST_ROOT_KEY)) {
            return 0;
        }
        String totalCntStr = getString(response.get(LIST_ROOT_KEY), "totalCnt");
        return totalCntStr != null ? Integer.parseInt(totalCntStr) : 0;
    }

    /**
     * List API 응답에서 PrecedentDto 목록을 파싱합니다.
     */
    public List<PrecedentDto> parseList(JsonNode listApiResponse) {
        if (listApiResponse == null || !listApiResponse.has(LIST_ROOT_KEY)) {
            log.warn("Invalid list response");
            return Collections.emptyList();
        }

        final JsonNode rootNode = listApiResponse.get(LIST_ROOT_KEY);
        if (!rootNode.has(LIST_ARRAY_KEY)) {
            log.debug("No data in response");
            return Collections.emptyList();
        }

        final ArrayNode items = normalizeToArray(rootNode.get(LIST_ARRAY_KEY));
        final List<PrecedentDto> precedents = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            JsonNode itemData = items.get(i);
            PrecedentDto dto = parsePrecListItem(itemData);
            precedents.add(dto);
        }

        return precedents;
    }

    /**
     * Content API 응답을 파싱하여 PrecedentDto를 반환합니다.
     */
    public PrecedentDto parseContent(JsonNode contentApiResponse, Integer precId) {
        if (contentApiResponse == null) {
            return null;
        }

        if (!contentApiResponse.has(CONTENT_ROOT_KEY)) {
            log.warn("No {} in content response for precId={}", CONTENT_ROOT_KEY, precId);
            return null;
        }

        JsonNode contentNode = contentApiResponse.get(CONTENT_ROOT_KEY);
        return parsePrecContent(contentNode);
    }

    // ===== PREC (판례) 파싱 =====

    private PrecedentDto parsePrecListItem(JsonNode node) {
        Map<String, String> unexpected = new HashMap<>();
        trackUnexpectedFields(node, Set.of(
                "판례일련번호", "사건명", "사건번호", "선고일자", "법원명", "법원종류코드",
                "사건종류명", "사건종류코드", "판결유형", "선고", "데이터출처명",
                "id", "판례상세링크"), unexpected::put);

        return PrecedentDto.builder()
                .precId(getInt(node, "판례일련번호"))
                .caseName(getString(node, "사건명"))
                .caseNumber(getString(node, "사건번호"))
                .decisionDate(normalizeDate(getString(node, "선고일자")))
                .courtName(getString(node, "법원명"))
                .courtCode(getString(node, "법원종류코드"))
                .caseTypeName(getString(node, "사건종류명"))
                .caseTypeCode(getString(node, "사건종류코드"))
                .decisionType(getString(node, "판결유형"))
                .declaration(getString(node, "선고"))
                .dataSource(getString(node, "데이터출처명"))
                .unexpectedFieldMap(unexpected.isEmpty() ? null : unexpected)
                .build();
    }

    private PrecedentDto parsePrecContent(JsonNode node) {
        Map<String, String> unexpected = new HashMap<>();
        trackUnexpectedFields(node, Set.of(
                "판례정보일련번호", "사건번호", "사건명", "판시사항", "판결요지", "판례내용",
                "사건종류코드", "사건종류명", "법원종류코드", "법원명", "판결유형", "선고",
                "선고일자", "참조판례", "참조조문", "id", "판례상세링크"), unexpected::put);

        Integer decisionDate = getInt(node, "선고일자");
        String refArticlesStr = getString(node, "참조조문");
        String refPrecedentsStr = getString(node, "참조판례");

        return PrecedentDto.builder()
                .precId(getInt(node, "판례정보일련번호"))
                .caseName(getString(node, "사건명"))
                .caseNumber(getString(node, "사건번호"))
                .decisionDate(decisionDate)
                .courtName(getString(node, "법원명"))
                .courtCode(getString(node, "법원종류코드"))
                .caseTypeName(getString(node, "사건종류명"))
                .caseTypeCode(getString(node, "사건종류코드"))
                .decisionType(getString(node, "판결유형"))
                .declaration(getString(node, "선고"))
                .summary(getString(node, "판시사항"))
                .decisionSummary(getString(node, "판결요지"))
                .articleReferences(parseArticleReferences(refArticlesStr, decisionDate))
                .precedentReferences(parsePrecedentReferences(refPrecedentsStr))
                .content(getString(node, "판례내용"))
                .unexpectedFieldMap(unexpected.isEmpty() ? null : unexpected)
                .build();
    }

    // ===== 유틸리티 =====

    private void trackUnexpectedFields(
            JsonNode node,
            Set<String> knownFields,
            BiConsumer<String, String> recorder) {
        node.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            if (!knownFields.contains(fieldName)) {
                try {
                    String jsonValue = objectMapper.writeValueAsString(entry.getValue());
                    recorder.accept("$." + fieldName, jsonValue);
                    log.warn("Unknown field detected: {} = {}", fieldName, jsonValue);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize unknown field: {}", fieldName, e);
                }
            }
        });
    }

    // ===== 참조 파싱 =====

    /**
     * 참조조문 문자열을 List<ArticleReferenceDto>로 파싱
     */
    public static List<ArticleReferenceDto> parseArticleReferences(String referenceText, Integer decisionDate) {
        List<ArticleReferenceDto> result = new ArrayList<>();
        if (referenceText == null || referenceText.isBlank()) {
            return result;
        }

        // 쉼표 또는 슬래시+항목기호로 분리
        String[] parts = referenceText.split(",|\\s*/\\s*(?=[가-힣]\\.)|\\s*/\\s*(?=\\([0-9]+\\)\\.)");
        ArticleReferenceDto previous = null;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty())
                continue;

            // 항목기호 제거 (가., 나., (1). 등)
            part = part.replaceFirst("^[가-힣]\\.|^\\([0-9]+\\)\\.", "").trim();
            if (part.isEmpty())
                continue;

            ArticleReferenceDto dto = ArticleReferenceDto.parse(part, i, previous, decisionDate);
            result.add(dto);
            previous = dto;
        }

        return result;
    }

    /**
     * 참조판례 문자열을 List<PrecedentReferenceDto>로 파싱
     */
    public static List<PrecedentReferenceDto> parsePrecedentReferences(String referenceText) {
        List<PrecedentReferenceDto> result = new ArrayList<>();
        if (referenceText == null || referenceText.isBlank()) {
            return result;
        }

        // 쉼표로 분리
        String[] parts = referenceText.split(",");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty())
                continue;

            PrecedentReferenceDto dto = PrecedentReferenceDto.parse(part, i);
            result.add(dto);
        }

        return result;
    }

    /**
     * 날짜 문자열을 YYYYMMDD Integer로 정규화합니다.
     * "1960.08.18" → 19600818
     * "20200101" → 20200101
     */
    private Integer normalizeDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 숫자만 추출
        String digits = dateStr.replaceAll("[^0-9]", "");

        if (digits.length() == 8) {
            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse date: {}", dateStr);
                return null;
            }
        }

        log.warn("Invalid date format: {}", dateStr);
        return null;
    }
}
