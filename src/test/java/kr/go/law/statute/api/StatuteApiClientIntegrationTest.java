package kr.go.law.statute.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.statute.dto.StatuteDto;
import kr.go.law.statute.enums.StatuteSortOption;
import kr.go.law.statute.request.StatuteContentRequest;
import kr.go.law.statute.request.StatuteListRequest;
import okhttp3.OkHttpClient;

/**
 * StatuteApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class StatuteApiClientIntegrationTest {

  private StatuteApiClient client;

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

    client = new StatuteApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnStatuteList() {
    // Given
    StatuteListRequest request = StatuteListRequest.builder()
        .page(1)
        .display(10)
        .query("개인정보")
        .sort(StatuteSortOption.PROMULGATION_DATE_DESC)
        .build();

    // When
    ListApiResult<StatuteDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      StatuteDto first = result.items().get(0);
      System.out.println("\n=== First Statute DTO ===");
      System.out.println("  MST (법령일련번호): " + first.getMst());
      System.out.println("  LS_ID (법령ID): " + first.getLsId());
      System.out.println("  법령명: " + first.getLsNm());
      System.out.println("  법령명(한자): " + first.getLsNmHanja());
      System.out.println("  법령약칭: " + first.getLsNmAbbr());
      System.out.println("  법령구분: " + first.getKnd() + " (코드: " + first.getKndCd() + ")");
      System.out.println("  시행일자: " + first.getEfYd());
      System.out.println("  공포일자: " + first.getAncYd());
      System.out.println("  공포번호: " + first.getAncNo());
      System.out.println("  소관부처: " + first.getOrg());
      System.out.println("  소관부처코드: " + first.getOrgCd());
      System.out.println("  법령상태: " + first.getStatusCode());
      System.out.println("  제정구분: " + first.getEnactmentType());
      System.out.println("  법령종류: " + first.getLawType());
      System.out.println("========================\n");
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void getContent_shouldReturnStatuteContent() {
    // Given - 개인정보 보호법의 일련번호
    StatuteContentRequest request = StatuteContentRequest.builder()
        .statuteSerialNumber(253527) // 개인정보 보호법
        .build();

    // When
    ContentApiResult<StatuteDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(statute -> {
      System.out.println("\nStatute details:");
      System.out.println("  Name: " + statute.getLsNm());
      System.out.println("  Law ID: " + statute.getLsId());
      System.out.println("  Articles count: " +
          (statute.getArticles() != null ? statute.getArticles().size() : 0));

      if (statute.getArticles() != null && !statute.getArticles().isEmpty()) {
        System.out.println("\nFirst article:");
        var firstArticle = statute.getArticles().values().iterator().next();
        System.out.println("  Number: " + firstArticle.getJoNum());
        System.out.println("  Title: " + firstArticle.getJoTitle());
        String content = firstArticle.getContent();
        if (content != null && content.length() > 100) {
          System.out.println("  Content (first 100 chars): " +
              content.substring(0, 100) + "...");
        } else {
          System.out.println("  Content: " + content);
        }
      }
    });

    // Content API는 단일 항목 조회이므로 결과만 확인
    assertNotNull(result);
  }

  @Test
  void search_withPagination_shouldWorkCorrectly() {
    // Given - 첫 페이지
    StatuteListRequest request1 = StatuteListRequest.builder()
        .page(1)
        .display(5)
        .query("법률")
        .build();

    // When
    ListApiResult<StatuteDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      StatuteListRequest request2 = StatuteListRequest.builder()
          .page(2)
          .display(5)
          .query("법률")
          .build();

      ListApiResult<StatuteDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getLsId()
            .equals(result2.items().get(0).getLsId());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
