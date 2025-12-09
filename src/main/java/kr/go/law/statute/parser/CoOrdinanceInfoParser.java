package kr.go.law.statute.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.statute.dto.CoOrdinanceInfo;
import kr.go.law.statute.dto.Ordinance;

/** 공동부령정보 파서 */
public class CoOrdinanceInfoParser extends BaseParser<CoOrdinanceInfo> {
  private static final Set<String> KNOWN_FIELDS = Set.of(
      "no", "공포번호", "공동부령구분");

  public CoOrdinanceInfoParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public CoOrdinanceInfo parse(final JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    final Ordinance ordinance;
    if (node.has("공동부령구분")) {
      final JsonNode ordinanceNode = node.get("공동부령구분");
      if (ordinanceNode.isTextual()) {
        final String text = node.asText().strip();
        if (!text.isEmpty()) {
          ordinance = Ordinance.builder().name(text).build();
        } else {
          onTypeMismatch.accept("공동부령구분", ordinanceNode);
          ordinance = null;
        }
      } else if (ordinanceNode.isObject()) {
        final BiConsumer<String, JsonNode> onTypeMismatchInner = (fieldName, jsonNode) -> onTypeMismatch
            .accept("공동부령구분".concat(".").concat(fieldName), jsonNode);
        trackUnexpectedFields(node, Set.of("구분코드", "content"), onTypeMismatchInner);
        ordinance = Ordinance.builder()
            .name(getString(node, "content", onTypeMismatchInner))
            .code(getString(node, "구분코드", onTypeMismatchInner))
            .build();
      } else {
        onTypeMismatch.accept("공동부령구분", ordinanceNode);
        ordinance = null;
      }
    } else {
      onTypeMismatch.accept("공동부령구분", null);
      ordinance = null;
    }

    return CoOrdinanceInfo.builder()
        .no(getInt(node, "no", onTypeMismatch))
        .ancNo(getInt(node, "공포번호", onTypeMismatch))
        .ordinance(ordinance)
        .unexpected(unexpected)
        .build();
  }
}
