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
import kr.go.law.statute.dto.Hang;
import kr.go.law.statute.dto.Ho;

/** 항 파서 (HoParser 의존) */
public class HangParser extends BaseParser<Hang> {
  private final HoParser hoParser;

  private static final Set<String> KNOWN_FIELDS = Set.of(
      "항제개정유형",
      "항번호",
      "항가지번호",
      "항내용",
      "호",
      "항제개정일자문자열");

  public HangParser(ObjectMapper objectMapper, HoParser hoParser) {
    super(objectMapper);
    this.hoParser = hoParser;
  }

  @Override
  public Hang parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Hang.builder()
        .rrCls(getString(node, "항제개정유형", onTypeMismatch))
        .no(getString(node, "항번호", onTypeMismatch))
        .brNo(getString(node, "항가지번호", onTypeMismatch))
        .content(flattenStringArray(node, "항내용", onTypeMismatch))
        .ho(parseHo(node, onTypeMismatch))
        .rrClsYdStr(getString(node, "항제개정일자문자열", onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  private List<Ho> parseHo(JsonNode node, BiConsumer<String, JsonNode> onTypeMismatch) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    if (node.has("호")) {
      final List<Ho> infos = new ArrayList<>();
      final JsonNode hoNode = node.get("호");
      if (hoNode.isArray()) {
        final ArrayNode hoArr = (ArrayNode) hoNode;
        for (final JsonNode hoItem : hoArr) {
          final Ho ho = hoParser.parse(hoItem);
          if (ho != null) {
            infos.add(ho);
          }
        }
      } else {
        onTypeMismatch.accept("호", hoNode);
        return null;
      }
      return infos;
    } else {
      onTypeMismatch.accept("호", null);
      return null;
    }
  }
}
