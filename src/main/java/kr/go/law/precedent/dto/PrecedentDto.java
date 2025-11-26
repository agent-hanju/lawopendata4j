package kr.go.law.precedent.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 판례 DTO - PrecedentEntity와 1:1 대응 */
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrecedentDto {
  private Integer precId;
  private String caseNumber;
  private String caseName;
  private String caseTypeName;
  private String caseTypeCode;
  private String courtName;
  private String courtCode;
  private Integer decisionDate;
  private String declaration;
  private String decisionType;
  private String decisionSummary;
  private String summary;
  private String content;
  private String dataSource;
  private List<ArticleReferenceDto> articleReferences;
  private List<PrecedentReferenceDto> precedentReferences;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, String> unexpectedFieldMap;
}
