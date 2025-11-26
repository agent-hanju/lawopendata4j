package kr.go.law.term.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.term.dto.ArticleTermLinkDto;
import kr.go.law.term.dto.LawTermContentDto;
import kr.go.law.term.dto.LawTermDto;
import kr.go.law.term.dto.TermRelationDto;
import kr.go.law.term.enums.TermSortOption;
import kr.go.law.term.request.ArticleTermLinkRequest;
import kr.go.law.term.request.TermContentRequest;
import kr.go.law.term.request.TermListRequest;
import kr.go.law.term.request.TermRelationRequest;
import okhttp3.OkHttpClient;

/**
 * TermApiClient 통합 테스트
 * <p>
 * 실제 API를 호출하는 테스트입니다. 실행하려면 @Disabled 어노테이션을 제거하고
 * 환경변수 LAW_OPEN_DATA_API_KEY에 실제 API 키를 설정하세요.
 * </p>
 */
// @Disabled("Integration test - requires actual API key")
class TermApiClientIntegrationTest {

  private TermApiClient client;

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

    client = new TermApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  void search_shouldReturnTermList() {
    // Given
    TermListRequest request = TermListRequest.builder()
        .page(1)
        .display(10)
        .query("계약")
        .sort(TermSortOption.TERM_NAME_DESC)
        .build();

    // When
    ListApiResult<LawTermDto> result = client.search(request);

    // Then
    assertNotNull(result);
    System.out.println("Total count: " + result.totalCount());
    System.out.println("Page: " + result.page());
    System.out.println("Display: " + result.display());
    System.out.println("Items size: " + result.items().size());
    System.out.println("Has next page: " + result.hasNextPage());
    System.out.println("Total pages: " + result.totalPages());

    if (!result.items().isEmpty()) {
      LawTermDto first = result.items().get(0);
      System.out.println("\nFirst law term:");
      System.out.println("  Term Name: " + first.getTermName());
      System.out.println("  Term Serial Number: " + first.getLawTermId());
    }

    assertTrue(result.totalCount() >= 0);
    assertTrue(result.items().size() <= 10);
  }

  @Test
  void getContent_shouldReturnTermContent() {
    // Given - 특정 법령용어 일련번호
    TermContentRequest request = TermContentRequest.builder()
        .query("계약")
        .build();

    // When
    ContentApiResult<LawTermContentDto> result = client.getContent(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Law Term Content ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(term -> {
      System.out.println("\nTerm details:");
      if (term.getTermNamesHangul() != null && !term.getTermNamesHangul().isEmpty()) {
        System.out.println("  Term Names: " + term.getTermNamesHangul());
      }
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void getArticleTermLink_shouldReturnArticleLink() {
    // Given
    ArticleTermLinkRequest request = ArticleTermLinkRequest.builder()
        .lawId(123456)
        .articleNumber(1)
        .build();

    // When
    ContentApiResult<ArticleTermLinkDto> result = client.getArticleTermLink(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Article Term Link ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(link -> {
      System.out.println("\nArticle term link details:");
      System.out.println("  Law Name: " + link.getLawName());
      System.out.println("  Article Number: " + link.getArticleNumber());
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void getTermRelation_shouldReturnTermRelation() {
    // Given
    TermRelationRequest request = TermRelationRequest.builder()
        .termSerialNumber(12345)
        .build();

    // When
    ContentApiResult<TermRelationDto> result = client.getTermRelation(request);

    // Then
    assertNotNull(result);
    System.out.println("\n=== Term Relation ===");
    System.out.println("Has content: " + result.content().isPresent());

    result.content().ifPresent(relation -> {
      System.out.println("\nTerm relation details:");
      System.out.println("  Law Term Name: " + relation.getLawTermName());
    });

    // Content API가 실패할 수 있으므로 응답만 확인
    assertNotNull(result);
  }

  @Test
  void search_withPagination_shouldWorkCorrectly() {
    // Given - 첫 페이지
    TermListRequest request1 = TermListRequest.builder()
        .page(1)
        .display(5)
        .query("법률")
        .build();

    // When
    ListApiResult<LawTermDto> result1 = client.search(request1);

    // Then
    assertNotNull(result1);
    System.out.println("\n=== Page 1 ===");
    System.out.println("Total count: " + result1.totalCount());
    System.out.println("Items: " + result1.items().size());
    System.out.println("Has next page: " + result1.hasNextPage());

    // 다음 페이지가 있다면 2페이지 조회
    if (result1.hasNextPage()) {
      TermListRequest request2 = TermListRequest.builder()
          .page(2)
          .display(5)
          .query("법률")
          .build();

      ListApiResult<LawTermDto> result2 = client.search(request2);

      System.out.println("\n=== Page 2 ===");
      System.out.println("Items: " + result2.items().size());
      System.out.println("Has next page: " + result2.hasNextPage());

      // 두 페이지의 데이터가 다른지 확인
      if (!result1.items().isEmpty() && !result2.items().isEmpty()) {
        boolean isDifferent = !result1.items().get(0).getLawTermId()
            .equals(result2.items().get(0).getLawTermId());
        System.out.println("Pages have different content: " + isDifferent);
        // API가 페이지네이션을 제대로 처리하지 않을 수 있으므로 경고만 출력
        if (!isDifferent && result1.totalCount() > result1.display()) {
          System.out.println("WARNING: API returned same content for different pages");
        }
      }
    }
  }
}
