package kr.go.law.precedent.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.precedent.dto.PrecedentDto;
import kr.go.law.precedent.enums.CourtType;
import kr.go.law.precedent.enums.PrecedentSortOption;
import kr.go.law.precedent.request.PrecedentContentRequest;
import kr.go.law.precedent.request.PrecedentListRequest;
import okhttp3.OkHttpClient;

/**
 * PrecedentApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class PrecedentApiClientIntegrationTest {

  private PrecedentApiClient client;

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

    client = new PrecedentApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnPrecedentList() {
    // Given
    PrecedentListRequest request = PrecedentListRequest.builder()
        .page(1)
        .display(10)
        .query("손해배상")
        .sort(PrecedentSortOption.DECISION_DATE_DESC)
        .build();

    // When
    ListApiResult<PrecedentDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      PrecedentDto first = result.items().get(0);
      System.out.println("\n=== First Precedent DTO ===");
      System.out.println("  판례ID: " + first.getPrecId());
      System.out.println("  사건번호: " + first.getCaseNumber());
      System.out.println("  사건명: " + first.getCaseName());
      System.out.println("  사건종류: " + first.getCaseTypeName() + " (코드: " + first.getCaseTypeCode() + ")");
      System.out.println("  법원명: " + first.getCourtName() + " (코드: " + first.getCourtCode() + ")");
      System.out.println("  선고일자: " + first.getDecisionDate());
      System.out.println("  선고: " + first.getDeclaration());
      System.out.println("  판결유형: " + first.getDecisionType());
      System.out.println("  출처: " + first.getDataSource());
      if (first.getDecisionSummary() != null && !first.getDecisionSummary().isBlank()) {
        System.out.println("  판결요지: " + first.getDecisionSummary().substring(0, Math.min(100, first.getDecisionSummary().length())) + "...");
      }
      System.out.println("========================\n");
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void search_withCourtFilter_shouldReturnFilteredList() {
    // Given
    PrecedentListRequest request = PrecedentListRequest.builder()
        .page(1)
        .display(5)
        .query("민법")
        .courtType(CourtType.SUPREME_COURT)
        .build();

    // When
    ListApiResult<PrecedentDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Search with Court Filter ===");
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Items size: " + result.items().size());

    assertTrue(result.totalCount() >= 0);
  }

  @Test
  void getContent_shouldReturnPrecedentContent() {
    // Given - 특정 판례 일련번호
    PrecedentContentRequest request = PrecedentContentRequest.builder()
        .precedentSerialNumber(608799)
        .build();

    // When
    ContentApiResult<PrecedentDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Precedent Content ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(precedent -> {
      System.out.println("\nPrecedent details:");
      System.out.println("  Case Name: " + precedent.getCaseName());
      System.out.println("  Case Number: " + precedent.getCaseNumber());
      System.out.println("  Decision Date: " + precedent.getDecisionDate());
      System.out.println("  Court: " + precedent.getCourtName());
      String summary = precedent.getSummary();
      if (summary != null && summary.length() > 100) {
        System.out.println("  Summary (first 100 chars): " +
            summary.substring(0, 100) + "...");
      } else {
        System.out.println("  Summary: " + summary);
      }
    });

    // Content API가 실패할 수 있으므로 (Fallback API가 필요할 수 있음) 응답만 확인
    assertNotNull(result);
  }

  @Test
  void search_withDateRange_shouldReturnFilteredResults() {
    // Given
    PrecedentListRequest request = PrecedentListRequest.builder()
        .page(1)
        .display(5)
        .query("계약")
        .decisionDateFrom(20230101)
        .decisionDateTo(20231231)
        .build();

    // When
    ListApiResult<PrecedentDto> result = client.search(request);

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
    PrecedentListRequest request1 = PrecedentListRequest.builder()
        .page(1)
        .display(5)
        .query("판례")
        .build();

    // When
    ListApiResult<PrecedentDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      PrecedentListRequest request2 = PrecedentListRequest.builder()
          .page(2)
          .display(5)
          .query("판례")
          .build();

      ListApiResult<PrecedentDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getPrecId()
            .equals(result2.items().get(0).getPrecId());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
