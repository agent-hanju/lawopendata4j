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

/** 조문 본문 DTO (Content API 응답용) */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ArticleContentDto extends BaseDto {
  private Integer no; // 조문번호
  private Integer efYd; // 조문시행일자
  private Boolean isAmended; // 조문변경여부
  private Integer prevJo; // 조문이동이전
  private String reference; // 조문참고자료
  private Integer key; // 조문키
  private List<Hang> hang; // 항
  private String content; // 조문내용
  private String title; // 조문제목
  private Integer nextJo; // 조문이동이후
  private String type; // 조문여부(조문, 전문)
  private Integer brNo; // 조문가지번호
}
