package kr.go.law.statute.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.parser.BaseParser;
import kr.go.law.statute.dto.Department;
import kr.go.law.statute.dto.Org;

/** 부서 파서 (연락부서) */
public class DepartmentParser extends BaseParser<Department> {
  private static final Set<String> KNOWN_FIELDS = Set.of(
      "부서연락처",
      "부서키",
      "부서명",
      "소관부처명",
      "소관부처코드");

  public DepartmentParser(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public Department parse(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }

    final Map<String, String> unexpected = new HashMap<>();
    trackUnexpectedFields(node, KNOWN_FIELDS, unexpected);
    final BiConsumer<String, JsonNode> onTypeMismatch = createTypeMismatchRecorder(unexpected);

    return Department.builder()
        .address(getString(node, "부서연락처", onTypeMismatch))
        .key(getInt(node, "부서키", onTypeMismatch))
        .name(getString(node, "부서명", onTypeMismatch))
        .org(parseOrg(node, onTypeMismatch))
        .unexpected(unexpected)
        .build();
  }

  private Org parseOrg(final JsonNode node, final BiConsumer<String, JsonNode> onTypeMismatch) {
    return Org.builder()
        .name(getString(node, "소관부처명", onTypeMismatch))
        .code(getString(node, "소관부처코드", onTypeMismatch))
        .build();
  }
}
