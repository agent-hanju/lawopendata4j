package kr.go.law.statute.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.statute.dto.Appendix;

/** 별표 파서 */
public class AppendixParser extends BaseParser<Appendix> {
  private static final Set<String> KNOWN_FIELDS = Set.of(
      "별표제목",
      "별표PDF파일명",
      "별표HWP파일명",
      "별표번호",
      "별표서식PDF파일링크",
      "별표키",
      "별표내용",
      "별표이미지파일명",
      "별표구분",
      "별표서식파일링크",
      "별표가지번호");

  public AppendixParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public Appendix parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Appendix.builder()

        .title(getString(node, "별표제목", onTypeMismatch))
        .pdfFilename(getString(node, "별표PDF파일명", onTypeMismatch))
        .hwpFilename(getString(node, "별표HWP파일명", onTypeMismatch))
        .no(getInt(node, "별표번호", onTypeMismatch))
        .pdfFileLink(getString(node, "별표서식PDF파일링크", onTypeMismatch))
        .key(getString(node, "별표키", onTypeMismatch))
        .content(flattenStringArray(node, "별표내용", onTypeMismatch))
        .imgFilenames(parseImageFilenames(node))
        .type(getString(node, "별표구분", onTypeMismatch))
        .fileLink(getString(node, "별표서식파일링크", onTypeMismatch))
        .brNo(getInt(node, "별표가지번호", onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  /**
   * 별표이미지파일명 파싱 (단일 문자열 또는 배열)
   */
  private List<String> parseImageFilenames(JsonNode node) {
    if (!node.has("별표이미지파일명")) {
      return null;
    }
    final List<String> result = new ArrayList<>();
    final JsonNode imageNode = node.get("별표이미지파일명");

    if (imageNode.isTextual()) {
      final String text = imageNode.asText().strip();
      if (!text.isEmpty()) {
        result.add(text);
      }
      return result;
    }

    if (imageNode.isArray()) {
      for (final JsonNode img : imageNode) {
        if (img.isTextual()) {
          result.add(img.asText().strip());
        }
      }
    }

    return result;
  }
}
