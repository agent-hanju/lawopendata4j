package kr.go.law.precedent.dto;

import kr.go.law.common.dto.BaseDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 판례 본문 DTO - Content API 응답용
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrecedentContentDto extends BaseDto {
  private String summary; // 판시사항
  private String precedentReferences; // 참조판례
  private String caseTypeName; // 사건종류명
  private String decisionSummary; // 판결요지
  private String articleReferences; // 참조조문
  private Integer decisionDate; // 선고일자
  private String courtName; // 법원명
  private String caseName; // 사건명
  private String content; // 판례내용
  private String caseNumber; // 사건번호
  private String caseTypeCode; // 사건종류코드
  private Integer precId; // 판례정보일련번호
  private String decision; // 선고
  private String decisionType; // 판결유형
  private String courtCode; // 법원종류코드
}
