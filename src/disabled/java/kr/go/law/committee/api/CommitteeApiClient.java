package kr.go.law.committee.api;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.client.BaseApiClient;
import kr.go.law.committee.dto.CommitteeDecisionContentDto;
import kr.go.law.committee.dto.CommitteeDecisionDto;
import kr.go.law.config.LawOpenDataProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * 위원회 결정문 Open API 클라이언트
 * <p>
 * 개인정보보호위원회, 공정거래위원회, 노동위원회, 국가인권위원회 등의 결정문 조회 API를 호출합니다.
 * </p>
 */
@Slf4j
public class CommitteeApiClient extends BaseApiClient {

  /**
   * 위원회 결정문 목록 조회 결과
   * @deprecated Use {@link kr.go.law.common.response.ListApiResult} instead
   */
  @Deprecated
  public record ListApiResult(
      String rawData,
      List<CommitteeDecisionDto> decisions,
      int totalCount) {
  }

  /**
   * 위원회 결정문 본문 조회 결과
   * @deprecated Use {@link kr.go.law.common.response.ContentApiResult} instead
   */
  @Deprecated
  public record ContentApiResult(
      String rawData,
      Optional<CommitteeDecisionContentDto> content) {
  }

  public CommitteeApiClient(LawOpenDataProperties properties, ObjectMapper objectMapper, OkHttpClient client) {
    super(properties, objectMapper, client);
  }

  // TODO: Implement search() and getContent() methods using executeListApi() and executeContentApi()
}
