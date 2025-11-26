package kr.go.law.statute.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 법령 DTO - StatuteEntity와 1:1 대응 */
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatuteDto {
  private Integer mst;
  private Integer efYd;
  private Integer lsId;
  private String statusCode;
  private String lawType;
  private String lsNm;
  private String lsNmHanja;
  private String lsNmAbbr;
  private Integer ancYd;
  private Integer ancNo;
  private String enactmentType;
  /** 소관부처코드 (콤마 구분 → List) */
  private List<String> orgCd;
  /** 소관부처명 (콤마 구분 → List) */
  private List<String> org;
  private String knd;
  private String kndCd;
  private String isAnc;
  private Integer pyeonjangjeolgwan;
  private String decisionBody;
  private String proposerType;
  private String phoneNumber;
  private String language;
  private String appendixEditYn;
  private List<Department> contactInfo;
  private List<CoEnactment> coEnactments;
  private String amendment;
  private String amendmentReason;
  private List<Addendum> addenda;
  private List<Appendix> appendices;
  private Map<Integer, ArticleDto> articles;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, String> unexpectedFieldMap;
}
