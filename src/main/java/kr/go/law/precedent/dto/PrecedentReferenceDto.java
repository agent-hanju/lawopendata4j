package kr.go.law.precedent.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import kr.go.law.precedent.util.CaseNumberNormalizer;

/**
 * 판례/결정례/해석례 참조 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrecedentReferenceDto {
  private String rawText; // "대법원 2020. 1. 1. 선고 2019다12345 판결"
  private int textIndex; // 원문에서의 인덱스
  private String courtName; // "대법원", "헌법재판소" 등
  private String caseNumber; // "2019다12345"
  private Integer decisionDate; // 20200101

  // 법원명 패턴: ~법원, ~재판소
  private static final Pattern COURT_NAME_PATTERN = Pattern.compile(
      "([가-힣]+(?:법원|재판소))");

  // 날짜 패턴: YYYY.M.D. 또는 YYYY. M. D.
  private static final Pattern DATE_PATTERN = Pattern.compile(
      "(\\d{4})\\s*\\.\\s*(\\d{1,2})\\s*\\.\\s*(\\d{1,2})\\s*\\.?");

  // 사건번호 패턴: 숫자+한글+숫자 (4290민상165, 82다340, 2019다12345)
  private static final Pattern CASE_NUMBER_PATTERN = Pattern.compile(
      "(\\d{2,4}[가-힣]{1,4}\\d+)");

  /**
   * 텍스트에서 PrecedentReferenceDto를 파싱합니다.
   *
   * @param text 파싱할 텍스트
   * @param index 원문에서의 인덱스
   */
  public static PrecedentReferenceDto parse(String text, int index) {
    PrecedentReferenceDto dto = new PrecedentReferenceDto();
    dto.rawText = text;
    dto.textIndex = index;

    // HTML 태그 제거
    String cleaned = text.replaceAll("<[^>]+>", "").trim();

    dto.courtName = dto.extractCourtName(cleaned);
    dto.decisionDate = dto.extractDecisionDate(cleaned);
    dto.caseNumber = dto.extractCaseNumber(cleaned);

    return dto;
  }

  /**
   * 법원명 추출
   * ~법원, ~재판소로 끝나는 단어 찾기
   */
  private String extractCourtName(String text) {
    Matcher matcher = COURT_NAME_PATTERN.matcher(text);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  /**
   * 선고일자 추출
   * YYYY.M.D. 형식에서 YYYYMMDD 정수로 변환
   */
  private Integer extractDecisionDate(String text) {
    Matcher matcher = DATE_PATTERN.matcher(text);
    if (matcher.find()) {
      int year = Integer.parseInt(matcher.group(1));
      int month = Integer.parseInt(matcher.group(2));
      int day = Integer.parseInt(matcher.group(3));
      return year * 10000 + month * 100 + day;
    }
    return null;
  }

  /**
   * 사건번호 추출
   * 숫자+한글+숫자 패턴 (정규화 적용)
   */
  private String extractCaseNumber(String text) {
    Matcher matcher = CASE_NUMBER_PATTERN.matcher(text);
    if (matcher.find()) {
      return CaseNumberNormalizer.normalize(matcher.group(1));
    }
    return null;
  }
}
