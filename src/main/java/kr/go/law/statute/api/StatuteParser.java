package kr.go.law.statute.api;

import static kr.go.law.util.JsonParserUtil.flattenStringArray;
import static kr.go.law.util.JsonParserUtil.getInt;
import static kr.go.law.util.JsonParserUtil.getJsonObject;
import static kr.go.law.util.JsonParserUtil.getLong;
import static kr.go.law.util.JsonParserUtil.getString;
import static kr.go.law.util.JsonParserUtil.normalizeToArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.go.law.statute.dto.Addendum;
import kr.go.law.statute.dto.Appendix;
import kr.go.law.statute.dto.ArticleDto;
import kr.go.law.statute.dto.CoEnactment;
import kr.go.law.statute.dto.Department;
import kr.go.law.statute.dto.Hang;
import kr.go.law.statute.dto.Ho;
import kr.go.law.statute.dto.Mok;
import kr.go.law.statute.dto.StatuteDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 법령정보 API 응답 파서
 */
@Slf4j
public class StatuteParser {
  private final ObjectMapper objectMapper;

  public StatuteParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * API 응답에서 totalCnt를 파싱합니다.
   *
   * @param response API 응답
   * @param rootKey  루트 키 (LawSearch, LsJoHst 등)
   * @return totalCnt 값
   */
  public int parseTotalCount(JsonNode response, String rootKey) {
    if (response == null || !response.has(rootKey)) {
      return 0;
    }
    String totalCntStr = getString(response.get(rootKey), "totalCnt");
    return totalCntStr != null ? Integer.parseInt(totalCntStr) : 0;
  }

  /**
   * List API 응답에서 StatuteDto 목록을 파싱합니다.
   *
   * @param listApiResponse List API 전체 응답 (루트: LawSearch)
   * @return StatuteDto 목록
   */
  public List<StatuteDto> parseList(JsonNode listApiResponse) {
    if (listApiResponse == null || !listApiResponse.has("LawSearch")) {
      log.warn("Invalid list response");
      return Collections.emptyList();
    }
    final JsonNode lawSearch = listApiResponse.get("LawSearch");
    if (!lawSearch.has("law")) {
      log.debug("No data in response");
      return Collections.emptyList();
    }
    final ArrayNode laws = normalizeToArray(lawSearch.get("law"));
    final List<StatuteDto> statutes = new ArrayList<>();
    for (int i = 0; i < laws.size(); i++) {
      JsonNode lawData = laws.get(i);

      String mstStr = getString(lawData, "법령일련번호");
      if (mstStr == null || mstStr.trim().isEmpty()) {
        throw new IllegalArgumentException("Missing 법령일련번호");
      }
      Integer mst = Integer.parseInt(mstStr);

      String lawName = getString(lawData, "법령명한글");
      if (lawName == null || lawName.trim().isEmpty()) {
        throw new IllegalArgumentException("Missing 법령명한글");
      }

      Integer lsId = getInt(lawData, "법령ID");
      if (lsId == null) {
        throw new IllegalArgumentException("Missing 법령ID");
      }

      Integer ancYd = getInt(lawData, "공포일자");
      if (ancYd == null) {
        throw new IllegalArgumentException("Missing 공포일자");
      }

      Integer ancNo = getInt(lawData, "공포번호");
      if (ancNo == null) {
        throw new IllegalArgumentException("Missing 공포번호");
      }

      StatuteDto statute = StatuteDto.builder()
          .mst(mst)
          .efYd(getInt(lawData, "시행일자"))
          .lsId(lsId)
          .statusCode(getString(lawData, "현행연혁코드"))
          .lawType(getString(lawData, "자법타법여부"))
          .lsNm(lawName)
          .lsNmHanja(getString(lawData, "법령명한자"))
          .lsNmAbbr(getString(lawData, "법령약칭명"))
          .ancYd(ancYd)
          .ancNo(ancNo)
          .enactmentType(getString(lawData, "제개정구분명"))
          .orgCd(splitByComma(getString(lawData, "소관부처코드")))
          .org(splitByComma(getString(lawData, "소관부처명")))
          .knd(getString(lawData, "법령구분명"))
          .build();

      statutes.add(statute);
    }
    return statutes;
  }

