package kr.go.law.precedent.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Parser Factory (Composition Root)
 *
 * 모든 Parser 인스턴스를 생성하고 의존성을 주입하는 Factory 클래스.
 * Lazy initialization을 통해 필요한 Parser만 생성합니다.
 */
@RequiredArgsConstructor
public class PrecedentParserFactory {
  private final ObjectMapper objectMapper;

  // Lazy-initialized parser instances
  private PrecedentListParser precedentListParser;
  private PrecedentContentParser precedentContentParser;
  private PrecedentNtsParser precedentNtsParser;

  public PrecedentListParser getPrecedentListParser() {
    if (precedentListParser == null) {
      precedentListParser = new PrecedentListParser(objectMapper);
    }
    return precedentListParser;
  }

  public PrecedentContentParser getPrecedentContentParser() {
    if (precedentContentParser == null) {
      precedentContentParser = new PrecedentContentParser(objectMapper);
    }
    return precedentContentParser;
  }

  public PrecedentNtsParser getPrecedentNtsParser() {
    if (precedentNtsParser == null) {
      precedentNtsParser = new PrecedentNtsParser(objectMapper);
    }
    return precedentNtsParser;
  }

  /**
   * 기본 ObjectMapper를 사용하는 Factory 생성
   */
  public static PrecedentParserFactory createDefault() {
    return new PrecedentParserFactory(new ObjectMapper());
  }
}
