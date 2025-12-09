package kr.go.law.statute.dto;

import kr.go.law.common.dto.BaseDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** 부칙 단위 DTO */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Addendum extends BaseDto {
  private Long key; // 부칙키
  private Integer ancYd; // 부칙공포일자
  private String content; // 부칙내용
  private Integer ancNo; // 부칙공포번호
}
