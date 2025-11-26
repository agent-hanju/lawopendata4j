package kr.go.law.precedent.api;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import kr.go.law.precedent.dto.PrecedentDto;
import kr.go.law.util.HtmlParserUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON으로 제공하지 않는 데이터에 대한 fallback용 HTML 파서
 */
@Slf4j
@UtilityClass
public class PrecedentHtmlParser {

    /**
     * HTML 콘텐츠에서 판례 정보를 파싱
     */
    public PrecedentDto parseHtmlContent(String html) throws IOException {
        try {
            Document doc = Jsoup.parse(html);
            PrecedentDto dto = PrecedentDto.builder().build();

            // Extract metadata from hidden inputs
            String precSeq = extractHiddenInput(doc, "precSeq");
            String precNo = extractHiddenInput(doc, "precNo");
            String precNm = extractHiddenInput(doc, "precNm");

            if (precSeq != null && !precSeq.isEmpty()) {
                try {
                    dto.setPrecId(Integer.parseInt(precSeq));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse precSeq: {}", precSeq);
                }
            }
            if (precNo != null && !precNo.isEmpty()) {
                dto.setCaseNumber(precNo);
            }
            if (precNm != null && !precNm.isEmpty()) {
                dto.setCaseName(precNm);
            }

            // Extract main content
            String bodyText = doc.body().text();

            // Extract court and date info
            extractCourtAndDateInfo(bodyText, dto);

            // Extract sections
            dto.setSummary(extractSectionNormalized(bodyText, "판시사항", "판결요지"));
            dto.setDecisionSummary(extractSectionNormalized(bodyText, "판결요지", "참조조문"));

            String refArticlesStr = extractSectionNormalized(bodyText, "참조조문", "참조판례");
            String refPrecedentsStr = extractSectionNormalized(bodyText, "참조판례", "전문");
            dto.setArticleReferences(PrecedentParser.parseArticleReferences(refArticlesStr, dto.getDecisionDate()));
            dto.setPrecedentReferences(PrecedentParser.parsePrecedentReferences(refPrecedentsStr));

            // Extract content
            String bodyHtml = doc.body().html();
            String contentHtml = extractContentHtml(bodyHtml, "【전문】");
            if (contentHtml != null && !contentHtml.isEmpty()) {
                dto.setContent(HtmlParserUtil.cleanHtml(contentHtml));
            }

            log.debug("Successfully parsed HTML content for precedent: {}", dto.getPrecId());
            return dto;

        } catch (Exception e) {
            log.error("Failed to parse HTML content: {}", e.getMessage(), e);
            throw new IOException("Failed to parse HTML precedent content", e);
        }
    }

    private String extractHiddenInput(Document doc, String inputId) {
        Element input = doc.selectFirst("input[type=hidden]#" + inputId);
        if (input != null) {
            return input.attr("value");
        }
        return null;
    }

    private void extractCourtAndDateInfo(String text, PrecedentDto dto) {
        try {
            int startBracket = text.indexOf('[');
            int endBracket = text.indexOf(']', startBracket);

            if (startBracket >= 0 && endBracket > startBracket) {
                String titleInfo = text.substring(startBracket + 1, endBracket);
                String[] parts = titleInfo.split("\\s+");

                if (parts.length >= 1) {
                    dto.setCourtName(parts[0]);
                }

                String year = null;
                String month = null;
                String day = null;

                for (int i = 1; i < parts.length && i <= 3; i++) {
                    if (parts[i].matches("\\d{4}\\.")) {
                        year = parts[i].replace(".", "");
                    } else if (parts[i].matches("\\d{1,2}\\.")) {
                        if (month == null) {
                            month = parts[i].replace(".", "");
                        } else {
                            day = parts[i].replace(".", "");
                        }
                    }
                }

                if (year != null && month != null && day != null) {
                    try {
                        Integer decisionDate = Integer.parseInt(year) * 10000 +
                                Integer.parseInt(month) * 100 + Integer.parseInt(day);
                        dto.setDecisionDate(decisionDate);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to format date: year={}, month={}, day={}", year, month, day);
                    }
                }

                if (parts.length >= 6) {
                    for (int i = 5; i < parts.length; i++) {
                        if (parts[i].contains("판결") || parts[i].contains("결정") ||
                                parts[i].contains("전원합의체") || parts[i].contains("합의부")) {
                            dto.setDecisionType(parts[i]);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract court and date info: {}", e.getMessage());
        }
    }

    private String extractContentHtml(String html, String marker) {
        if (html == null || marker == null) {
            return null;
        }

        int markerIndex = html.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }

        return html.substring(markerIndex + marker.length()).trim();
    }

    private String extractSectionNormalized(String text, String startKeyword, String endKeyword) {
        try {
            Pattern pattern = Pattern.compile("【([^】]+)】");
            Matcher matcher = pattern.matcher(text);

            int startPos = -1;
            int endPos = -1;

            while (matcher.find()) {
                String header = matcher.group(1).replaceAll("\\s+", "");

                if (startPos == -1 && header.equals(startKeyword)) {
                    startPos = matcher.end();
                } else if (startPos != -1) {
                    if (endKeyword == null) {
                        endPos = text.length();
                        break;
                    } else if (header.equals(endKeyword)) {
                        endPos = matcher.start();
                        break;
                    }
                }
            }

            if (startPos != -1 && endPos != -1) {
                String section = text.substring(startPos, endPos).trim();
                return section.isEmpty() ? null : section;
            }

            if (startPos != -1 && endKeyword == null) {
                String section = text.substring(startPos).trim();
                return section.isEmpty() ? null : section;
            }

            return null;

        } catch (Exception e) {
            log.warn("Failed to extract section {}: {}", startKeyword, e.getMessage());
            return null;
        }
    }
}
