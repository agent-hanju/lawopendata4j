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
 * 판례 목록 DTO - List API 응답용
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrecedentListDto extends BaseDto {
  private String caseNumber; // 사건번호
  private String dataSource; // 데이터출처명
  private String caseTypeCode; // 사건종류코드
  private String caseTypeName; // 사건종류명
  private String decision; // 선고
  private Integer decisionDate; // 선고일자
  private Integer precId; // 판례일련번호
  private String decisionType; // 판결유형
  private String courtCode; // 법원종류코드
  private String courtName; // 법원명
  private String caseName; // 사건명
}
