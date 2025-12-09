package kr.go.law.statute.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Parser Factory (Composition Root)
 *
 * 모든 Parser 인스턴스를 생성하고 의존성을 주입하는 Factory 클래스.
 * Lazy initialization을 통해 필요한 Parser만 생성합니다.
 */
@RequiredArgsConstructor
public class StatuteParserFactory {
  private final ObjectMapper objectMapper;

  // Lazy-initialized parser instances
  private MokParser mokParser;
  private HoParser hoParser;
  private HangParser hangParser;
  private DepartmentParser departmentParser;
  private AddendumParser addendumParser;
  private AppendixParser appendixParser;
  private CoOrdinanceInfoParser coOrdinanceInfoParser;
  private ArticleParser articleParser;
  private StatuteContentParser statuteContentParser;
  private StatuteListParser statuteListParser;
  private StatuteHistoryParser statuteHistoryParser;

  public MokParser getMokParser() {
    if (mokParser == null) {
      mokParser = new MokParser(objectMapper);
    }
    return mokParser;
  }

  public HoParser getHoParser() {
    if (hoParser == null) {
      hoParser = new HoParser(objectMapper, getMokParser());
    }
    return hoParser;
  }

  public HangParser getHangParser() {
    if (hangParser == null) {
      hangParser = new HangParser(objectMapper, getHoParser());
    }
    return hangParser;
  }

  public DepartmentParser getDepartmentParser() {
    if (departmentParser == null) {
      departmentParser = new DepartmentParser(objectMapper);
    }
    return departmentParser;
  }

  public AddendumParser getAddendumParser() {
    if (addendumParser == null) {
      addendumParser = new AddendumParser(objectMapper);
    }
    return addendumParser;
  }

  public AppendixParser getAppendixParser() {
    if (appendixParser == null) {
      appendixParser = new AppendixParser(objectMapper);
    }
    return appendixParser;
  }

  public CoOrdinanceInfoParser getCoOrdinanceInfoParser() {
    if (coOrdinanceInfoParser == null) {
      coOrdinanceInfoParser = new CoOrdinanceInfoParser(objectMapper);
    }
    return coOrdinanceInfoParser;
  }

  public ArticleParser getArticleParser() {
    if (articleParser == null) {
      articleParser = new ArticleParser(objectMapper, getHangParser());
    }
    return articleParser;
  }

  public StatuteContentParser getStatuteContentParser() {
    if (statuteContentParser == null) {
      statuteContentParser = new StatuteContentParser(
          objectMapper,
          getArticleParser(),
          getDepartmentParser(),
          getAddendumParser(),
          getAppendixParser(),
          getCoOrdinanceInfoParser());
    }
    return statuteContentParser;
  }

  public StatuteListParser getStatuteListParser() {
    if (statuteListParser == null) {
      statuteListParser = new StatuteListParser(objectMapper, getCoOrdinanceInfoParser());
    }
    return statuteListParser;
  }

  public StatuteHistoryParser getStatuteHistoryParser() {
    if (statuteHistoryParser == null) {
      statuteHistoryParser = new StatuteHistoryParser(objectMapper);
    }
    return statuteHistoryParser;
  }

  /**
   * 기본 ObjectMapper를 사용하는 Factory 생성
   */
  public static StatuteParserFactory createDefault() {
    return new StatuteParserFactory(new ObjectMapper());
  }
}
