package kr.go.law.precedent.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.go.law.common.response.ContentApiResult;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.precedent.dto.PrecedentContentDto;
import kr.go.law.precedent.dto.PrecedentListDto;
import kr.go.law.precedent.request.PrecedentListRequest;
import okhttp3.OkHttpClient;

/**
 * PrecedentApiClient 통합 테스트
 * <p>
 * 1947년부터 1개월 간격으로 목록을 조회하고,
 * 목록에서 얻은 key로 본문을 테스트합니다.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @Disabled("Integration test - requires actual API key")
class PrecedentApiClientIntegrationTest {

  private PrecedentApiClient client;

  /** 본문 테스트용 샘플 데이터 */
  record ContentSample(Integer precId, String dataSource, String caseName, String caseNumber) {}

  private final List<ContentSample> samples = new ArrayList<>();
  private int unexpectedCount = 0;
  private final List<String> unexpectedDetails = new ArrayList<>();
  private int apiErrorCount = 0;

  @BeforeAll
  void setUp() {
    String apiKey = System.getenv("LAW_OPEN_DATA_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      apiKey = "dykim2098";
    }

    LawOpenDataProperties properties = LawOpenDataProperties.builder()
        .oc(apiKey)
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    client = new PrecedentApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  @Order(1)
  void testList_shouldCollectSamples() {
    System.out.println("========== 판례 목록 테스트 (1947년~ 분기 간격) ==========\n");

    LocalDate startDate = LocalDate.of(1947, 1, 1);
    LocalDate endDate = LocalDate.now();
    int periodCount = 0;
    int totalItemCount = 0;

    while (startDate.isBefore(endDate)) {
      LocalDate periodEnd = startDate.plusMonths(3).minusDays(1);
      if (periodEnd.isAfter(endDate)) {
        periodEnd = endDate;
      }

      int fromDate = toDateInt(startDate);
      int toDate = toDateInt(periodEnd);

      PrecedentListRequest request = PrecedentListRequest.builder()
          .page(1)
          .display(10)
          .decisionDateFrom(fromDate)
          .decisionDateTo(toDate)
          .build();

      ListApiResult<PrecedentListDto> result = client.search(request);
      assertNotNull(result);
      periodCount++;

      // API 에러 체크
      if (result.hasError()) {
        apiErrorCount++;
        System.out.printf("[%d] %d ~ %d: API 에러 발생%n", periodCount, fromDate, toDate);
        startDate = startDate.plusMonths(3);
        sleep(100);
        continue;
      }

      int count = result.items().size();
      totalItemCount += count;

      if (count > 0) {
        System.out.printf("[%d] %d ~ %d: %d건 (총 %d건)%n",
            periodCount, fromDate, toDate, count, result.totalCount());

        // unexpected 필드 감지
        checkUnexpectedFields(result.items());

        // 10개 중 3개 샘플링 (인덱스 0, 4, 9 또는 가능한 범위)
        collectSamples(result.items(), count);
      }

      startDate = startDate.plusMonths(3);
      sleep(100); // API 서버 부하 방지
    }

    System.out.printf("%n######## 목록 테스트 완료 ########%n");
    System.out.printf("총 %d개 기간(분기) 조회, %d건 조회, %d개 샘플 수집%n",
        periodCount, totalItemCount, samples.size());
    System.out.printf("API 에러 %d건, UNEXPECTED %d건 감지%n", apiErrorCount, unexpectedCount);
    if (!unexpectedDetails.isEmpty()) {
      System.out.println("UNEXPECTED 상세:");
      unexpectedDetails.forEach(detail -> System.out.println("  - " + detail));
    }
    System.out.println("######## END 목록 테스트 ########\n");

    assertTrue(samples.size() > 0, "샘플이 수집되어야 합니다");
  }

  @Test
  @Order(2)
  void testContent_withSampledKeys() {
    ensureSamplesExist();

    System.out.println("========== 판례 본문 테스트 ==========\n");

    int successCount = 0;
    int failCount = 0;
    int errorCount = 0;

    for (ContentSample sample : samples) {
      System.out.printf("본문 조회: precId=%d, dataSource=%s (%s)%n",
          sample.precId(), sample.dataSource(), sample.caseName());

      // dataSource 기반으로 본문 조회 (fallback 로직 포함)
      ContentApiResult<PrecedentContentDto> result = client.getContent(sample.precId(), sample.dataSource());
      assertNotNull(result);

      if (result.hasError()) {
        System.out.printf("  -> API 에러 발생%n");
        errorCount++;
      } else if (result.content().isPresent()) {
        PrecedentContentDto content = result.content().get();
        System.out.printf("  -> 성공: 사건명=%s, 법원=%s, 선고일=%s%n",
            content.getCaseName(),
            content.getCourtName(),
            content.getDecisionDate());

        // 판시사항/요지 확인
        String summary = content.getSummary();
        if (summary != null && !summary.isBlank()) {
          System.out.printf("  -> 판시사항: %s%n",
              summary.length() > 50 ? summary.substring(0, 50) + "..." : summary);
        }
        successCount++;
      } else {
        System.out.printf("  -> 실패: 본문 없음%n");
        failCount++;
      }
      sleep(100);
    }

    System.out.printf("%n######## 본문 테스트 완료 ########%n");
    System.out.printf("성공=%d, 실패=%d, API에러=%d%n", successCount, failCount, errorCount);
    System.out.println("######## END 본문 테스트 ########");
  }

  /** unexpected 필드 감지 및 수집 */
  private void checkUnexpectedFields(List<PrecedentListDto> items) {
    for (PrecedentListDto item : items) {
      var unexpected = item.getUnexpected();
      if (unexpected != null && !unexpected.isEmpty()) {
        unexpectedCount++;
        unexpectedDetails.add(String.format("precId=%d, 사건명=%s: %s",
            item.getPrecId(), item.getCaseName(), unexpected));
      }
    }
  }

  /** 목록에서 3개 샘플링 (인덱스 0, 4, 9) */
  private void collectSamples(List<PrecedentListDto> items, int count) {
    int[] sampleIndices = {0, Math.min(4, count - 1), Math.min(9, count - 1)};

    for (int idx : sampleIndices) {
      if (idx < count) {
        PrecedentListDto item = items.get(idx);
        ContentSample sample = new ContentSample(
            item.getPrecId(),
            item.getDataSource(),
            item.getCaseName(),
            item.getCaseNumber()
        );
        // 중복 방지
        if (samples.stream().noneMatch(s -> s.precId().equals(sample.precId()))) {
          samples.add(sample);
          System.out.printf("  -> 샘플 추가: precId=%d, dataSource=%s, 사건명=%s%n",
              sample.precId(), sample.dataSource(), sample.caseName());
        }
      }
    }
  }

  /** 샘플이 비어있으면 일부 기간에서 수집 (개별 테스트 실행 시) */
  private void ensureSamplesExist() {
    if (!samples.isEmpty()) {
      return;
    }

    System.out.println("샘플 수집 중 (개별 테스트 모드)...\n");

    LocalDate[] testDates = {
        LocalDate.of(1948, 7, 1),
        LocalDate.of(1960, 1, 1),
        LocalDate.of(1980, 1, 1),
        LocalDate.of(2000, 1, 1),
        LocalDate.of(2020, 1, 1),
        LocalDate.now().minusMonths(1)
    };

    for (LocalDate date : testDates) {
      LocalDate periodEnd = date.plusMonths(1).minusDays(1);

      PrecedentListRequest request = PrecedentListRequest.builder()
          .page(1)
          .display(10)
          .decisionDateFrom(toDateInt(date))
          .decisionDateTo(toDateInt(periodEnd))
          .build();

      ListApiResult<PrecedentListDto> result = client.search(request);
      if (result != null && !result.items().isEmpty()) {
        collectSamples(result.items(), result.items().size());
      }

      if (samples.size() >= 10) break;
    }

    System.out.printf("샘플 %d개 수집 완료%n%n", samples.size());
  }

  private int toDateInt(LocalDate date) {
    return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
  }

  @SuppressWarnings("java:S2925") // 테스트 목적의 delay
  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
