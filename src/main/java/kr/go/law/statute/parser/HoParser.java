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
import kr.go.law.statute.dto.Ho;
import kr.go.law.statute.dto.Mok;

/** 호 파서 (MokParser 의존) */
public class HoParser extends BaseParser<Ho> {
  private final MokParser mokParser;

  private static final Set<String> KNOWN_FIELDS = Set.of(
      "호번호", "호가지번호", "호내용", "목");

  public HoParser(ObjectMapper objectMapper, MokParser mokParser) {
    super(objectMapper);
    this.mokParser = mokParser;
  }

  @Override
  public Ho parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Ho.builder()
        .no(getString(node, "호번호", onTypeMismatch))
        .brNo(getString(node, "호가지번호", onTypeMismatch))
        .content(flattenStringArray(node, "호내용", onTypeMismatch))
        .mok(parseMok(node, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  private List<Mok> parseMok(JsonNode node, BiConsumer<String, JsonNode> onTypeMismatch) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    if (node.has("목")) {
      final List<Mok> infos = new ArrayList<>();
      final JsonNode mokNode = node.get("목");
      if (mokNode.isArray()) {
        final ArrayNode mokArr = (ArrayNode) mokNode;
        for (final JsonNode mokItem : mokArr) {
          final Mok ho = mokParser.parse(mokItem);
          if (ho != null) {
            infos.add(ho);
          }
        }
      } else {
        onTypeMismatch.accept("목", mokNode);
        return null;
      }
      return infos;
    } else {
      onTypeMismatch.accept("목", null);
      return null;
    }
  }
}
