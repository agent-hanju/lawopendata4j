package kr.go.law.statute.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 조문 DTO - ArticleEntity와 1:1 대응 */
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleDto {
  private Integer mst;
  private Integer joUnique; // API에서의 조문키
  private String joType;
  private Integer joKey; // 조문 지정자(조문 검색에 쓰이는 JO 필드를 joNum, joBrNum으로부터 복원하여 작성)
  private Integer lsId;
  private Integer ancYd;
  private Integer ancNo;
  private String lsNm;
  private Integer joNum;
  private Integer joBrNum;
  private String joTitle;
  private String content;
  private List<Hang> hangList;
  private String amendedType;
  private String amendedDateStr;
  private String isAmended;
  private Boolean isChanged;
  private Integer efYd;
  private Integer prevJoKey;
  private Integer nextJoKey;
  private String joRef;
  private String titleChanged;
  private String isHangul;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, String> unexpectedFieldMap;
}
