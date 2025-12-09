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
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StatuteBasicInfo extends BaseDto {
  private String lsNm;
  private String appendixEfYdStr;
  private String decisionBody;
  private String proposalType;
  private Integer ancNo;
  private String phoneNumber;
  private String lang;
  private String rrClsNm;
  private Integer lsId;
  private List<CoOrdinanceInfo> coOrdinanceInfos;
  private Org org;
  private Boolean isAnc;
  private Knd knd;
  private Boolean isTitleChanged;
  private Integer efYd;
  private Boolean appendixEditYn;
  private List<Department> contactInfo;
  private String articleEfYdStr;
  private String lsNmHanja;
  private String lsNmAbbr;
  private Integer ancYd;
  private Boolean isHangul;
  private Integer chapter;
}
