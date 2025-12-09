package kr.go.law.precedent.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kr.go.law.precedent.dto.PrecedentContentDto;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 근로복지공단 산재판례 사이트(sanjaecase.comwel.or.kr) HTML 파서
 *
 * law.go.kr API에서 누락된 필드(선고일자 등)를 보완하기 위해 사용
 */
@Slf4j
@UtilityClass
public class PrecedentComwelParser {

  private static final String BASE_URL = "https://sanjaecase.comwel.or.kr/service/dataView";

  /**
   * 사건번호와 법원명으로 COMWEL URL 생성
   *
   * @param caseNumber 사건번호 (예: "2005구단1603")
   * @param courtName  법원명 (예: "창원지방법원")
   * @return COMWEL URL
   */
  public String buildUrl(String caseNumber, String courtName) {
    if (caseNumber == null || courtName == null) {
      return null;
    }
    return BASE_URL + "?id=" + caseNumber + "_" + courtName;
  }

  /**
   * HTML 콘텐츠에서 판례 메타데이터 파싱
   *
   * @param html HTML 콘텐츠
   * @return 파싱된 메타데이터 맵 (키: 필드명, 값: 필드값)
   */
  public Map<String, String> parseMetadata(String html) throws IOException {
    Map<String, String> metadata = new HashMap<>();

    try {
      Document doc = Jsoup.parse(html);

      // div.info 내의 ul > li.item1(키), li.item2(값) 구조 파싱
      Element infoDiv = doc.selectFirst("div.info");
      if (infoDiv == null) {
        log.warn("COMWEL HTML: div.info not found");
        return metadata;
      }

      Elements ulElements = infoDiv.select("ul");
      for (Element ul : ulElements) {
        Element keyElement = ul.selectFirst("li.item1");
        Element valueElement = ul.selectFirst("li.item2");

        if (keyElement != null && valueElement != null) {
          String key = keyElement.text().trim().replace(" ", "");
          String value = valueElement.text().trim();
          if (!key.isEmpty() && !value.isEmpty()) {
            metadata.put(key, value);
          }
        }
      }

      log.debug("COMWEL metadata parsed: {}", metadata.keySet());
      return metadata;

    } catch (Exception e) {
      log.error("Failed to parse COMWEL HTML: {}", e.getMessage());
      throw new IOException("Failed to parse COMWEL HTML content", e);
    }
  }

  /**
   * 기존 DTO에 COMWEL에서 파싱한 메타데이터를 병합
   *
   * @param dto      기존 DTO (law.go.kr API에서 수집한 데이터)
   * @param metadata COMWEL에서 파싱한 메타데이터
   * @return 병합된 DTO
   */
  public PrecedentContentDto mergeMetadata(PrecedentContentDto dto, Map<String, String> metadata) {
    if (dto == null || metadata == null || metadata.isEmpty()) {
      return dto;
    }

    // 판결선고 → decisionDate
    String decisionDateStr = metadata.get("판결선고");
    if (decisionDateStr != null && (dto.getDecisionDate() == null || dto.getDecisionDate() == 10101)) {
      Integer decisionDate = parseDateString(decisionDateStr);
      if (decisionDate != null) {
        dto.setDecisionDate(decisionDate);
        log.debug("COMWEL: decisionDate updated to {}", decisionDate);
      }
    }

    // 변론종결 → 별도 필드 없으면 로깅만
    String argumentCloseDateStr = metadata.get("변론종결");
    if (argumentCloseDateStr != null) {
      log.debug("COMWEL: argumentCloseDate found: {}", argumentCloseDateStr);
    }

    // 전심판결 → 별도 필드 없으면 로깅만
    String priorJudgment = metadata.get("전심판결");
    if (priorJudgment != null) {
      log.debug("COMWEL: priorJudgment found: {}", priorJudgment);
    }

    return dto;
  }

  /**
   * 날짜 문자열을 Integer로 변환 (YYYYMMDD 형식)
   *
   * @param dateStr 날짜 문자열 (예: "2007. 04. 19" 또는 "2007.04.19")
   * @return YYYYMMDD 형식의 Integer (예: 20070419)
   */
  private Integer parseDateString(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return null;
    }

    try {
      // "2007. 04. 19" 또는 "2007.04.19" 형식 처리
      String normalized = dateStr.replaceAll("\\s+", "").replace(".", "");
      if (normalized.length() == 8) {
        return Integer.parseInt(normalized);
      }

      // 다른 형식 시도: "2007. 4. 19" → 패딩 필요
      String[] parts = dateStr.split("[.\\s]+");
      if (parts.length >= 3) {
        int year = Integer.parseInt(parts[0].trim());
        int month = Integer.parseInt(parts[1].trim());
        int day = Integer.parseInt(parts[2].trim());
        return year * 10000 + month * 100 + day;
      }

      log.warn("COMWEL: Unable to parse date string: {}", dateStr);
      return null;

    } catch (NumberFormatException e) {
      log.warn("COMWEL: Failed to parse date string '{}': {}", dateStr, e.getMessage());
      return null;
    }
  }
}
