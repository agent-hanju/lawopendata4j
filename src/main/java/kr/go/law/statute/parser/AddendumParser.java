package kr.go.law.statute.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.statute.dto.Addendum;

/** 부칙 파서 */
public class AddendumParser extends BaseParser<Addendum> {
  private static final Set<String> KNOWN_FIELDS = Set.of(
      "부칙키", "부칙공포일자", "부칙내용", "부칙공포번호");

  public AddendumParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public Addendum parse(final JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Addendum.builder()
        .key(getLong(node, "부칙키", onTypeMismatch))
        .ancYd(getInt(node, "부칙공포일자", onTypeMismatch))
        .content(flattenStringArray(node, "부칙내용", onTypeMismatch))
        .ancNo(getInt(node, "부칙공포번호", onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }
}
