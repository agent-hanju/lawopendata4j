package kr.go.law.statute.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.common.parser.TypeMismatchException;
import kr.go.law.statute.dto.Addendum;
import kr.go.law.statute.dto.Appendix;
import kr.go.law.statute.dto.ArticleContentDto;
import kr.go.law.statute.dto.CoOrdinanceInfo;
import kr.go.law.statute.dto.Department;
import kr.go.law.statute.dto.Knd;
import kr.go.law.statute.dto.Org;
import kr.go.law.statute.dto.StatuteBasicInfo;
import kr.go.law.statute.dto.StatuteContentDto;
import lombok.extern.slf4j.Slf4j;

/** 법령 본문 API 응답 파서 */
@Slf4j
public class StatuteContentParser extends BaseParser<StatuteContentDto> {
  private final ArticleParser articleParser;
  private final DepartmentParser departmentParser;
  private final AddendumParser addendumParser;
  private final AppendixParser appendixParser;
  private final CoOrdinanceInfoParser coOrdinanceInfoParser;

  public StatuteContentParser(
      ObjectMapper objectMapper,
      ArticleParser articleParser,
      DepartmentParser departmentParser,
      AddendumParser addendumParser,
      AppendixParser appendixParser,
      CoOrdinanceInfoParser coOrdinanceInfoParser) {
    super(objectMapper);
    this.articleParser = articleParser;
    this.departmentParser = departmentParser;
    this.addendumParser = addendumParser;
    this.appendixParser = appendixParser;
    this.coOrdinanceInfoParser = coOrdinanceInfoParser;
  }

  private static final String ROOT_FIELD = "법령";

  // ROOT_FIELD의 하위 필드
  private static final String AMENDMENT = "개정문";
  private static final String AMENDMENT_CONTENT = "개정문내용";
  private static final String APPENDICES = "별표";
  private static final String BASIC_INFO = "기본정보";
  private static final String ADDENDA = "부칙";
  private static final String ARTICLES = "조문";
  private static final String RR_CLS_REASON = "제개정이유";
  private static final String RR_CLS_REASON_CONTENT = "제개정이유내용";

  // BASIC_INFO의 하위 필드
  private static final String LS_NM = "법령명_한글";
  private static final String APPENDIX_EF_YD_STR = "별표시행일자문자열";
  private static final String DECISION_BODY = "의결구분";
  private static final String PROPOSAL_TYPE = "제안구분";
  private static final String ANC_NO = "공포번호";
  private static final String PHONE_NUMBER = "전화번호";
  private static final String LANG = "언어";
  private static final String RR_CLS_NM = "제개정구분";
  private static final String LS_ID = "법령ID";
  private static final String CO_ORDINANCE_INFO = "공동부령정보";
  private static final String ORG = "소관부처";
  private static final String ORG_NM = "content";
  private static final String ORG_CD = "소관부처코드";
  private static final String IS_ANC = "공포법령여부";
  private static final String KND = "법종구분";
  private static final String KND_NM = "content";
  private static final String KND_CD = "법종구분코드";
  private static final String IS_TITLE_CHANGED = "제명변경여부";
  private static final String EF_YD = "시행일자";
  private static final String APPENDIX_EDIT_YN = "별표편집여부";
  private static final String CONTACT = "연락부서";
  private static final String ARTICLE_EF_YD_STR = "조문시행일자문자열";
  private static final String LS_NM_HANJA = "법령명_한자";
  private static final String LS_NM_ABBR = "법령명약칭";
  private static final String ANC_YD = "공포일자";
  private static final String IS_HANGUL = "한글법령여부";
  private static final String CHAPTER = "편장절관";

  private static final Set<String> ROOT_MEMBERS = Set.of(
      AMENDMENT,
      "법령키", // 다른 필드를 통해 복원가능하므로 생략
      APPENDICES,
      BASIC_INFO,
      ADDENDA,
      ARTICLES,
      RR_CLS_REASON);

  private static final Set<String> BASIC_INFO_MEMBERS = Set.of(
      LS_NM,
      APPENDIX_EF_YD_STR,
      DECISION_BODY,
      PROPOSAL_TYPE,
      ANC_NO,
      PHONE_NUMBER,
      LANG,
      RR_CLS_NM,
      LS_ID,
      CO_ORDINANCE_INFO,
      ORG,
      IS_ANC,
      KND,
      IS_TITLE_CHANGED,
      EF_YD,
      APPENDIX_EDIT_YN,
      CONTACT,
      ARTICLE_EF_YD_STR,
      LS_NM_HANJA,
      LS_NM_ABBR,
      ANC_YD,
      IS_HANGUL,
      CHAPTER);

