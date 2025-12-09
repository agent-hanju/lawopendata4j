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

/** 부서 DTO (연락부서) */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Department extends BaseDto {
  private Integer key; // 부서키
  private String name; // 부서명
  private String address; // 부서연락처

  private Org org;

  // private Integer orgCode; // 소관부처코드
  // private String orgName; // 소관부처명
}
