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

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StatuteHistoryDto extends BaseDto {
  private List<ArticleListDto> jo; // 조문정보
  private Integer lsId; // 법령ID
  private Integer mst; // 법령일련번호
  private Integer efYd; // 시행일자
  private String lsNm; // 법령명한글
  private String kndNm; // 법령구분명
  private Integer ancNo; // 공포번호
  private String rrClsNm; // 제개정구분명
  private Integer ancYd; // 공포일자
  private List<Org> orgs;
  // private List<String> orgNms; // 소관부처명
  // private List<Integer> orgCds;// 소관부처코드
}
