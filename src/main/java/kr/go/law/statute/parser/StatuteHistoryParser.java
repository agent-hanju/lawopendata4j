package kr.go.law.statute.parser;

import java.util.ArrayList;
import java.util.Collections;
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
import kr.go.law.statute.dto.ArticleListDto;
import kr.go.law.statute.dto.Org;
import kr.go.law.statute.dto.StatuteHistoryDto;
import lombok.extern.slf4j.Slf4j;

/** 법령 연혁 API 응답 파서 */
@Slf4j
public class StatuteHistoryParser extends BaseParser<StatuteHistoryDto> {
  private static final String ROOT_FIELD = "LawSearch";

  public StatuteHistoryParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }
  private static final String ITEMS_FIELD = "law";
  private static final String TOTAL_CNT = "totalCnt";

  private static final String STATUTE_INFO = "법령정보";

  private static final String LS_ID = "법령ID";
  private static final String MST = "법령일련번호";
  private static final String EF_YD = "시행일자";
  private static final String LS_NM = "법령명한글";
  private static final String KND_NM = "법령구분명";
  private static final String ORG_NM = "소관부처명";
  private static final String RR_CLS_NM = "제개정구분명";
  private static final String ANC_YD = "공포일자";
  private static final String ORG_CD = "소관부처코드";

  private static final String JO_LIST = "조문정보";
  private static final String JO_ITEMS = "jo";

  private static final String JO = "조문번호";
  private static final String JO_RR_CLS = "변경사유";
  private static final String JO_EF_YD = "조문시행일";
  private static final String JO_RR_CLS_YD = "조문개정일";

  private static final Set<String> KNOWN_FIELDS = Set.of("id", JO_LIST, STATUTE_INFO);
  private static final Set<String> STATUTE_FIELDS = Set.of(
      LS_ID,
      MST,
      EF_YD,
      LS_NM,
      KND_NM,
      ORG_NM,
      RR_CLS_NM,
      ANC_YD,
      ORG_CD);
  private static final Set<String> JO_FIELDS = Set.of(
      JO,
      "조문변경이력상세링크",
      "num",
      "조문링크",
      JO_RR_CLS,
      JO_EF_YD,
      JO_RR_CLS_YD);

  /**
   * History API 응답 (루트: LawSearch)에서 변경된 StatuteHistory 목록 파싱
   */
  public List<StatuteHistoryDto> parseList(JsonNode historyApiResponse) {
    if (historyApiResponse != null && historyApiResponse.has(ROOT_FIELD)) {
      final JsonNode lawSearch = historyApiResponse.get("LawSearch");
      try {
        final ArrayNode laws = normalizeToArray(lawSearch, ITEMS_FIELD);
        if (!laws.isEmpty()) {
          final List<StatuteHistoryDto> histories = new ArrayList<>();
          for (final JsonNode lawData : laws) {
            final StatuteHistoryDto history = parse(lawData);
            if (history != null) {
              histories.add(history);
            }
          }
          return histories;
        } else {
          log.debug("No data in response");
          return Collections.emptyList();
        }
      } catch (TypeMismatchException e) {
        log.warn("Invalid list response");
        return Collections.emptyList();
      }
    } else {
      log.warn("Invalid list response");
      return Collections.emptyList();
    }
  }

  /** 개별 이력 데이터 파싱 */
  @Override
  public StatuteHistoryDto parse(JsonNode lawData) {
    if (lawData == null || lawData.isMissingNode()) {
      log.warn("Invalid StatuteListDto: {}", lawData);
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(lawData, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    StatuteHistoryDto history = StatuteHistoryDto.builder().build();
    if (lawData.has(JO_LIST)) {
      final JsonNode joList = lawData.get(JO_LIST);
      try {
        final ArrayNode joArr = normalizeToArray(joList, JO_ITEMS);
        final List<ArticleListDto> infos = new ArrayList<>();
        if (!joArr.isEmpty()) {
          for (final JsonNode item : joArr) {
            final ArticleListDto info = parseArticles(item);
            if (info != null) {
              infos.add(info);
            }
          }
        }
        history.setJo(infos);
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(JO_LIST, joList);
      }
    } else {
      onTypeMismatch.accept(JO_LIST, null);
    }
    // statuteInfo는 flatten한다.
    if (lawData.has(STATUTE_INFO)) {
      final JsonNode statuteInfo = lawData.get(STATUTE_INFO);
      trackUnexpectedFields(statuteInfo, STATUTE_FIELDS, unexpected);
      history.setLsId(getInt(statuteInfo, LS_ID, onTypeMismatch));
      history.setMst(getInt(statuteInfo, MST, onTypeMismatch));
      history.setEfYd(getInt(statuteInfo, EF_YD, onTypeMismatch));
      history.setLsNm(getString(statuteInfo, LS_NM, onTypeMismatch));
      history.setKndNm(getString(statuteInfo, KND_NM, onTypeMismatch));
      history.setRrClsNm(getString(statuteInfo, RR_CLS_NM, onTypeMismatch));
      history.setAncYd(getInt(statuteInfo, ANC_YD, onTypeMismatch));
      history.setOrgs(parseOrgs(statuteInfo, onTypeMismatch));
    } else {
      onTypeMismatch.accept(STATUTE_INFO, null);
    }
    return history;
  }

  private ArticleListDto parseArticles(final JsonNode node) {
    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, JO_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);
    return ArticleListDto.builder()
        .jo(getInt(node, JO, onTypeMismatch))
        .rrCls(getString(node, JO_RR_CLS, onTypeMismatch))
        .efYd(getInt(node, JO_EF_YD, onTypeMismatch))
        .rrClsYd(getInt(node, JO_RR_CLS_YD, onTypeMismatch))
        .build();
  }

  private List<Org> parseOrgs(final JsonNode node, final BiConsumer<String, JsonNode> onTypeMismatch) {
    final List<String> orgNms = getCommaJoinedString(node, ORG_NM, onTypeMismatch, true);
    final List<String> orgCds = getCommaJoinedString(node, ORG_CD, onTypeMismatch, true);

    // 방어적 null 체크 (RuntimeException 등 예외 상황 대비)
    final int nmSize = orgNms != null ? orgNms.size() : 0;
    final int cdSize = orgCds != null ? orgCds.size() : 0;

    if (nmSize == 0 && cdSize == 0) {
      return null;
    }

    final List<Org> orgs = new ArrayList<>();
    for (int i = 0; i < Math.max(nmSize, cdSize); i++) {
      final Org org = Org.builder().build();
      if (orgNms != null && i < nmSize) {
        org.setName(orgNms.get(i));
      }
      if (orgCds != null && i < cdSize) {
        org.setCode(orgCds.get(i));
      }
      orgs.add(org);
    }
    return orgs;
  }

  /**
   * History API 응답의 totalCnt를 파싱해 총 데이터 개수를 구한다.
   *
   * @param listApiResponse List API 응답
   * @return 파싱된 totalCnt. totalCnt가 없거나 유효한 integer가 아닐 경우 -1
   */
  public int parseTotalCount(final JsonNode historyApiResponse) {
    if (historyApiResponse == null || !historyApiResponse.has(ROOT_FIELD)) {
      log.warn("Invalid list response");
      return -1;
    } else {
      final JsonNode lawSearch = historyApiResponse.get(ROOT_FIELD);
      final Integer totalCnt = getInt(lawSearch, TOTAL_CNT,
          (fieldName, jsonNode) -> log.warn("{} is not integer: {}", fieldName, jsonNode));
      if (totalCnt == null) {
        return -1;
      } else {
        return totalCnt;
      }
    }
  }
}
