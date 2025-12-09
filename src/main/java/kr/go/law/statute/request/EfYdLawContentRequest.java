package kr.go.law.statute.request;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.go.law.common.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 시행일자 기준 법령 본문 조회 요청 (target: eflaw)
 * <p>
 * API Endpoint: {@code http://www.law.go.kr/DRF/lawService.do}
 * </p>
 * <p>
 * 공포일자 기준 조회({@link StatuteContentRequest})와 달리 시행일자(efYd)를 기준으로 법령 본문을 조회합니다.
 * 일부 필드(제명변경여부, 한글법령여부)가 제공되지 않을 수 있습니다.
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * // MST + 시행일자로 조회
 * EfYdLawContentRequest request = EfYdLawContentRequest.builder()
 *     .mst(253527)
 *     .efYd(20240101)
 *     .build();
 *
 * // ID로 조회 (현행 법령 본문 조회, efYd 무시)
 * EfYdLawContentRequest request = EfYdLawContentRequest.builder()
 *     .id(1233)
 *     .build();
 *
 * // 특정 조문만 조회
 * EfYdLawContentRequest request = EfYdLawContentRequest.builder()
 *     .mst(253527)
 *     .efYd(20240101)
 *     .jo(400) // 제4조만 조회
 *     .build();
 * }
 * </pre>
 */
@Builder
@Getter
public class EfYdLawContentRequest implements BaseRequest {

  private static final String TARGET = "eflaw";

  /**
   * 법령ID (6자리, 예: 1233 → "001233")
   * <p>
   * API 파라미터: ID
   * </p>
   * <p>
   * ID 또는 MST 중 하나는 반드시 입력.
   * ID로 검색하면 해당 법령의 현행 법령 본문을 조회하며, efYd 값은 무시됩니다.
   * </p>
   */
  private Integer id;

  /**
   * 법령일련번호 (법령테이블의 lsi_seq 값)
   * <p>
   * API 파라미터: MST
   * </p>
   * <p>
   * ID 또는 MST 중 하나는 반드시 입력
   * </p>
   */
  private Integer mst;

  /**
   * 시행일자 (YYYYMMDD 형태의 정수)
   * <p>
   * API 파라미터: efYd
   * </p>
   * <p>
   * ID 입력시에는 무시되는 값으로 입력하지 않음.
   * MST로 조회할 때 필수입니다.
   * </p>
   */
  private Integer efYd;

  /**
   * 조번호 (6자리숫자 : 조번호(4자리)+조가지번호(2자리))
   * <p>
   * API 파라미터: JO
   * </p>
   * <p>
   * 생략(기본값): 모든 조를 표시
   * </p>
   * <p>
   * 예: 200 → "000200" (제2조), 1002 → "001002" (제10조의2)
   * </p>
   */
  private Integer jo;

  /**
   * 원문/한글 여부
   * <p>
   * API 파라미터: chrClsCd
   * </p>
   * <p>
   * 생략(기본값): 한글
   * </p>
   * <p>
   * 010202: 한글, 010201: 원문
   * </p>
   */
  private String chrClsCd;

  @Override
  public String getTarget() {
    return TARGET;
  }

  @Override
  public Map<String, String> toQueryParameters() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("target", TARGET);
    params.put("type", "JSON");

    // 법령ID (6자리 zero-padding)
    if (id != null) {
      params.put("ID", String.format("%06d", id));
    }

    // 법령일련번호
    if (mst != null) {
      params.put("MST", String.valueOf(mst));
    }

    // 시행일자
    if (efYd != null) {
      params.put("efYd", String.valueOf(efYd));
    }

    // 조번호 (6자리 zero-padding)
    if (jo != null) {
      params.put("JO", String.format("%06d", jo));
    }

    // 원문/한글 여부
    if (chrClsCd != null && !chrClsCd.isBlank()) {
      params.put("chrClsCd", chrClsCd);
    }

    return params;
  }

  /**
   * 원문/한글 구분 코드 상수
   */
  public static class ChrClsCd {
    /** 한글 */
    public static final String HANGUL = "010202";
    /** 원문 */
    public static final String ORIGINAL = "010201";

    private ChrClsCd() {}
  }
}
