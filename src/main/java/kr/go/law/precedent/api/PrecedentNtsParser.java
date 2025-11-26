package kr.go.law.precedent.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;

import kr.go.law.precedent.dto.PrecedentDto;
import kr.go.law.util.HtmlParserUtil;
import kr.go.law.util.JsonParserUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 국세법령정보시스템(NTS) API 응답 파서
 */
@Slf4j
@UtilityClass
public class PrecedentNtsParser {

    /**
     * NTS API 응답에서 판례 정보를 파싱
     */
    public PrecedentDto parseNtsContent(JsonNode ntsResponse) throws IOException {
        String status = JsonParserUtil.getString(ntsResponse, "status");
        if (!"SUCCESS".equals(status)) {
            throw new IOException("NTS API returned non-success status: " + status);
        }

        JsonNode data = JsonParserUtil.getJsonObject(ntsResponse, "data");
        if (data == null) {
            throw new IOException("NTS API response missing 'data' field");
        }

        JsonNode actionData = JsonParserUtil.getJsonObject(data, "ASIQTB002PR01");
        if (actionData == null) {
            throw new IOException("NTS API response missing 'ASIQTB002PR01' field");
        }

        JsonNode dcmDVO = JsonParserUtil.getJsonObject(actionData, "dcmDVO");
        if (dcmDVO == null) {
            throw new IOException("NTS API response missing 'dcmDVO' field");
        }

        PrecedentDto dto = PrecedentDto.builder().build();

        // Parse basic fields
        String title = JsonParserUtil.getString(dcmDVO, "ntstDcmTtl");
        if (title != null && !title.isEmpty()) {
            dto.setCaseName(HtmlParserUtil.toPlainText(title));
        }

        String caseNumber = JsonParserUtil.getString(dcmDVO, "ntstDcmDscmCntn");
        if (caseNumber != null && !caseNumber.isEmpty()) {
            dto.setCaseNumber(HtmlParserUtil.toPlainText(caseNumber));
        }

        String gist = JsonParserUtil.getString(dcmDVO, "ntstDcmGistCntn");
        if (gist != null) {
            dto.setSummary(HtmlParserUtil.toPlainText(gist));
        }

        String content = JsonParserUtil.getString(dcmDVO, "ntstDcmCntn");
        if (content != null) {
            dto.setContent(HtmlParserUtil.cleanHtml(content));
        }

        // Try to extract HTML content if content is placeholder or empty
        if (content == null || content.trim().isEmpty() || content.contains("붙임과 같습니다")
                || content.contains("이하참조")) {
            String htmlContent = extractHtmlContent(actionData);
            if (htmlContent != null && !htmlContent.isEmpty()) {
                String processedHtml = parseNtsHtmlContent(htmlContent, dto);
                if (processedHtml != null && !processedHtml.isEmpty()) {
                    dto.setContent(processedHtml);
                } else {
                    dto.setContent(HtmlParserUtil.cleanHtml(htmlContent));
                }
            }
        }

        String subject = JsonParserUtil.getString(dcmDVO, "ntstDcmMatrCntn");
        if (subject != null) {
            dto.setDecisionSummary("주제: " + HtmlParserUtil.toPlainText(subject));
        }

        String decisionDateStr = JsonParserUtil.getString(dcmDVO, "ntstDcmDscmDt");
        if (decisionDateStr != null && !decisionDateStr.isEmpty()) {
            try {
                dto.setDecisionDate(Integer.parseInt(decisionDateStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse decisionDate: {}", decisionDateStr);
            }
        }

        String relatedLaws = JsonParserUtil.getString(dcmDVO, "ntstDcmRelLgltCntn");
        if (relatedLaws != null) {
            String refArticlesStr = HtmlParserUtil.toPlainText(relatedLaws);
            dto.setArticleReferences(PrecedentParser.parseArticleReferences(refArticlesStr, dto.getDecisionDate()));
        }

        // Parse additional lists
        parseReferencedStatutes(actionData, dto);
        parseRelatedPrecedents(actionData, dto);

        dto.setDataSource("국세법령정보시스템");

        log.debug("Parsed NTS precedent: title={}, caseNumber={}", title, caseNumber);
        return dto;
    }

    private String parseNtsHtmlContent(String html, PrecedentDto dto) {
        if (html == null || html.isEmpty()) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(html);
            String plainText = doc.body().text();

            // Extract from table
            Element table = doc.selectFirst("table.sebeop_t");
            if (table != null) {
                extractFromTable(table, dto);
            }

            // Fallback to plain text extraction
            extractFromPlainText(plainText, dto);

            return HtmlParserUtil.cleanHtml(html);

        } catch (Exception e) {
            log.warn("Failed to parse NTS HTML content: {}", e.getMessage());
            return null;
        }
    }

    private void extractFromTable(Element table, PrecedentDto dto) {
        Elements rows = table.select("tbody > tr");

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() != 2) {
                continue;
            }

            String label = cells.get(0).text().replaceAll("\\s+", "");
            String value = cells.get(1).text().trim();

            switch (label) {
                case "사건":
                    extractCaseInfo(value, dto);
                    break;
                case "판결선고":
                    extractDecisionDate(value, dto);
                    break;
                default:
                    break;
            }
        }
    }

    private void extractCaseInfo(String caseInfo, PrecedentDto dto) {
        if (caseInfo == null || caseInfo.isEmpty()) {
            return;
        }

        String[] parts = caseInfo.split("\\s+", 2);
        if (parts.length >= 1) {
            String caseNumberPart = parts[0];
            if (caseNumberPart.contains("-")) {
                String courtName = caseNumberPart.substring(0, caseNumberPart.indexOf("-"));
                if (dto.getCourtName() == null || dto.getCourtName().isEmpty()) {
                    dto.setCourtName(courtName);
                }
            }
        }

        if (parts.length >= 2) {
            String caseType = parts[1];
            if (dto.getCaseName() == null || dto.getCaseName().isEmpty()) {
                dto.setCaseName(caseType);
            }
        }
    }

    private void extractDecisionDate(String dateStr, PrecedentDto dto) {
        if (dateStr == null || dateStr.isEmpty()) {
            return;
        }

        try {
            Pattern datePattern = Pattern.compile("(\\d{4})\\.(\\d{1,2})\\.(\\d{1,2})\\.");
            Matcher matcher = datePattern.matcher(dateStr);

            if (matcher.find()) {
                String year = matcher.group(1);
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                Integer decisionDate = Integer.parseInt(year) * 10000 + month * 100 + day;

                if (dto.getDecisionDate() == null) {
                    dto.setDecisionDate(decisionDate);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse decision date: {}", dateStr);
        }
    }

    private void extractFromPlainText(String plainText, PrecedentDto dto) {
        if (plainText == null || plainText.isEmpty()) {
            return;
        }

        if (dto.getDecisionDate() == null) {
            Pattern decisionDatePattern = Pattern
                    .compile("판\\s*결\\s*선\\s*고\\s*(\\d{4})\\s*\\.\\s*(\\d{1,2})\\s*\\.\\s*(\\d{1,2})\\s*\\.");
            Matcher matcher = decisionDatePattern.matcher(plainText);
            if (matcher.find()) {
                String year = matcher.group(1);
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                dto.setDecisionDate(Integer.parseInt(year) * 10000 + month * 100 + day);
            }
        }

        if (dto.getCaseName() == null || dto.getCaseName().isEmpty()) {
            Pattern casePattern = Pattern.compile("사\\s*건\\s+([^\\s]+)\\s+([^원피변]+)");
            Matcher matcher = casePattern.matcher(plainText);
            if (matcher.find()) {
                String caseNumberMatch = matcher.group(1);
                String caseName = matcher.group(2).trim();

                if (caseNumberMatch.contains("-")) {
                    if (dto.getCourtName() == null || dto.getCourtName().isEmpty()) {
                        String courtName = caseNumberMatch.substring(0, caseNumberMatch.indexOf("-"));
                        dto.setCourtName(courtName);
                    }
                }

                if (!caseName.isEmpty()) {
                    dto.setCaseName(caseName);
                }
            }
        }
    }

    private String extractHtmlContent(JsonNode actionData) {
        if (!actionData.has("dcmHwpEditorDVOList")) {
            return null;
        }

        try {
            JsonNode editorList = actionData.get("dcmHwpEditorDVOList");
            if (editorList == null || editorList.isEmpty()) {
                return null;
            }

            for (JsonNode element : editorList) {
                if (!element.isObject()) {
                    continue;
                }

                String fileType = JsonParserUtil.getString(element, "dcmFleTy");
                if ("html".equals(fileType)) {
                    String htmlContent = JsonParserUtil.getString(element, "dcmFleByte");
                    if (htmlContent != null && !htmlContent.isEmpty()) {
                        return htmlContent;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract HTML content from dcmHwpEditorDVOList", e);
        }

        return null;
    }

    private void parseReferencedStatutes(JsonNode actionData, PrecedentDto dto) {
        if (!actionData.has("dcmRltnStttList")) {
            return;
        }

        try {
            JsonNode statuteList = actionData.get("dcmRltnStttList");
            if (statuteList == null || statuteList.isEmpty()) {
                return;
            }

            List<String> statutes = new ArrayList<>();
            for (JsonNode element : statuteList) {
                if (!element.isObject()) {
                    continue;
                }

                String statuteName = JsonParserUtil.getString(element, "ntstTextNm");
                if (statuteName != null && !statuteName.isEmpty()) {
                    statutes.add(statuteName);
                }
            }

            if (!statutes.isEmpty()) {
                String newRefs = String.join(", ", statutes);
                var newArticleRefs = PrecedentParser.parseArticleReferences(newRefs, dto.getDecisionDate());

                // 기존 참조에 추가
                var existingRefs = dto.getArticleReferences();
                if (existingRefs != null && !existingRefs.isEmpty()) {
                    var combined = new ArrayList<>(existingRefs);
                    combined.addAll(newArticleRefs);
                    dto.setArticleReferences(combined);
                } else {
                    dto.setArticleReferences(newArticleRefs);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse dcmRltnStttList", e);
        }
    }

    private void parseRelatedPrecedents(JsonNode actionData, PrecedentDto dto) {
        if (!actionData.has("trilPsagList")) {
            return;
        }

        try {
            JsonNode precedentList = actionData.get("trilPsagList");
            if (precedentList == null || precedentList.isEmpty()) {
                return;
            }

            List<String> precedents = new ArrayList<>();
            for (JsonNode element : precedentList) {
                if (!element.isObject()) {
                    continue;
                }

                String caseNumberVal = JsonParserUtil.getString(element, "ntstDcmDscmCntn");
                if (caseNumberVal != null && !caseNumberVal.isEmpty()) {
                    precedents.add(caseNumberVal);
                }
            }

            if (!precedents.isEmpty()) {
                String refs = String.join(", ", precedents);
                dto.setPrecedentReferences(PrecedentParser.parsePrecedentReferences(refs));
            }
        } catch (Exception e) {
            log.warn("Failed to parse trilPsagList", e);
        }
    }
}
