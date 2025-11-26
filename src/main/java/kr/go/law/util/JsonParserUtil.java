package kr.go.law.util;

import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * JSON 파싱 유틸리티 클래스
 */
public final class JsonParserUtil {
    private JsonParserUtil() {
    }

    /**
     * JsonNode에서 문자열 값을 추출합니다.
     *
     * @param node      JSON 노드
     * @param fieldName 필드명
     * @return 문자열 값 (없거나 null이면 null 반환)
     */
    public static String getString(JsonNode node, String fieldName) {
        return getString(node, fieldName, null);
    }

    /**
     * JsonNode에서 문자열 값을 추출합니다.
     * 필드가 존재하지만 문자열이 아닌 경우 onTypeMismatch 콜백을 호출합니다.
     *
     * @param node           JSON 노드
     * @param fieldName      필드명
     * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
     * @return 문자열 값 (없거나 null이면 null 반환)
     */
    public static String getString(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isNull()) {
            return null;
        }
        // 문자열이 아닌 복합 타입(배열/객체)인 경우 타입 불일치로 처리
        if (onTypeMismatch != null && (fieldNode.isArray() || fieldNode.isObject())) {
            onTypeMismatch.accept(fieldName, fieldNode);
        }
        return fieldNode.asText(null);
    }

    /**
     * JsonNode에서 Integer 값을 추출합니다.
     *
     * @param node      JSON 노드
     * @param fieldName 필드명
     * @return Integer 값 (없거나 파싱 실패시 null 반환)
     */
    public static Integer getInt(JsonNode node, String fieldName) {
        return getInt(node, fieldName, null);
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
    public static Integer getInt(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asInt();
        }
        if (fieldNode.isTextual()) {
            String text = fieldNode.asText();
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                if (onTypeMismatch != null) {
                    onTypeMismatch.accept(fieldName, fieldNode);
                }
                return null;
            }
        }
        // 배열이나 객체인 경우 타입 불일치
        if (onTypeMismatch != null) {
            onTypeMismatch.accept(fieldName, fieldNode);
        }
        return null;
    }

    /**
     * JsonNode에서 Long 값을 추출합니다.
     *
     * @param node      JSON 노드
     * @param fieldName 필드명
     * @return Long 값 (없거나 파싱 실패시 null 반환)
     */
    public static Long getLong(JsonNode node, String fieldName) {
        return getLong(node, fieldName, null);
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
    public static Long getLong(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asLong();
        }
        if (fieldNode.isTextual()) {
            String text = fieldNode.asText();
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                if (onTypeMismatch != null) {
                    onTypeMismatch.accept(fieldName, fieldNode);
                }
                return null;
            }
        }
        // 배열이나 객체인 경우 타입 불일치
        if (onTypeMismatch != null) {
            onTypeMismatch.accept(fieldName, fieldNode);
        }
        return null;
    }

    /**
     * JsonNode에서 중첩된 JSON 객체를 추출합니다.
     *
     * @param node      JSON 노드
     * @param fieldName 필드명
     * @return JsonNode 객체 (없거나 객체가 아니면 null 반환)
     */
    public static JsonNode getJsonObject(JsonNode node, String fieldName) {
        return getJsonObject(node, fieldName, null);
    }

    /**
     * JsonNode에서 중첩된 JSON 객체를 추출합니다.
     * 필드가 존재하지만 객체가 아닌 경우 onTypeMismatch 콜백을 호출합니다.
     *
     * @param node           JSON 노드
     * @param fieldName      필드명
     * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
     * @return JsonNode 객체 (없거나 객체가 아니면 null 반환)
     */
    public static JsonNode getJsonObject(JsonNode node, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isNull()) {
            return null;
        }
        if (!fieldNode.isObject()) {
            // 빈 문자열("")은 무시 (API에서 없는 값을 빈 문자열로 반환하는 경우)
            if (fieldNode.isTextual() && fieldNode.asText().isEmpty()) {
                return null;
            }
            if (onTypeMismatch != null) {
                onTypeMismatch.accept(fieldName, fieldNode);
            }
            return null;
        }
        return fieldNode;
    }

    /**
     * 단일 객체 또는 배열을 ArrayNode로 정규화합니다.
     * API 응답에서 단일 항목일 때 객체로, 복수 항목일 때 배열로 오는 경우를 처리합니다.
     *
     * @param node JSON 노드 (객체 또는 배열)
     * @return ArrayNode (항상 배열 형태)
     */
    public static ArrayNode normalizeToArray(JsonNode node) {
        return normalizeToArray(node, null);
    }

    /**
     * 단일 객체 또는 배열을 ArrayNode로 정규화합니다.
     * API 응답에서 단일 항목일 때 객체로, 복수 항목일 때 배열로 오는 경우를 처리합니다.
     * 예상치 못한 타입(예: 문자열)인 경우 onTypeMismatch 콜백을 호출합니다.
     *
     * @param node           JSON 노드 (객체 또는 배열)
     * @param onTypeMismatch 타입 불일치시 호출될 콜백 (null, actualValue)
     * @return ArrayNode (항상 배열 형태)
     */
    public static ArrayNode normalizeToArray(JsonNode node, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return JsonNodeFactory.instance.arrayNode();
        }
        // 빈 문자열("")은 빈 배열로 처리 (API에서 없는 값을 빈 문자열로 반환하는 경우)
        if (node.isTextual() && node.asText().isEmpty()) {
            return JsonNodeFactory.instance.arrayNode();
        }
        if (node.isArray()) {
            return (ArrayNode) node;
        }
        if (node.isObject()) {
            // 단일 객체를 배열로 감싸기
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
            arrayNode.add(node);
            return arrayNode;
        }
        // 문자열이나 숫자 등 예상치 못한 타입
        if (onTypeMismatch != null) {
            onTypeMismatch.accept(null, node);
        }
        return JsonNodeFactory.instance.arrayNode();
    }

    /**
     * 문자열 배열(1차원 또는 2차원)을 단일 문자열로 평탄화합니다.
     * - 단일 문자열: 그대로 반환
     * - 1차원 배열: 줄바꿈으로 연결
     * - 2차원 배열: 내부 배열은 줄바꿈, 외부 배열은 빈 줄로 연결
     *
     * @param node JSON 노드 (문자열, 문자열 배열, 또는 문자열 배열의 배열)
     * @return 평탄화된 문자열 (null이거나 빈 경우 null 반환)
     */
    public static String flattenStringArray(JsonNode node) {
        return flattenStringArray(node, null);
    }

    /**
     * 문자열 배열(1차원 또는 2차원)을 단일 문자열로 평탄화합니다.
     *
     * @param node           JSON 노드
     * @param onTypeMismatch 타입 불일치시 호출될 콜백 (null, actualValue)
     * @return 평탄화된 문자열 (null이거나 빈 경우 null 반환)
     */
    public static String flattenStringArray(JsonNode node, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        // 이미 단순 문자열인 경우
        if (node.isTextual()) {
            String text = node.asText();
            return text.isEmpty() ? null : text;
        }

        // 배열이 아닌 경우 타입 불일치
        if (!node.isArray()) {
            if (onTypeMismatch != null) {
                onTypeMismatch.accept(null, node);
            }
            return null;
        }

        java.util.List<String> paragraphs = new java.util.ArrayList<>();

        for (JsonNode paragraphNode : node) {
            if (paragraphNode.isArray()) {
                // 2차원 배열: 내부 배열의 문자열들을 줄바꿈으로 연결
                java.util.List<String> lines = new java.util.ArrayList<>();
                for (JsonNode lineNode : paragraphNode) {
                    if (lineNode.isTextual()) {
                        lines.add(lineNode.asText());
                    }
                }
                if (!lines.isEmpty()) {
                    paragraphs.add(String.join("\n", lines));
                }
            } else if (paragraphNode.isTextual()) {
                // 1차원 배열
                paragraphs.add(paragraphNode.asText());
            }
        }

        return paragraphs.isEmpty() ? null : String.join("\n\n", paragraphs);
    }

    /**
     * 부모 노드에서 특정 필드를 문자열 배열로 평탄화하여 추출합니다.
     *
     * @param parent         부모 JSON 노드
     * @param fieldName      필드명
     * @param onTypeMismatch 타입 불일치시 호출될 콜백 (fieldName, actualValue)
     * @return 평탄화된 문자열 (없거나 빈 경우 null 반환)
     */
    public static String flattenStringArray(JsonNode parent, String fieldName, BiConsumer<String, JsonNode> onTypeMismatch) {
        if (parent == null || !parent.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = parent.get(fieldName);
        BiConsumer<String, JsonNode> wrappedCallback = onTypeMismatch == null ? null
                : (ignored, actualValue) -> onTypeMismatch.accept(fieldName, actualValue);
        return flattenStringArray(fieldNode, wrappedCallback);
    }
}
