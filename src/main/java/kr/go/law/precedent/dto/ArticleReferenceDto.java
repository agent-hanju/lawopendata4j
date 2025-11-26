package kr.go.law.precedent.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 조문 참조 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleReferenceDto {
  private String rawText; // 원본 텍스트 "민법 제750조"
  private int textIndex; // 원문에서의 인덱스 (쉼표 기준)
  private String lawName; // "민법"
  private Integer joKey; // joNum * 100 + joBrNum
  private Integer referenceDate; // 참조 기준 날짜 (YYYYMMDD)
  private boolean isOlderThan; // true: referenceDate 이전, false: referenceDate 이하(포함)

  // 법명 패턴: ~법, ~령, ~규칙, ~규정, ~조례, ~긴급조치로 끝나는 단어
  private static final Pattern LAW_NAME_PATTERN = Pattern.compile(
      "([가-힣]+(?:긴급조치|법|령|규칙|규정|조례))");

  // 상대적 법명 패턴: 동법, 같은법, 같은법시행령 등
  private static final Pattern RELATIVE_LAW_PATTERN = Pattern.compile(
      "(동법|같은\\s*법)(?:시행령|시행규칙)?");

  // 조번호 패턴: 제N조, 제N조의M, 제N의M조, N조, N조의M (제 생략 허용)
  // "조" 글자가 반드시 포함되어야 함
  private static final Pattern JO_PATTERN = Pattern.compile(
      "(?:제\\s*)?(\\d+)(?:의\\s*(\\d+))?\\s*조(?:의\\s*(\\d+))?");

  /**
   * 텍스트에서 ArticleReferenceDto를 파싱합니다.
   *
   * @param text 파싱할 텍스트
   * @param index 원문에서의 인덱스
   * @param previous 이전 DTO (법명 계승용)
   * @param referenceDate 참조 기준 날짜
   */
  public static ArticleReferenceDto parse(String text, int index,
                                          ArticleReferenceDto previous,
                                          Integer referenceDate) {
    ArticleReferenceDto dto = new ArticleReferenceDto();
    dto.rawText = text;
    dto.textIndex = index;
    dto.referenceDate = referenceDate;

    // HTML 태그 제거
    String cleaned = text.replaceAll("<[^>]+>", "").trim();

    dto.isOlderThan = dto.extractIsOlderThan(cleaned);
    dto.lawName = dto.extractLawName(cleaned, previous);
    dto.joKey = dto.extractJoKey(cleaned);

    return dto;
  }

  /**
   * 참조일 제외 여부 추출
   * "전의 것" 포함 시 true (referenceDate 이전, 해당일 제외)
   * "구 "는 단순히 구법을 의미하므로 isOlderThan과 무관
   */
  private boolean extractIsOlderThan(String text) {
    return text.contains("전의 것");
  }

  /**
   * 법명 추출
   * ~법, ~령, ~규칙, ~규정, ~조례로 끝나는 단어 찾기
   * "동법", "같은법" 등 상대적 표현은 이전 DTO 법명 사용
   * 없으면 이전 DTO의 법명 사용
   */
  private String extractLawName(String text, ArticleReferenceDto previous) {
    // 상대적 법명 체크 (동법, 같은법 등)
    Matcher relativeMatcher = RELATIVE_LAW_PATTERN.matcher(text);
    if (relativeMatcher.find()) {
      return previous != null ? previous.getLawName() : null;
    }

    // 일반 법명 패턴
    Matcher matcher = LAW_NAME_PATTERN.matcher(text);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return previous != null ? previous.getLawName() : null;
  }

  /**
   * joKey 추출
   * 조번호 * 100 + 조의번호
   * 예: 제750조 -> 75000, 제3조의2 -> 302, 제1조 -> 100
   */
  private Integer extractJoKey(String text) {
    Matcher joMatcher = JO_PATTERN.matcher(text);
    if (!joMatcher.find()) {
      return null;
    }

    int joNum = Integer.parseInt(joMatcher.group(1));
    // group(2): 제N의M조 형태, group(3): 제N조의M 형태
    int joBrNum = 0;
    if (joMatcher.group(2) != null) {
      joBrNum = Integer.parseInt(joMatcher.group(2));
    } else if (joMatcher.group(3) != null) {
      joBrNum = Integer.parseInt(joMatcher.group(3));
    }

    return joNum * 100 + joBrNum;
  }
}
