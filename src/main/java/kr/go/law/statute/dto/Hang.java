package kr.go.law.statute.dto;

import java.util.List;

import kr.go.law.common.dto.BaseDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** 항 DTO */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Hang extends BaseDto {
  private String no; // 항번호
  private String brNo; // 항가지번호
  private String content; // 항내용
  private List<Ho> ho; // 호

  private String rrCls; // 항제개정유형
  private String rrClsYd; // 항제개정일자
  private String rrClsYdStr; // 항제개정일자문자열
}
