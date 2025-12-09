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

/** 별표 단위 DTO */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Appendix extends BaseDto {
  private String title; // 별표제목
  private String pdfFilename; // 별표PDF파일명
  private String hwpFilename; // 별표HWP파일명
  private Integer no; // 별표번호
  private String pdfFileLink; // 별표서식PDF파일링크
  private String key; // 별표키
  private String content; // 별표내용
  private List<String> imgFilenames; // 별표이미지파일명
  private String type; // 별표구분
  private String fileLink; // 별표서식파일링크
  private Integer brNo; // 별표가지번호
}
