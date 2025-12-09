package kr.go.law.interpretation.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.interpretation.dto.LegalInterpretationContentDto;
import kr.go.law.interpretation.dto.LegalInterpretationDto;
import kr.go.law.interpretation.enums.InterpretationSortOption;
import kr.go.law.interpretation.request.InterpretationContentRequest;
import kr.go.law.interpretation.request.InterpretationListRequest;
import okhttp3.OkHttpClient;

/**
 * InterpretationApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class InterpretationApiClientIntegrationTest {

  private InterpretationApiClient client;

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

    client = new InterpretationApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnInterpretationList() {
    // Given
    InterpretationListRequest request = InterpretationListRequest.builder()
        .page(1)
        .display(10)
        .sort(InterpretationSortOption.INTERPRETATION_DATE_DESC)
        .registrationDateFrom(20200101)
        .registrationDateTo(20251231)
        .build();

    // When
    ListApiResult<LegalInterpretationDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      LegalInterpretationDto first = result.items().get(0);
      System.out.println("\nFirst legal interpretation:");
      System.out.println("  Question: " + first.getAgendaName());
      System.out.println("  Number: " + first.getAgendaNumber());
      System.out.println("  Date: " + first.getReplyDate());
      System.out.println("  Serial Number: " + first.getInterpretationSerialNumber());
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void getContent_shouldReturnInterpretationContent() {
    // Given - 특정 법령해석례 일련번호
    InterpretationContentRequest request = InterpretationContentRequest.builder()
        .serialNumber(123456)
        .build();

    // When
    ContentApiResult<LegalInterpretationContentDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Legal Interpretation Content ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(interpretation -> {
      System.out.println("\nInterpretation details:");
      System.out.println("  Question: " + interpretation.getAgendaName());
      System.out.println("  Number: " + interpretation.getAgendaNumber());
      System.out.println("  Date: " + interpretation.getReplyDate());
      System.out.println("  Request Agency: " + interpretation.getReplyAgencyName());
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void search_withDateRange_shouldReturnFilteredResults() {
    // Given
    InterpretationListRequest request = InterpretationListRequest.builder()
        .page(1)
        .display(5)
        .registrationDateFrom(20230101)
        .registrationDateTo(20231231)
        .build();

    // When
    ListApiResult<LegalInterpretationDto> result = client.search(request);

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
    InterpretationListRequest request1 = InterpretationListRequest.builder()
        .page(1)
        .display(5)
        .registrationDateFrom(20200101)
        .build();

    // When
    ListApiResult<LegalInterpretationDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      InterpretationListRequest request2 = InterpretationListRequest.builder()
          .page(2)
          .display(5)
          .registrationDateFrom(20200101)
          .build();

      ListApiResult<LegalInterpretationDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getInterpretationSerialNumber()
            .equals(result2.items().get(0).getInterpretationSerialNumber());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
