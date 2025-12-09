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

/** 법령 목록 DTO (목록 API 응답용) */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StatuteListDto extends BaseDto {
  private String nw; // 현행연혁코드
  private Integer mst; // 법령일련번호
  // private String lawType; // 자법타법여부, 무조건 ""로 추정되어 임시로 제외하였다.
  private String lsNm; // 법령명한글
  private String kndNm; // 법령구분명
  // private List<String> orgNms; // 소관부처명
  private Integer ancNo; // 공포번호
  private String rrClsNm; // 제개정구분명
  // private List<Integer> orgCds;// 소관부처코드
  private Integer lsId; // 법령ID
  private List<CoOrdinanceInfo> coOrdinanceInfos; // 공동부령정보
  private Integer efYd; // 시행일자
  private Integer ancYd; // 공포일자
  private String lsNmAbbr; // 법령약칭명
  private List<Org> orgs;
}