  /**
   * History API 응답에서 변경된 ArticleDto 목록을 파싱합니다.
   *
   * @param historyApiResponse History API 전체 응답 (루트: LawSearch)
   * @return List of ArticleDto (isChanged=true, content=null)
   */
  public List<ArticleDto> parseHistory(JsonNode historyApiResponse) {
    final List<ArticleDto> result = new ArrayList<>();

    if (historyApiResponse == null || !historyApiResponse.has("LawSearch")) {
      log.debug("No history data in response");
      return result;
    }

    final JsonNode lawSearch = historyApiResponse.get("LawSearch");
    if (!lawSearch.has("law")) {
      log.debug("No law in History API response");
      return result;
    }

    final ArrayNode lawArray = normalizeToArray(lawSearch.get("law"));

    for (int i = 0; i < lawArray.size(); i++) {
      try {
        final JsonNode lawItem = lawArray.get(i);
        if (lawItem == null || !lawItem.has("조문정보")) {
          continue;
        }

        final JsonNode joInfo = lawItem.get("조문정보");
        if (!joInfo.has("jo")) {
          continue;
        }

        final ArrayNode joArray = normalizeToArray(joInfo.get("jo"));

        for (int j = 0; j < joArray.size(); j++) {
          try {
            final JsonNode joNode = joArray.get(j);
            if (joNode == null) {
              continue;
            }
            Integer joKey = getInt(joNode, "조문번호");
            if (joKey == null) {
              throw new IllegalArgumentException("조문번호 cannot be null");
            }
            Integer joNum = joKey / 100;
            Integer joBrNum = joKey % 100;

            Integer efYd = getInt(joNode, "조문시행일");
            if (efYd == null) {
              throw new IllegalArgumentException("조문시행일 cannot be null");
            }

            // unexpectedFieldMap 처리 - 무시할 필드 제외
            Map<String, String> unexpectedFieldMap = new HashMap<>();
            trackUnexpectedFields(
                joNode,
                Set.of("num", "조문변경이력상세링크", "조문링크", "변경사유", "조문번호", "조문시행일", "조문개정일"),
                unexpectedFieldMap::put);

            final ArticleDto jo = ArticleDto.builder()
                .joKey(joKey)
                .joNum(joNum)
                .joBrNum(joBrNum == 0 ? null : joBrNum)
                .joType("조문")
                .amendedType(getString(joNode, "변경사유"))
                .isChanged(true)
                .efYd(efYd)
                .unexpectedFieldMap(unexpectedFieldMap.isEmpty() ? null : unexpectedFieldMap)
                .build();
            result.add(jo);
          } catch (Exception e) {
            log.error("Failed to parse history jo: {}", e.getMessage(), e);
          }
        }
      } catch (Exception e) {
        log.error("Failed to parse history law item: {}", e.getMessage(), e);
      }
    }

    return result;
  }

  /**
   * Content API 응답을 파싱하여 StatuteDto를 반환합니다.
   * articles 필드에 파싱된 ArticleDto들이 포함됩니다.
   *
   * @param contentApiResponse Content API 전체 응답 (루트: 법령)
   * @return 파싱된 StatuteDto (null if no response)
   */
  public StatuteDto parseContent(JsonNode contentApiResponse) {
    if (contentApiResponse == null || !contentApiResponse.has("법령")) {
      log.debug("No data in response");
      return null;
    }
    final JsonNode lawNode = contentApiResponse.get("법령");

    StatuteDto statute = parseStatute(lawNode);

    final Map<Integer, ArticleDto> articleMap = new HashMap<>();
    if (lawNode.has("조문") && lawNode.get("조문").has("조문단위")) {
      final ArrayNode joArray = normalizeToArray(lawNode.get("조문").get("조문단위"));

      for (int i = 0; i < joArray.size(); i++) {
        try {
          final JsonNode joNode = joArray.get(i);
          final ArticleDto parsed = parseArticle(joNode, statute);
          articleMap.put(parsed.getJoKey(), parsed);
        } catch (Exception e) {
          log.error("Failed to parse content jo for mst={}: {}", statute.getMst(), e.getMessage(), e);
        }
      }
    }
    statute.setArticles(articleMap);
    return statute;
  }

