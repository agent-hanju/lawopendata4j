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
import kr.go.law.statute.dto.ArticleContentDto;
import kr.go.law.statute.dto.Hang;
import lombok.extern.slf4j.Slf4j;

/** 조문 파서 (HangParser 의존) */
@Slf4j
public class ArticleParser extends BaseParser<ArticleContentDto> {
  private static final String NO = "조문번호";
  private static final String RR_CLS = "조문제개정유형";
  private static final String EF_YD = "조문시행일자";
  private static final String IS_AMENDED = "조문변경여부";
  private static final String PREV_JO = "조문이동이전";
  private static final String REFERENCE = "조문참고자료";
  private static final String KEY = "조문키";
  private static final String HANG = "항";
  private static final String CONTENT = "조문내용";
  private static final String TITLE = "조문제목";
  private static final String NEXT_JO = "조문이동이후";
  private static final String TYPE = "조문여부";
  private static final String BR_NO = "조문가지번호";

  private final HangParser hangParser;

  private static final Set<String> KNOWN_FIELDS = Set.of(
      NO,
      RR_CLS,
      EF_YD,
      IS_AMENDED,
      PREV_JO,
      REFERENCE,
      KEY,
      HANG,
      CONTENT,
      TITLE,
      NEXT_JO,
      TYPE,
      BR_NO);

  public ArticleParser(ObjectMapper objectMapper, HangParser hangParser) {
    super(objectMapper);
    this.hangParser = hangParser;
  }

  @Override
  public ArticleContentDto parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);
    return ArticleContentDto.builder()
        .no(getInt(node, NO, onTypeMismatch))
        .rrCls(getString(node, RR_CLS, onTypeMismatch))
        .brNo(getInt(node, BR_NO, onTypeMismatch, true))
        .isAmended(getBoolean(node, IS_AMENDED, onTypeMismatch))
        .prevJo(getInt(node, PREV_JO, onTypeMismatch))
        .reference(getString(node, REFERENCE, onTypeMismatch, true))
        .key(getInt(node, KEY, onTypeMismatch))
        .hang(parseHang(node, onTypeMismatch))
        .content(flattenStringArray(node, CONTENT, onTypeMismatch))
        .title(getString(node, TITLE, onTypeMismatch, true))
        .nextJo(getInt(node, NEXT_JO, onTypeMismatch))
        .type(getString(node, TYPE, onTypeMismatch))
        .efYd(getInt(node, EF_YD, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  private List<Hang> parseHang(final JsonNode node, BiConsumer<String, JsonNode> onTypeMismatch) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    if (node.has(HANG)) {
      final List<Hang> infos = new ArrayList<>();
      final JsonNode hangNode = node.get(HANG);
      if (hangNode.isArray()) {
        final ArrayNode hangArr = (ArrayNode) hangNode;
        for (final JsonNode hangItem : hangArr) {
          final Hang hang = hangParser.parse(hangItem);
          if (hang != null) {
            infos.add(hang);
          }
        }
      } else {
        onTypeMismatch.accept(HANG, hangNode);
        return null;
      }
      return infos;
    } else {
      onTypeMismatch.accept(HANG, null);
      return null;
    }
  }
}
