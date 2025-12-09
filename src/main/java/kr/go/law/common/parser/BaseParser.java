package kr.go.law.common.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import kr.go.law.common.dto.BaseDto;

/**
 * JsonNode → DTO 변환을 담당하는 Parser 추상 클래스
 *
 * @param <T> 파싱 결과 DTO 타입 (BaseDto를 상속해야 함)
 */
public abstract class BaseParser<T extends BaseDto> {
  private static final Logger log = LoggerFactory.getLogger(BaseParser.class);
  protected final ObjectMapper objectMapper;

  protected BaseParser(ObjectMapper objectMapper) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
  }

  /**
   * JsonNode를 DTO로 변환합니다.
   *
   * @param node 파싱할 JSON 노드
   * @return 파싱된 DTO (node가 null이거나 missing인 경우 null)
   */
  public abstract T parse(JsonNode node);

  /**
   * JsonNode에서 문자열 값을 추출합니다.
   *
   * @param node      JSON 노드
   * @param fieldName 필드명
   * @return 문자열 값(빈 문자열이거나 타입 불일치 시 null 반환)
   */
  protected String getString(JsonNode node, String fieldName) {
    return getString(node, fieldName, null, false);
  }

  /**
   * JsonNode에서 문자열 값을 추출합니다.
   * 문자열이 아닌 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return 문자열 값(빈 문자열이거나 타입 불일치 시 null 반환)
   */
  protected String getString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    return getString(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 문자열 값을 추출합니다.
   * 문자열이 아닌 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return 문자열 값(빈 문자열이거나 타입 불일치 시 null 반환)
   */
  protected String getString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch, final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      // LawOpenData Open API에서 빈 문자열은 null을 의미한다.
      if (text.isEmpty()) {
        return null;
      } else {
        return text;
      }
    }
    // 문자열이 아닐 경우 타입 불일치로 처리
    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * 날짜를 반환할 것으로 예상되는 필드에서 yyyymmdd형태의 Integer를 추출합니다.
   * 예상한 값이 아닐 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return 8자리 integer
   */
  protected Integer getDateInString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    return getDateInString(node, fieldName, onTypeMismatch, false);
  }

  /**
   * 날짜를 반환할 것으로 예상되는 필드에서 yyyymmdd형태의 Integer를 추출합니다.
   * 예상한 값이 아닐 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return 8자리 integer
   */
  protected Integer getDateInString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch, final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }

    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isInt()) {
      final int digits = fieldNode.asInt();
      if (9999999 < digits && digits < 100000000) { // 8자리 수
        return digits;
      }
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      // LawOpenData Open API에서 빈 문자열은 null을 의미한다.
      if (text.isEmpty()) {
        return null;
      }
      final String[] parts = text.split("[.\\s]+");
      if (parts.length >= 3) {
        try {
          int year = Integer.parseInt(parts[0].strip());
          int month = Integer.parseInt(parts[1].strip());
          int day = Integer.parseInt(parts[2].strip());
          return year * 10000 + month * 100 + day;
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
    }
    // 변환할 수 없는 값의 경우
    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * JsonNode에서 ","로 연결된 문자열을 문자열 리스트로 추출합니다. 빈 문자열의 경우 empty list를 반환합니다.
   * 그 외의 경우 onTypeMismatch 콜백을 호출하고 null을 반환합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 예상된 값이 아닐 경우 호출될 콜백 (fieldName, actualValue)
   * @return String List(onTypeMismatch 발생 시 null)
   */
  @Nullable
  protected List<String> getCommaJoinedString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    return getCommaJoinedString(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 ","로 연결된 문자열을 문자열 리스트로 추출합니다. 빈 문자열의 경우 empty list를 반환합니다.
   * 그 외의 경우 onTypeMismatch 콜백을 호출하고 null을 반환합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 예상된 값이 아닐 경우 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return String List(onTypeMismatch 발생 시 null)
   */
  @Nullable
  protected List<String> getCommaJoinedString(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch, final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return Collections.emptyList();
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      if (text.isEmpty()) {
        return Collections.emptyList();
      } else {
        final List<String> result = new ArrayList<>();
        for (String splitted : text.split(",")) {
          result.add(splitted.strip());
        }
        return result;
      }
    }

    // 변환할 수 없는 값의 경우
    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * JsonNode에서 Integer 값을 추출합니다.
   * 필드가 존재하지만 숫자로 변환 불가능한 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return Integer 값 (없거나 파싱 실패시 null 반환)
   */
  protected Integer getInt(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    return getInt(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 Integer 값을 추출합니다.
   * 필드가 존재하지만 숫자로 변환 불가능한 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return Integer 값 (없거나 파싱 실패시 null 반환)
   */
  protected Integer getInt(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch, final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isInt()) {
      return fieldNode.asInt();
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      if (text.isEmpty()) {
        return null;
      } else {
        try {
          return Integer.parseInt(text);
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
    }

    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * JsonNode에서 ","로 연결된 문자열을 정수 리스트로 추출합니다. 빈 문자열의 경우 empty list를 반환합니다.
   * 그 외의 경우 onTypeMismatch 콜백을 호출하고 null을 반환합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 예상된 값이 아닐 경우 호출될 콜백 (fieldName, actualValue)
   * @return Integer List(onTypeMismatch 발생 시 null)
   */
  @Nullable
  protected List<Integer> getCommaJoinedInt(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch) {
    return getCommaJoinedInt(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 ","로 연결된 문자열을 정수 리스트로 추출합니다. 빈 문자열의 경우 empty list를 반환합니다.
   * 그 외의 경우 onTypeMismatch 콜백을 호출하고 null을 반환합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 예상된 값이 아닐 경우 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return Integer List(onTypeMismatch 발생 시 null)
   */
  @Nullable
  protected List<Integer> getCommaJoinedInt(final JsonNode node, final String fieldName,
      final BiConsumer<String, JsonNode> onTypeMismatch, final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return Collections.emptyList();
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      if (text.isEmpty()) {
        return Collections.emptyList();
      } else {
        final List<Integer> result = new ArrayList<>();
        try {
          for (final String splitted : text.split(",")) {
            result.add(Integer.parseInt(splitted.strip()));
          }
          return result;
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
    }

    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * JsonNode에서 Long 값을 추출합니다.
   * 필드가 존재하지만 숫자로 변환 불가능한 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return Long 값 (없거나 파싱 실패시 null 반환)
   */
  protected Long getLong(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
    return getLong(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 Long 값을 추출합니다.
   * 필드가 존재하지만 숫자로 변환 불가능한 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return Long 값 (없거나 파싱 실패시 null 반환)
   */
  protected Long getLong(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch,
      final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isLong()) {
      return fieldNode.asLong();
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      if (text.isEmpty()) {
        return null;
      } else {
        try {
          return Long.parseLong(text);
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
    }

    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * JsonNode에서 Boolean 값을 추출합니다.
   * "Y", "N" 문자열을 boolean으로 변환합니다.
   * 필드가 존재하지만 "Y", "N"이 아닌 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return Boolean 값 ("Y"이면 true, "N"이면 false, 그 외에는 null)
   */
  protected Boolean getBoolean(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
    return getBoolean(node, fieldName, onTypeMismatch, false);
  }

  /**
   * JsonNode에서 Boolean 값을 추출합니다.
   * "Y", "N" 문자열을 boolean으로 변환합니다.
   * 필드가 존재하지만 "Y", "N"이 아닌 경우 onTypeMismatch 콜백을 호출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return Boolean 값 ("Y"이면 true, "N"이면 false, 그 외에는 null)
   */
  protected Boolean getBoolean(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch,
      final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isBoolean()) {
      return fieldNode.asBoolean();
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip().toUpperCase();
      if (text.isEmpty()) {
        return null;
      } else if ("Y".equals(text)) {
        return true;
      } else if ("N".equals(text)) {
        return false;
      }
    }

    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * 빈 문자열, 단일 객체, 객체의 배열을 ArrayNode로 정규화합니다.
   *
   * @param node      정규화할 JSON 노드
   * @param fieldName 빈 문자열이 아닐 경우 객체나 배열이 들어있는 필드 이름
   * @return ArrayNode
   * @throws TypeMismatchException 유효하지 않은 필드인 경우
   */
  public static ArrayNode arrayNormalizer(final JsonNode node, final String fieldName) throws TypeMismatchException {
    if (node == null || node.isMissingNode()) {
      throw new TypeMismatchException("not exist");
    }
    // 빈 문자열("")일 경우 빈 배열로 취급
    if (node.isTextual() && node.asText().strip().isEmpty()) {
      return JsonNodeFactory.instance.arrayNode();
    }
    // 객체인데 fieldName을 포함하지 않을 경우 빈 배열로 취급
    else if (node.isObject()) {
      if (!node.has(fieldName)) {
        return JsonNodeFactory.instance.arrayNode();
      }
      // 객체인데 fieldName을 포함하고 있다면 객체일 때와 배열일 때를 구분
      else {
        final JsonNode complexNode = node.get(fieldName);
        // 배열일 경우에는 모든 원소가 객체일 경우에만 배열로 반환(객체의 배열만 유효)
        if (complexNode.isArray()) {
          for (final JsonNode item : ((ArrayNode) complexNode)) {
            if (!item.isObject()) {
              throw new TypeMismatchException("invalid type");
            }
          }
          return (ArrayNode) complexNode;
        }
        // 단일 객체의 경우 배열로 감싸서 반환
        else if (complexNode.isObject()) {
          final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
          arrayNode.add(complexNode);
          return arrayNode;
        }
      }
    }

    // 기타 형태의 경우 예외 처리
    // 1. null, 원시값
    // 2. 비어있지 않은 문자열
    // 3. fieldName의 필드를 가지나 그 값이 null, 원시값, 문자열
    // 4. fieldName의 필드가 배열이지만 객체의 배열이 아님
    throw new TypeMismatchException("invalid type");
  }

  /**
   * 빈 문자열, 단일 객체, 객체의 배열을 ArrayNode로 정규화합니다.
   * ({@link #arrayNormalizer(JsonNode, String)}와 동일)
   *
   * @param node      정규화할 JSON 노드
   * @param fieldName 빈 문자열이 아닐 경우 객체나 배열이 들어있는 필드 이름
   * @return ArrayNode
   * @throws TypeMismatchException 유효하지 않은 필드인 경우
   */
  protected ArrayNode normalizeToArray(final JsonNode node, final String fieldName) throws TypeMismatchException {
    return BaseParser.arrayNormalizer(node, fieldName);
  }

  /**
   * 부모 노드에서 특정 필드를 문자열 배열로 평탄화하여 추출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @return 평탄화된 문자열 (없거나 빈 경우 null 반환)
   */
  protected String flattenStringArray(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
    return flattenStringArray(node, fieldName, onTypeMismatch, false);
  }

  /**
   * 부모 노드에서 특정 필드를 문자열 배열로 평탄화하여 추출합니다.
   *
   * @param node           JSON 노드
   * @param fieldName      필드명
   * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
   * @param optional       true이면 필드가 없을 때 로깅 없이 null 반환
   * @return 평탄화된 문자열 (없거나 빈 경우 null 반환)
   */
  protected String flattenStringArray(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch,
      final boolean optional) {
    final JsonNode fieldNode;
    try {
      fieldNode = validateAndGetFieldNode(node, fieldName);
    } catch (RuntimeException e) {
      return null;
    }
    if (fieldNode.isMissingNode() && optional) {
      return null;
    } else if (fieldNode.isTextual()) {
      final String text = fieldNode.asText().strip();
      return text.isEmpty() ? null : text;
    } else if (fieldNode.isArray()) {
      final List<String> paragraphs = new ArrayList<>();
      for (final JsonNode paragraphNode : fieldNode) {
        if (paragraphNode.isArray()) {
          final List<String> lines = new ArrayList<>();
          for (final JsonNode lineNode : paragraphNode) {
            if (lineNode.isTextual()) {
              lines.add(lineNode.asText().strip());
            } else {
              if (onTypeMismatch != null) {
                onTypeMismatch.accept(fieldName, fieldNode);
              }
              return null;
            }
          }
          if (!lines.isEmpty()) {
            paragraphs.add(String.join("\n", lines));
          }
        } else if (paragraphNode.isTextual()) {
          paragraphs.add(paragraphNode.asText().strip());
        } else {
          if (onTypeMismatch != null) {
            onTypeMismatch.accept(fieldName, fieldNode);
          }
          return null;
        }
      }
      return paragraphs.isEmpty() ? null : String.join("\n\n", paragraphs);
    }

    if (onTypeMismatch != null) {
      onTypeMismatch.accept(fieldName, fieldNode);
    }
    return null;
  }

  /**
   * 타입 불일치 핸들러를 생성합니다.
   * 필드 타입이 예상과 다를 때 targetMap에 해당 필드와 값을 기록합니다.
   *
   * @param targetMap 기록할 Map (fieldName → jsonValue)
   * @return BiConsumer 핸들러
   */
  protected BiConsumer<String, JsonNode> createTypeMismatchRecorder(Map<String, String> targetMap) {
    return (fieldName, actualValue) -> {
      try {
        targetMap.put(fieldName, objectMapper.writeValueAsString(actualValue));
      } catch (JsonProcessingException e) {
        targetMap.put(fieldName, "[serialize error]");
      }
    };
  }

  /**
   * 예상치 못한 필드를 추적합니다.
   * knownFields에 포함되지 않은 필드를 발견하면 targetMap에 기록합니다.
   *
   * @param node        JSON 노드
   * @param knownFields 알려진 필드명 목록
   * @param targetMap   기록할 Map (fieldName → jsonValue)
   */
  protected void trackUnexpectedFields(
      JsonNode node,
      Set<String> knownFields,
      Map<String, String> targetMap) {
    trackUnexpectedFields(node, knownFields, createTypeMismatchRecorder(targetMap));
  }

  /**
   * 예상치 못한 필드를 추적합니다.
   * knownFields에 포함되지 않은 필드를 발견하면 recorder 콜백을 호출합니다.
   * 중첩 객체 파싱 시 필드명에 prefix를 추가하는 패턴에 사용됩니다.
   *
   * @param node        JSON 노드
   * @param knownFields 알려진 필드명 목록
   * @param recorder    예상치 못한 필드 발견 시 호출될 콜백 (fieldName, jsonNode)
   */
  protected void trackUnexpectedFields(
      JsonNode node,
      Set<String> knownFields,
      BiConsumer<String, JsonNode> recorder) {
    if (node == null || !node.isObject()) {
      return;
    }
    node.fields().forEachRemaining(entry -> {
      String fieldName = entry.getKey();
      if (!knownFields.contains(fieldName)) {
        recorder.accept(fieldName, entry.getValue());
      }
    });
  }

  private JsonNode validateAndGetFieldNode(final JsonNode node, final String fieldName) {
    if (node == null) {
      log.debug("node is null for field: {}", fieldName);
      throw new NullPointerException("node cannot be null");
    }
    if (fieldName == null || fieldName.isBlank()) {
      log.debug("fieldName is blank or null");
      throw new IllegalArgumentException("fieldName cannot be blank");
    }
    return node.path(fieldName.strip());
  }
}