  // ====================================================================
  // Content API 본문 수집
  // ====================================================================

  /**
   * Content API의 "법령" 노드를 파싱하여 StatuteDto를 반환합니다.
   * (조문은 제외 - ArticleDto로 별도 저장)
   *
   * @param lawNode Content API 응답의 "법령" 노드 (기본정보, 부칙, 별표 등 포함)
   * @return 파싱된 StatuteDto
   */
  private StatuteDto parseStatute(JsonNode lawNode) {
    if (lawNode == null) {
      log.warn("parseStatute called with null lawNode");
      return null;
    }

    try {
      // Navigate to "기본정보"
      JsonNode basicInfo = lawNode.path("기본정보");

      // ===== 기본 메타데이터 필드 파싱 =====
      Integer mst = getInt(basicInfo, "법령일련번호");
      Integer efYd = getInt(basicInfo, "시행일자");
      Integer lsId = getInt(basicInfo, "법령ID");
      String lsNm = getString(basicInfo, "법령명_한글");
      String lsNmHanja = getString(basicInfo, "법령명_한자");
      String lsNmAbbr = getString(basicInfo, "법령명약칭");
      Integer ancYd = getInt(basicInfo, "공포일자");
      Integer ancNo = getInt(basicInfo, "공포번호");
      String enactmentType = getString(basicInfo, "제개정구분");

      // orgCd, org - nested: 소관부처 (콤마 구분 → List)
      List<String> orgCd = null;
      List<String> org = null;
      JsonNode orgNode = getJsonObject(basicInfo, "소관부처");
      if (orgNode != null) {
        orgCd = splitByComma(getString(orgNode, "소관부처코드"));
        org = splitByComma(getString(orgNode, "content"));
      }

      // knd, kndCd - nested: 법종구분
      String knd = null;
      String kndCd = null;
      JsonNode kndNode = getJsonObject(basicInfo, "법종구분");
      if (kndNode != null) {
        knd = getString(kndNode, "content");
        kndCd = getString(kndNode, "법종구분코드");
      }

      String isAnc = getString(basicInfo, "공포법령여부");
      Integer pyeonjangjeolgwan = getInt(basicInfo, "편장절관");
      String decisionBody = getString(basicInfo, "의결구분");
      String proposerType = getString(basicInfo, "제안구분");
      String phoneNumber = getString(basicInfo, "전화번호");
      String language = getString(basicInfo, "언어");
      String appendixEditYn = getString(basicInfo, "별표편집여부");

      // amendmentReason, amendment
      String amendmentReason = null;
      JsonNode amendmentReasonNode = lawNode.path("제개정이유").path("제개정이유내용");
      if (!amendmentReasonNode.isMissingNode()) {
        amendmentReason = amendmentReasonNode.toString();
      }

      String amendment = null;
      JsonNode amendmentNode = lawNode.path("개정문").path("개정문내용");
      if (!amendmentNode.isMissingNode()) {
        amendment = amendmentNode.toString();
      }

      // ===== 컬렉션 필드 파싱 =====
      List<Department> contactInfo = null;
      JsonNode departmentsNode = basicInfo.path("연락부서").path("부서단위");
      if (!departmentsNode.isMissingNode()) {
        contactInfo = normalizeToList(basicInfo.path("연락부서"), "부서단위", Department.class,
            this::parseDepartment);
      }

      List<CoEnactment> coEnactments = null;
      JsonNode coEnactmentsNode = basicInfo.path("공동부령정보");
      if (!coEnactmentsNode.isMissingNode() && !coEnactmentsNode.isNull() && !coEnactmentsNode.asText().isEmpty()) {
        coEnactments = new ArrayList<>();
        if (coEnactmentsNode.isArray()) {
          for (JsonNode item : coEnactmentsNode) {
            coEnactments.add(parseCoEnactment(item));
          }
        } else if (coEnactmentsNode.isObject()) {
          coEnactments.add(parseCoEnactment(coEnactmentsNode));
        }
      }

      List<Addendum> addenda = null;
      JsonNode addendaNode = lawNode.path("부칙").path("부칙단위");
      if (!addendaNode.isMissingNode()) {
        addenda = normalizeToList(lawNode.path("부칙"), "부칙단위", Addendum.class, this::parseAddendum);
      }

      List<Appendix> appendices = null;
      JsonNode appendicesNode = lawNode.path("별표").path("별표단위");
      if (!appendicesNode.isMissingNode()) {
        appendices = normalizeToList(lawNode.path("별표"), "별표단위", Appendix.class, this::parseAppendix);
      }

      StatuteDto statute = StatuteDto.builder()
          .mst(mst)
          .efYd(efYd)
          .lsId(lsId)
          .lsNm(lsNm)
          .lsNmHanja(lsNmHanja)
          .lsNmAbbr(lsNmAbbr)
          .ancYd(ancYd)
          .ancNo(ancNo)
          .enactmentType(enactmentType)
          .orgCd(orgCd)
          .org(org)
          .knd(knd)
          .kndCd(kndCd)
          .isAnc(isAnc)
          .pyeonjangjeolgwan(pyeonjangjeolgwan)
          .decisionBody(decisionBody)
          .proposerType(proposerType)
          .phoneNumber(phoneNumber)
          .language(language)
          .appendixEditYn(appendixEditYn)
          .contactInfo(contactInfo)
          .coEnactments(coEnactments)
          .amendment(amendment)
          .amendmentReason(amendmentReason)
          .addenda(addenda)
          .appendices(appendices)
          .build();

      log.debug("Successfully parsed StatuteDto: mst={}, efYd={}, lsNm={}",
          statute.getMst(), statute.getEfYd(), statute.getLsNm());

      return statute;

    } catch (Exception e) {
      log.error("Failed to parse StatuteDto from raw_json", e);
      throw new RuntimeException("Parse error", e);
    }
  }

