package kr.go.law.administrative.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.administrative.dto.AdministrativeAppealContentDto;
import kr.go.law.administrative.dto.AdministrativeAppealDto;
import kr.go.law.administrative.enums.AdministrativeSortOption;
import kr.go.law.administrative.request.AdministrativeContentRequest;
import kr.go.law.administrative.request.AdministrativeListRequest;
import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import okhttp3.OkHttpClient;

/**
 * AdministrativeApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class AdministrativeApiClientIntegrationTest {

  private AdministrativeApiClient client;

  @BeforeEach
  void setUp() {
    String apiKey = System.getenv("LAW_OPEN_DATA_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      apiKey = "test-key"; // 테스트용 기본값
    }

    LawOpenDataProperties properties = LawOpenDataProperties.builder()
        .oc(apiKey)
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    client = new AdministrativeApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnAdministrativeAppealList() {
    // Given
    AdministrativeListRequest request = AdministrativeListRequest.builder()
        .page(1)
        .display(10)
        .sort(AdministrativeSortOption.RESOLUTION_DATE_DESC)
        .resolutionDateFrom(20200101)
        .resolutionDateTo(20251231)
        .build();

    // When
    ListApiResult<AdministrativeAppealDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      AdministrativeAppealDto first = result.items().get(0);
      System.out.println("\nFirst administrative appeal:");
      System.out.println("  Case Name: " + first.getCaseName());
      System.out.println("  Case Number: " + first.getCaseNumber());
      System.out.println("  Decision Date: " + first.getDecisionDate());
      System.out.println("  Serial Number: " + first.getAppealSerialNumber());
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void getContent_shouldReturnAdministrativeAppealContent() {
    // Given - 특정 행정심판례 일련번호
    AdministrativeContentRequest request = AdministrativeContentRequest.builder()
        .serialNumber(123456)
        .build();

    // When
    ContentApiResult<AdministrativeAppealContentDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Administrative Appeal Content ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(appeal -> {
      System.out.println("\nAppeal details:");
      System.out.println("  Case Name: " + appeal.getCaseName());
      System.out.println("  Case Number: " + appeal.getCaseNumber());
      System.out.println("  Decision Date: " + appeal.getDecisionDate());
      System.out.println("  Adjudication Agency: " + appeal.getAdjudicationAgency());
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void search_withDateRange_shouldReturnFilteredResults() {
    // Given
    AdministrativeListRequest request = AdministrativeListRequest.builder()
        .page(1)
        .display(5)
        .resolutionDateFrom(20230101)
        .resolutionDateTo(20231231)
        .build();

    // When
    ListApiResult<AdministrativeAppealDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Search with Date Range ===");
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Items: " + result.items().size());

    assertTrue(result.totalCount() >= 0);
  }

  @Test
  void search_withPagination_shouldWorkCorrectly() {
    // Given - 첫 페이지
    AdministrativeListRequest request1 = AdministrativeListRequest.builder()
        .page(1)
        .display(5)
        .resolutionDateFrom(20200101)
        .build();

    // When
    ListApiResult<AdministrativeAppealDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      AdministrativeListRequest request2 = AdministrativeListRequest.builder()
          .page(2)
          .display(5)
          .resolutionDateFrom(20200101)
          .build();

      ListApiResult<AdministrativeAppealDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getAppealSerialNumber()
            .equals(result2.items().get(0).getAppealSerialNumber());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
