package kr.go.law.statute.dto;

import java.util.List;

import kr.go.law.common.dto.BaseDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** 법령 본문 DTO (본문 API 응답용) */
@SuperBuilder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StatuteContentDto extends BaseDto {
  private String amendment; // 개정문내용
  private List<Appendix> appendices; // 별표
  private StatuteBasicInfo basicInfo; // 기본정보
  private List<Addendum> addenda; // 부칙
  private List<ArticleContentDto> articles; // 조문
  private String rrClsReason; // 제개정이유
}