  /** Content API 응답 파싱 */
  @Override
  public StatuteContentDto parse(final JsonNode response) {
    if (response != null && response.has(ROOT_FIELD)) {
      final JsonNode lawNode = response.get(ROOT_FIELD);

      final Map<String, String> unexpected = new HashMap<>();
      trackUnexpectedFields(lawNode, ROOT_MEMBERS, unexpected);
      final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

      return StatuteContentDto.builder()
          .amendment(parseAmendment(lawNode, onTypeMismatch))
          .appendices(parseAppendices(lawNode, onTypeMismatch))
          .basicInfo(parseBasicInfo(lawNode, onTypeMismatch))
          .addenda(parseAddenda(lawNode, onTypeMismatch))
          .articles(parseArticles(lawNode, onTypeMismatch))
          .rrClsReason(parseRrClsReason(lawNode, onTypeMismatch))
          .unexpected(unexpected)
          .build();
    } else {
      log.warn("No data in response");
      return null;
    }

  }

  /** 개정문 파싱 */
  private String parseAmendment(final JsonNode lawNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(AMENDMENT)) {
      final JsonNode amendmentNode = lawNode.get(AMENDMENT);
      try {
        return flattenStringArray(amendmentNode, AMENDMENT_CONTENT, onTypeMismatch);
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(AMENDMENT, amendmentNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(AMENDMENT, null);
      return null;
    }
  }

  /** 별표 파싱 */
  private List<Appendix> parseAppendices(final JsonNode lawNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(APPENDICES)) {
      final JsonNode appendicesNode = lawNode.get(APPENDICES);
      try {
        final ArrayNode arrayNode = normalizeToArray(appendicesNode, "별표단위");
        final List<Appendix> appendices = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          appendices.add(appendixParser.parse(node));
        }
        return appendices;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(APPENDICES, appendicesNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(APPENDICES, null);
      return null;
    }
  }

  /** 법령 기본정보 파싱 */
  private StatuteBasicInfo parseBasicInfo(final JsonNode lawNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(BASIC_INFO)) {
      final JsonNode basicNode = lawNode.get(BASIC_INFO);
      final BiConsumer<String, JsonNode> onTypeMismatchInner = (fieldName, jsonNode) -> onTypeMismatch
          .accept(BASIC_INFO.concat(".").concat(fieldName), jsonNode);
      trackUnexpectedFields(basicNode, BASIC_INFO_MEMBERS, onTypeMismatchInner);
      return StatuteBasicInfo.builder()
          .lsNm(getString(basicNode, LS_NM, onTypeMismatchInner))
          .appendixEfYdStr(getString(basicNode, APPENDIX_EF_YD_STR, onTypeMismatchInner, true))
          .decisionBody(getString(basicNode, DECISION_BODY, onTypeMismatchInner))
          .proposalType(getString(basicNode, PROPOSAL_TYPE, onTypeMismatchInner))
          .ancNo(getInt(basicNode, ANC_NO, onTypeMismatchInner))
          .phoneNumber(getString(basicNode, PHONE_NUMBER, onTypeMismatchInner, true))
          .lang(getString(basicNode, LANG, onTypeMismatchInner))
          .rrClsNm(getString(basicNode, RR_CLS_NM, onTypeMismatchInner))
          .lsId(getInt(basicNode, LS_NM, onTypeMismatchInner))
          .coOrdinanceInfos(parseCoOrdinanceInfos(basicNode, onTypeMismatchInner))
          .org(parseOrg(basicNode, onTypeMismatchInner))
          .isAnc(getBoolean(basicNode, IS_ANC, onTypeMismatchInner))
          .knd(parseKnd(basicNode, onTypeMismatchInner))
          .isTitleChanged(getBoolean(basicNode, IS_TITLE_CHANGED, onTypeMismatchInner))
          .efYd(getInt(basicNode, EF_YD, onTypeMismatchInner))
          .appendixEditYn(getBoolean(basicNode, APPENDIX_EDIT_YN, onTypeMismatchInner))
          .contactInfo(parseContactInfos(basicNode, onTypeMismatchInner))
          .articleEfYdStr(getString(basicNode, ARTICLE_EF_YD_STR, onTypeMismatchInner, true))
          .lsNmHanja(getString(basicNode, LS_NM_HANJA, onTypeMismatchInner))
          .lsNmAbbr(getString(basicNode, LS_NM_ABBR, onTypeMismatchInner))
          .ancYd(getInt(basicNode, ANC_YD, onTypeMismatchInner))
          .isHangul(getBoolean(basicNode, IS_HANGUL, onTypeMismatchInner))
          .chapter(getInt(basicNode, CHAPTER, onTypeMismatchInner))
          .build();
    } else {
      onTypeMismatch.accept(BASIC_INFO, null);
      return null;
    }
  }

