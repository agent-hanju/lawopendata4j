package kr.go.law.statute.dto;

import kr.go.law.common.dto.BaseDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 조문 개정 이력 DTO (History API 응답용)
 */
@SuperBuilder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ArticleListDto extends BaseDto {
  private Integer jo; // 조문번호
  private String rrCls; // 변경사유
  private Integer efYd; // 조문시행일
  private Integer rrClsYd; // 조문개정일
}