  /**
   * 부서 파싱
   */
  private Department parseDepartment(JsonNode node) {
    try {
      String 부서키 = getString(node, "부서키");
      Integer 소관부처코드 = getInt(node, "소관부처코드");
      String 소관부처명 = getString(node, "소관부처명");
      String 부서명 = getString(node, "부서명");
      String 부서연락처 = getString(node, "부서연락처");

      Map<String, String> unexpectedFieldMap = new HashMap<>();
      trackUnexpectedFields(
          node,
          Set.of("부서키", "소관부처코드", "소관부처명", "부서명", "부서연락처"),
          unexpectedFieldMap::put);

      return new Department(부서키, 소관부처코드, 소관부처명, 부서명, 부서연락처, unexpectedFieldMap);

    } catch (Exception e) {
      log.error("Failed to parse Department: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Department(null, null, null, null, null, errorMap);
    }
  }

  /**
   * 부칙 파싱
   */
  private Addendum parseAddendum(JsonNode node) {
    try {
      Long 부칙키 = getLong(node, "부칙키");
      Integer 부칙공포일자 = getInt(node, "부칙공포일자");
      String 부칙내용 = flattenStringArray(node.get("부칙내용"));
      Integer 부칙공포번호 = getInt(node, "부칙공포번호");

      Map<String, String> metadata = new HashMap<>();
      trackUnexpectedFields(
          node,
          Set.of("부칙키", "부칙공포일자", "부칙내용", "부칙공포번호"),
          metadata::put);

      return new Addendum(부칙키, 부칙공포일자, 부칙내용, 부칙공포번호, metadata);

    } catch (Exception e) {
      log.error("Failed to parse Addendum: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Addendum(null, null, null, null, errorMap);
    }
  }

  /**
   * 별표 파싱
   */
  private Appendix parseAppendix(JsonNode node) {
    try {
      String 별표키 = getString(node, "별표키");
      String 별표구분 = getString(node, "별표구분");
      Integer 별표번호 = getInt(node, "별표번호");
      Integer 별표가지번호 = getInt(node, "별표가지번호");
      String 별표제목문자열 = getString(node, "별표제목문자열");
      String 별표내용 = flattenStringArray(node.get("별표내용"));
      String 별표서식파일링크 = getString(node, "별표서식파일링크");
      String 별표서식PDF파일링크 = getString(node, "별표서식PDF파일링크");
      Integer 별표시행일자 = getInt(node, "별표시행일자");
      String 별표제목 = getString(node, "별표제목");
      String 별표PDF파일명 = getString(node, "별표PDF파일명");
      String 별표HWP파일명 = getString(node, "별표HWP파일명");

      List<String> 별표이미지파일명 = null;
      if (node.has("별표이미지파일명")) {
        JsonNode imageNode = node.get("별표이미지파일명");
        if (imageNode.isTextual()) {
          별표이미지파일명 = Collections.singletonList(imageNode.asText());
        } else if (imageNode.isArray()) {
          별표이미지파일명 = new ArrayList<>();
          for (JsonNode img : imageNode) {
            별표이미지파일명.add(img.asText());
          }
        }
      }

      Map<String, String> metadata = new HashMap<>();
      trackUnexpectedFields(
          node,
          Set.of("별표키", "별표구분", "별표번호", "별표가지번호", "별표제목문자열",
              "별표내용", "별표서식파일링크", "별표서식PDF파일링크", "별표시행일자",
              "별표제목", "별표PDF파일명", "별표HWP파일명", "별표이미지파일명"),
          metadata::put);

      return new Appendix(별표키, 별표구분, 별표번호, 별표가지번호, 별표제목문자열,
          별표내용, 별표서식파일링크, 별표서식PDF파일링크, 별표시행일자,
          별표제목, 별표PDF파일명, 별표HWP파일명, 별표이미지파일명, metadata);

    } catch (Exception e) {
      log.error("Failed to parse Appendix: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Appendix(null, null, null, null, null, null, null, null, null, null, null, null, null, errorMap);
    }
  }

  /**
   * 공동부령 파싱
   */
  private CoEnactment parseCoEnactment(JsonNode node) {
    try {
      Integer no = getInt(node, "no");
      Integer 공포번호 = getInt(node, "공포번호");

      String 공동부령구분 = null;
      String 공동부령구분코드 = null;
      if (node.has("공동부령구분")) {
        JsonNode typeNode = node.get("공동부령구분");
        if (typeNode.isObject()) {
          공동부령구분 = getString(typeNode, "content");
          공동부령구분코드 = getString(typeNode, "구분코드");
        }
      }

      Map<String, String> unexpectedFieldMap = new HashMap<>();
      trackUnexpectedFields(
          node,
          Set.of("no", "공포번호", "공동부령구분"),
          unexpectedFieldMap::put);

      return new CoEnactment(no, 공포번호, 공동부령구분, 공동부령구분코드, unexpectedFieldMap);

    } catch (Exception e) {
      log.error("Failed to parse CoEnactment: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new CoEnactment(null, null, null, null, errorMap);
    }
  }

  // ====================================================================
  // Content API Article 수집
  // ====================================================================

  /**
   * 조문단위 JSON을 ArticleDto로 파싱합니다.
   * (Low-level: 단일 조문 노드 → ArticleDto)
   *
   * @param joNode  조문단위 JSON 노드
   * @param statute 부모 StatuteDto (denormalized 필드 제공)
   * @return ArticleDto
   */
  private ArticleDto parseArticle(JsonNode joNode, StatuteDto statute) {
    if (joNode == null || statute == null) {
      throw new IllegalArgumentException("joNode and statute cannot be null");
    }

    // Parse 조문번호 and 조문가지번호
    Integer joNum = getInt(joNode, "조문번호");
    Integer joBrNum = getInt(joNode, "조문가지번호");
    Integer joUnique = getInt(joNode, "조문키");

    if (joNum == null) {
      throw new IllegalArgumentException("조문번호 cannot be null");
    }

    // Calculate joKey
    Integer joKey = calculateJoKey(joNum, joBrNum);

    // Parse 항 (List<Hang>)
    List<Hang> hangList = normalizeToList(joNode, "항", Hang.class, this::parseHang);

    // Track unexpected fields (metadata)
    Map<String, String> unexpectedFieldMap = new HashMap<>();
    trackUnexpectedFields(
        joNode,
        Set.of("조문번호", "조문가지번호", "조문제목", "조문내용", "조문여부", "항",
            "조문제개정유형", "조문제개정일자문자열", "조문변경여부", "조문이동이전", "조문이동이후",
            "조문시행일자", "조문참고자료", "제명변경여부", "한글법령여부"),
        unexpectedFieldMap::put);

    // Build ArticleDto
    return ArticleDto.builder()
        .mst(statute.getMst())
        .joUnique(joUnique)
        .joKey(joKey)
        .lsId(statute.getLsId())
        .ancYd(statute.getAncYd())
        .ancNo(statute.getAncNo())
        .lsNm(statute.getLsNm())
        .joNum(joNum)
        .joBrNum(joBrNum)
        .joTitle(getString(joNode, "조문제목"))
        .joType(getString(joNode, "조문여부"))
        .content(joNode.has("조문내용") ? flattenStringArray(joNode.get("조문내용")) : null)
        .hangList(hangList)
        .amendedType(getString(joNode, "조문제개정유형"))
        .amendedDateStr(getString(joNode, "조문제개정일자문자열"))
        .isAmended(getString(joNode, "조문변경여부"))
        .isChanged(false)
        .efYd(getInt(joNode, "조문시행일자"))
        .prevJoKey(getInt(joNode, "조문이동이전"))
        .nextJoKey(getInt(joNode, "조문이동이후"))
        .joRef(getString(joNode, "조문참고자료"))
        .titleChanged(getString(joNode, "제명변경여부"))
        .isHangul(getString(joNode, "한글법령여부"))
        .unexpectedFieldMap(unexpectedFieldMap.isEmpty() ? null : unexpectedFieldMap)
        .build();
  }

  /**
   * 항 파싱
   */
  private Hang parseHang(JsonNode node) {
    try {
      String 항번호 = getString(node, "항번호");
      String 항가지번호 = getString(node, "항가지번호");
      String 항내용 = flattenStringArray(node.get("항내용"));
      List<Ho> 호 = normalizeToList(node, "호", Ho.class, this::parseHo);
      String 항제개정유형 = getString(node, "항제개정유형");
      String 항제개정일자 = getString(node, "항제개정일자");
      String 항제개정일자문자열 = getString(node, "항제개정일자문자열");

      Map<String, String> unexpectedFieldMap = new HashMap<>();
      trackUnexpectedFields(
          node,
          Set.of("항번호", "항가지번호", "항내용", "호", "항제개정유형", "항제개정일자", "항제개정일자문자열"),
          unexpectedFieldMap::put);

      return new Hang(항번호, 항가지번호, 항내용, 호, 항제개정유형, 항제개정일자, 항제개정일자문자열, unexpectedFieldMap);

    } catch (Exception e) {
      log.error("Failed to parse Hang: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Hang(null, null, null, null, null, null, null, errorMap);
    }
  }

  /**
   * 호 파싱
   */
  private Ho parseHo(JsonNode node) {
    try {
      String 호번호 = getString(node, "호번호");
      String 호가지번호 = getString(node, "호가지번호");
      String 호내용 = flattenStringArray(node.get("호내용"));
      List<Mok> 목 = normalizeToList(node, "목", Mok.class, this::parseMok);

      Map<String, String> unexpectedFieldMap = new HashMap<>();
      trackUnexpectedFields(node, Set.of("호번호", "호가지번호", "호내용", "목"), unexpectedFieldMap::put);

      return new Ho(호번호, 호가지번호, 호내용, 목, unexpectedFieldMap);

    } catch (Exception e) {
      log.error("Failed to parse Ho: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Ho(null, null, null, null, errorMap);
    }
  }

  /**
   * 목 파싱 (최하위)
   */
  private Mok parseMok(JsonNode node) {
    try {
      String 목번호 = getString(node, "목번호");
      String 목가지번호 = getString(node, "목가지번호");
      String 목내용 = flattenStringArray(node.get("목내용"));

      Map<String, String> unexpectedFieldMap = new HashMap<>();
      trackUnexpectedFields(node, Set.of("목번호", "목가지번호", "목내용"), unexpectedFieldMap::put);

      return new Mok(목번호, 목가지번호, 목내용, unexpectedFieldMap);

    } catch (Exception e) {
      log.error("Failed to parse Mok: {}", node, e);
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("$._error", e.getMessage());
      return new Mok(null, null, null, errorMap);
    }
  }

  // ====================================================================
  // 유틸리티
  // ====================================================================

  /**
   * 조문번호와 조문가지번호로부터 joKey를 계산합니다.
   *
   * joKey = 조문번호 * 100 + (조문가지번호 ?? 0)
   *
   * 예시:
   * - 조문10 → joKey = 1000
   * - 조문10의2 → joKey = 1002
   * - 조문17 → joKey = 1700
   *
   * @param joNum   조문번호 (필수)
   * @param joBrNum 조문가지번호 (선택, null이면 0으로 처리)
   * @return joKey
   * @throws IllegalArgumentException 조문번호가 null인 경우
   */
  private Integer calculateJoKey(Integer joNum, Integer joBrNum) {
    if (joNum == null) {
      throw new IllegalArgumentException("조문번호 cannot be null");
    }
    return joNum * 100 + (joBrNum != null ? joBrNum : 0);
  }

  /**
   * "" | 단일 | 배열 → List 정규화 (커스텀 파서 버전)
   *
   * @param parent    부모 노드
   * @param fieldName 필드명
   * @param dtoClass  DTO 클래스
   * @param parser    파싱 함수
   * @return 정규화된 리스트 (null/empty → empty list)
   */
  private <T> List<T> normalizeToList(
      JsonNode parent,
      String fieldName,
      Class<T> dtoClass,
      Function<JsonNode, T> parser) {
    if (parent == null || parent.isMissingNode()) {
      return Collections.emptyList();
    }

    JsonNode fieldNode = parent.get(fieldName);

    if (fieldNode == null || fieldNode.isMissingNode()) {
      return Collections.emptyList();
    }

    // 빈 문자열
    if (fieldNode.isTextual() && fieldNode.asText().isEmpty()) {
      return Collections.emptyList();
    }

    // 배열
    if (fieldNode.isArray()) {
      List<T> result = new ArrayList<>();
      for (JsonNode element : fieldNode) {
        result.add(parser.apply(element));
      }
      return result;
    }

    // 단일 객체
    if (fieldNode.isObject()) {
      return Collections.singletonList(parser.apply(fieldNode));
    }

    log.warn("Unexpected node type for {}: {}", fieldName, fieldNode.getNodeType());
    return Collections.emptyList();
  }

  /**
   * 미지의 필드 추적 (metadata 기록)
   *
   * @param node             JSON 노드
   * @param knownFields      알려진 필드 목록
   * @param metadataRecorder metadata 기록 함수
   */
  private void trackUnexpectedFields(
      JsonNode node,
      Set<String> knownFields,
      BiConsumer<String, String> metadataRecorder) {
    node.fields().forEachRemaining(entry -> {
      String fieldName = entry.getKey();
      if (!knownFields.contains(fieldName)) {
        try {
          String jsonValue = objectMapper.writeValueAsString(entry.getValue());
          metadataRecorder.accept("$." + fieldName, jsonValue);
          log.warn("Unknown field detected: {} = {}", fieldName, jsonValue);
        } catch (JsonProcessingException e) {
          log.error("Failed to serialize unknown field: {}", fieldName, e);
        }
      }
    });
  }

  /**
   * 콤마로 구분된 문자열을 List로 분리
   *
   * @param value 콤마 구분 문자열 (예: "법제처,행정안전부")
   * @return 분리된 List (null/빈값 → null)
   */
  private List<String> splitByComma(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Arrays.asList(value.split(","));
  }
}
