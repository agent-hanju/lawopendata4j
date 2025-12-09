package kr.go.law.common.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** DTO 공통 추상 클래스 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseDto {
  /** 파싱 시 예상치 못한 필드들을 저장 */
  private Map<String, String> unexpected;
}
