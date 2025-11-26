package kr.go.law.precedent.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

/**
 * 사건번호 정규화 유틸리티
 */
@UtilityClass
public class CaseNumberNormalizer {
  // Pattern: 2-4자리 연도 + 한글 + 숫자
  private final Pattern CORE_PATTERN = Pattern.compile("(\\d{2,4})[^가-힣]*(\\p{IsHangul}+)[^\\d]*(\\d+)");

  /**
   * 사건번호를 정규화 (연도 + 사건종류 + 번호)
   * 2자리 연도는 4자리로 변환 (60-99 → 1960-1999, 00-59 → 2000-2059)
   */
  public String normalize(String caseNumber) {
    if (caseNumber == null) {
      return null;
    }

    Matcher matcher = CORE_PATTERN.matcher(caseNumber);
    if (matcher.find()) {
      String yearStr = matcher.group(1);
      String caseType = matcher.group(2);
      String number = matcher.group(3);

      // 2자리 연도를 4자리로 변환
      if (yearStr.length() == 2) {
        int year = Integer.parseInt(yearStr);
        yearStr = (year >= 60 ? "19" : "20") + yearStr;
      }

      return yearStr + caseType + number;
    }

    return caseNumber;
  }
}
