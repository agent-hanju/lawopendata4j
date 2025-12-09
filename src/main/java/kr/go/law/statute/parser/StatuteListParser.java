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
import kr.go.law.statute.dto.CoOrdinanceInfo;
import kr.go.law.statute.dto.Org;
import kr.go.law.statute.dto.StatuteListDto;
import lombok.extern.slf4j.Slf4j;

/** 법령 목록 API 응답 파서 */
@Slf4j
public class StatuteListParser extends BaseParser<StatuteListDto> {
  private final CoOrdinanceInfoParser coOrdinanceInfoParser;

  public StatuteListParser(ObjectMapper objectMapper, CoOrdinanceInfoParser coOrdinanceInfoParser) {
    super(objectMapper);
    this.coOrdinanceInfoParser = coOrdinanceInfoParser;
  }

  private static final String ROOT_FIELD = "LawSearch";
  private static final String ITEMS_FIELD = "law";
  private static final String TOTAL_CNT = "totalCnt";

  private static final String NW = "현행연혁코드";
  private static final String MST = "법령일련번호";
  private static final String LS_NM = "법령명한글";
  private static final String KND_NM = "법령구분명";
  private static final String ORG_NM = "소관부처명";
  private static final String ANC_NO = "공포번호";
  private static final String RR_CLS_NM = "제개정구분명";
  private static final String ORG_CD = "소관부처코드";
  private static final String LS_ID = "법령ID";
  private static final String CO_ORDINANCE_INFO = "공동부령정보";
  private static final String EF_YD = "시행일자";
  private static final String ANC_YD = "공포일자";
  private static final String LS_NM_ABBR = "법령약칭명";

  private static final Set<String> KNOWN_FIELDS = Set.of(
      NW,
      MST,
      "자법타법여부", // 항상 빈 값으로 추정되어 무시
      "법령상세링크", // 재구성 가능한 필드여서 무시
      LS_NM,
      KND_NM,
      ORG_NM,
      ANC_NO,
      RR_CLS_NM,
      ORG_CD,
      "id", // 검색 결과로 결정되는 값이어서 무시
      LS_ID,
      CO_ORDINANCE_INFO,
      EF_YD,
      ANC_YD,
      LS_NM_ABBR);

  /**
   * List API 응답을 List<StatuteListDto>로 파싱한다.
   * 유효하지 않은 값은 제외하며, 유효하지 않은 반환도 빈 배열으로 반환한다.
   *
   * @param listApiResponse List API 응답을
   * @return List<StatuteListDto>
   */
  public List<StatuteListDto> parseList(final JsonNode listApiResponse) {
    if (listApiResponse != null && listApiResponse.has(ROOT_FIELD)) {
      final JsonNode lawSearch = listApiResponse.get(ROOT_FIELD);
      try {
        final ArrayNode laws = normalizeToArray(lawSearch, ITEMS_FIELD);
        if (!laws.isEmpty()) {
          final List<StatuteListDto> statutes = new ArrayList<>();
          for (final JsonNode lawData : laws) {
            final StatuteListDto dto = parse(lawData);
            if (dto != null) {
              statutes.add(dto);
            }
          }
          return statutes;
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

  /**
   * List API 응답의 totalCnt를 파싱해 총 데이터 개수를 구한다.
   *
   * @param listApiResponse List API 응답
   * @return 파싱된 totalCnt. totalCnt가 없거나 유효한 integer가 아닐 경우 -1
   */
  public int parseTotalCount(final JsonNode listApiResponse) {
    if (listApiResponse == null || !listApiResponse.has(ROOT_FIELD)) {
      log.warn("Invalid list response");
      return -1;
    } else {
      final JsonNode lawSearch = listApiResponse.get(ROOT_FIELD);
      final Integer totalCnt = getInt(lawSearch, TOTAL_CNT,
          (fieldName, jsonNode) -> log.warn("{} is not integer: {}", fieldName, jsonNode));
      if (totalCnt == null) {
        return -1;
      } else {
        return totalCnt;
      }
    }
  }

  /** 개별 법령 데이터 파싱 */
  @Override
  public StatuteListDto parse(final JsonNode lawData) {
    if (lawData == null || lawData.isMissingNode()) {
      log.warn("Invalid StatuteListDto: {}", lawData);
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(lawData, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    // ==========
    // 자법타법여부 특수처리(빈값이 아닌 경우만을 감지하기 위한 임시 코드)
    // 해당 필드가 실제 유의미한 값을 가지는지 확인되지 않아 이렇게 체크 중이다.
    String lawType = getString(lawData, "자법타법여부", onTypeMismatch);
    if (lawType != null && !lawType.isEmpty()) {
      unexpected.put("자법타법여부", lawType);
    }
    // ==========

    return StatuteListDto.builder()
        .nw(getString(lawData, NW, onTypeMismatch))
        .mst(getInt(lawData, MST, onTypeMismatch))
        .lsNm(getString(lawData, LS_NM, onTypeMismatch))
        .kndNm(getString(lawData, KND_NM, onTypeMismatch))
        .ancNo(getInt(lawData, ANC_NO, onTypeMismatch))
        .rrClsNm(getString(lawData, RR_CLS_NM, onTypeMismatch))
        .lsId(getInt(lawData, LS_ID, onTypeMismatch))
        .coOrdinanceInfos(parseCoOrdinanceInfos(lawData, onTypeMismatch))
        .efYd(getInt(lawData, EF_YD, onTypeMismatch))
        .ancYd(getInt(lawData, ANC_YD, onTypeMismatch))
        .lsNmAbbr(getString(lawData, LS_NM_ABBR, onTypeMismatch))
        .orgs(parseOrgs(lawData, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  private List<CoOrdinanceInfo> parseCoOrdinanceInfos(
      final JsonNode lawData,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    if (lawData != null && lawData.has(CO_ORDINANCE_INFO)) {
      final JsonNode coOrdinanceInfoNode = lawData.get(CO_ORDINANCE_INFO);
      try {
        final ArrayNode arrayNode = normalizeToArray(coOrdinanceInfoNode, "공동부령");
        final List<CoOrdinanceInfo> infos = new ArrayList<>();
        for (final JsonNode node : arrayNode) {
          final CoOrdinanceInfo info = coOrdinanceInfoParser.parse(node);
          if (info != null) {
            infos.add(info);
          }
        }
        return infos;
      } catch (TypeMismatchException e) {
        onTypeMismatch.accept(CO_ORDINANCE_INFO, lawData);
        return null;
      }
    } else {
      onTypeMismatch.accept(CO_ORDINANCE_INFO, null);
      return null;
    }
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
}
