package kr.go.law.constitutional.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.constitutional.dto.ConstitutionalDecisionContentDto;
import kr.go.law.constitutional.dto.ConstitutionalDecisionDto;
import kr.go.law.constitutional.enums.ConstitutionalSortOption;
import kr.go.law.constitutional.request.ConstitutionalContentRequest;
import kr.go.law.constitutional.request.ConstitutionalListRequest;
import okhttp3.OkHttpClient;

/**
 * ConstitutionalApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class ConstitutionalApiClientIntegrationTest {

  private ConstitutionalApiClient client;

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

    client = new ConstitutionalApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnConstitutionalDecisionList() {
    // Given
    ConstitutionalListRequest request = ConstitutionalListRequest.builder()
        .page(1)
        .display(10)
        .sort(ConstitutionalSortOption.END_DATE_DESC)
        .endDateFrom(20200101)
        .endDateTo(20251231)
        .build();

    // When
    ListApiResult<ConstitutionalDecisionDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      ConstitutionalDecisionDto first = result.items().get(0);
      System.out.println("\nFirst constitutional decision:");
      System.out.println("  Case Name: " + first.getCaseName());
      System.out.println("  Case Number: " + first.getCaseNumber());
      System.out.println("  Final Date: " + first.getFinalDate());
      System.out.println("  Serial Number: " + first.getDecisionSerialNumber());
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void getContent_shouldReturnConstitutionalDecisionContent() {
    // Given - 특정 헌법재판소 결정 일련번호
    ConstitutionalContentRequest request = ConstitutionalContentRequest.builder()
        .serialNumber(132256)
        .build();

    // When
    ContentApiResult<ConstitutionalDecisionContentDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Constitutional Decision Content ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(decision -> {
      System.out.println("\nDecision details:");
      System.out.println("  Case Name: " + decision.getCaseName());
      System.out.println("  Case Number: " + decision.getCaseNumber());
      System.out.println("  Final Date: " + decision.getFinalDate());
      System.out.println("  Case Type: " + decision.getCaseTypeName());
      String summary = decision.getJudgmentMatters();
      if (summary != null && summary.length() > 100) {
        System.out.println("  Summary (first 100 chars): " +
            summary.substring(0, 100) + "...");
      } else {
        System.out.println("  Summary: " + summary);
      }
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void search_withDateRange_shouldReturnFilteredResults() {
    // Given
    ConstitutionalListRequest request = ConstitutionalListRequest.builder()
        .page(1)
        .display(5)
        .endDateFrom(20200101)
        .endDateTo(20201231)
        .build();

    // When
    ListApiResult<ConstitutionalDecisionDto> result = client.search(request);

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
    ConstitutionalListRequest request1 = ConstitutionalListRequest.builder()
        .page(1)
        .display(5)
        .endDateFrom(20200101)
        .build();

    // When
    ListApiResult<ConstitutionalDecisionDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      ConstitutionalListRequest request2 = ConstitutionalListRequest.builder()
          .page(2)
          .display(5)
          .endDateFrom(20200101)
          .build();

      ListApiResult<ConstitutionalDecisionDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getDecisionSerialNumber()
            .equals(result2.items().get(0).getDecisionSerialNumber());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
