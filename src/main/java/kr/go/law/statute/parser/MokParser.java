package kr.go.law.statute.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.statute.dto.Mok;

/** 목 파서 */
public class MokParser extends BaseParser<Mok> {
  private static final Set<String> KNOWN_FIELDS = Set.of(
      "목번호", "목가지번호", "목내용");

  public MokParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public Mok parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Mok.builder()
        .no(getString(node, "목번호", onTypeMismatch))
        .brNo(getString(node, "목가지번호", onTypeMismatch))
        .content(flattenStringArray(node, "목내용", onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }
}