  /** 공동부령정보 파싱 */
  private List<CoOrdinanceInfo> parseCoOrdinanceInfos(final JsonNode basicNode,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (basicNode != null && basicNode.has(CO_ORDINANCE_INFO)) {
      final JsonNode coOrdinanceInfosNode = basicNode.get(CO_ORDINANCE_INFO);
      try {
        final ArrayNode arrayNode = normalizeToArray(coOrdinanceInfosNode, "공동부령");
        final List<CoOrdinanceInfo> infos = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          infos.add(coOrdinanceInfoParser.parse(node));
        }
        return infos;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(CO_ORDINANCE_INFO, coOrdinanceInfosNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(CO_ORDINANCE_INFO, null);
      return null;
    }
  }

  /** 소관부처 파싱 */
  private Org parseOrg(final JsonNode basicNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (basicNode != null && basicNode.has(ORG)) {
      final JsonNode orgNode = basicNode.get(ORG);
      final BiConsumer<String, JsonNode> onTypeMismatchInner = (fieldName, jsonNode) -> onTypeMismatch
          .accept(ORG.concat(".").concat(fieldName), jsonNode);
      trackUnexpectedFields(basicNode, Set.of(ORG_NM, ORG_CD), onTypeMismatchInner);

      final String name = getString(orgNode, ORG_NM, onTypeMismatchInner);
      final String code = getString(orgNode, ORG_CD, onTypeMismatchInner);

      if (name == null && code == null) {
        return null;
      }
      return Org.builder().name(name).code(code).build();
    } else {
      onTypeMismatch.accept(ORG, null);
      return null;
    }

  }

  /** 법종구분 파싱 */
  private Knd parseKnd(final JsonNode basicNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (basicNode != null && basicNode.has(KND)) {
      final JsonNode kndNode = basicNode.get(KND);
      final BiConsumer<String, JsonNode> onTypeMismatchInner = (fieldName, jsonNode) -> onTypeMismatch
          .accept(KND.concat(".").concat(fieldName), jsonNode);
      trackUnexpectedFields(basicNode, Set.of(KND_CD, KND_NM), onTypeMismatchInner);

      final String code = getString(kndNode, KND_CD, onTypeMismatchInner);
      final String name = getString(kndNode, KND_NM, onTypeMismatchInner);
      if (name == null && code == null) {
        return null;
      }
      return Knd.builder().name(name).code(code).build();

    } else {
      onTypeMismatch.accept(KND, null);
      return null;
    }

  }

  /** 연락부서 파싱 */
  private List<Department> parseContactInfos(final JsonNode basicNode,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (basicNode != null && basicNode.has(CONTACT)) {
      final JsonNode contactInfosNode = basicNode.get(CONTACT);
      try {
        final ArrayNode arrayNode = normalizeToArray(contactInfosNode, "부서단위");
        final List<Department> infos = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          final Department info = departmentParser.parse(node);
          if (info != null) {
            infos.add(info);
          }
        }
        return infos;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(CONTACT, contactInfosNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(CONTACT, null);
      return null;
    }
  }

  /** 부칙 파싱 */
  private List<Addendum> parseAddenda(final JsonNode lawNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(ADDENDA)) {
      final JsonNode addendaNode = lawNode.get(ADDENDA);
      try {
        final ArrayNode arrayNode = normalizeToArray(addendaNode, "부칙단위");
        final List<Addendum> addenda = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          addenda.add(addendumParser.parse(node));
        }
        return addenda;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(ADDENDA, addendaNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(ADDENDA, null);
      return null;
    }
  }

  /** 조문 파싱 */
  private List<ArticleContentDto> parseArticles(final JsonNode lawNode,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(ARTICLES)) {
      final JsonNode articlesNode = lawNode.get(ARTICLES);
      try {
        final ArrayNode arrayNode = normalizeToArray(articlesNode, "조문단위");
        final List<ArticleContentDto> articles = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          articles.add(articleParser.parse(node));
        }
        return articles;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(ARTICLES, articlesNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(ARTICLES, null);
      return null;
    }
  }

  /** 제개정이유 파싱 */
  private String parseRrClsReason(final JsonNode lawNode, final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawNode != null && lawNode.has(RR_CLS_REASON)) {
      final JsonNode reasonNode = lawNode.get(RR_CLS_REASON);
      try {
        return flattenStringArray(reasonNode, RR_CLS_REASON_CONTENT, onTypeMismatch);
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(RR_CLS_REASON, reasonNode);
        return null;
      }
    } else {
      onTypeMismatch.accept(RR_CLS_REASON, null);
      return null;
    }
  }
}
