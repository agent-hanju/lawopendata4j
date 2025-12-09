package kr.go.law.statute.api;

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
import kr.go.law.statute.dto.StatuteContentDto;
import kr.go.law.statute.dto.StatuteListDto;
import kr.go.law.statute.request.EfYdLawContentRequest;
import kr.go.law.statute.request.StatuteContentRequest;
import kr.go.law.statute.request.StatuteListRequest;
import okhttp3.OkHttpClient;

/**
 * StatuteApiClient 통합 테스트
 * <p>
 * 1947년부터 분기 간격으로 목록을 조회하고,
 * 목록에서 얻은 key로 본문을 테스트합니다. (공포일자 기준, 시행일자 기준)
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @Disabled("Integration test - requires actual API key")
class StatuteApiClientIntegrationTest {

  private StatuteApiClient client;

  /** 본문 테스트용 샘플 데이터 */
  record ContentSample(Integer mst, Integer efYd, Integer ancYd, Integer lsId, String lsNm) {}

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

    client = new StatuteApiClient(properties, objectMapper, okHttpClient);
  }

  @Test
  @Order(1)
  void testList_shouldCollectSamples() {
    System.out.println("========== 법령 목록 테스트 (1947년~ 분기 간격) ==========\n");

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

      StatuteListRequest request = StatuteListRequest.builder()
          .page(1)
          .display(10)
          .promulgationDateFrom(fromDate)
          .promulgationDateTo(toDate)
          .build();

      ListApiResult<StatuteListDto> result = client.search(request);
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

        // 첫 번째 항목 샘플링
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

    System.out.println("========== 법령 본문 테스트 (공포일자 기준) ==========\n");

    int successCount = 0;
    int failCount = 0;
    int errorCount = 0;

    for (ContentSample sample : samples) {
      System.out.printf("본문 조회: mst=%d (%s)%n", sample.mst(), sample.lsNm());

      StatuteContentRequest request = StatuteContentRequest.builder()
          .mst(sample.mst())
          .build();

      ContentApiResult<StatuteContentDto> result = client.getContent(request);
      assertNotNull(result);

      if (result.hasError()) {
        System.out.printf("  -> API 에러 발생%n");
        errorCount++;
      } else if (result.content().isPresent()) {
        StatuteContentDto content = result.content().get();
        var basicInfo = content.getBasicInfo();
        int articleCount = content.getArticles() != null ? content.getArticles().size() : 0;
        System.out.printf("  -> 성공: 법령명=%s, 조문수=%d%n",
            basicInfo != null ? basicInfo.getLsNm() : "N/A", articleCount);
        successCount++;
      } else {
        System.out.printf("  -> 실패: 본문 없음%n");
        failCount++;
      }
      // Statute Content API는 응답시간이 1초 이상이므로 추가 딜레이 불필요
    }

    System.out.printf("%n######## 본문 테스트 (공포일자 기준) 완료 ########%n");
    System.out.printf("성공=%d, 실패=%d, API에러=%d%n", successCount, failCount, errorCount);
    System.out.println("######## END 본문 테스트 (공포일자) ########\n");
  }

  @Test
  @Order(3)
  void testContentByEfYd_withSampledKeys() {
    ensureSamplesExist();

    System.out.println("========== 법령 본문 테스트 (시행일자 기준) ==========\n");

    int successCount = 0;
    int failCount = 0;
    int errorCount = 0;

    for (ContentSample sample : samples) {
      if (sample.efYd() == null) {
        System.out.printf("본문 조회 스킵: efYd 없음 (%s)%n", sample.lsNm());
        failCount++;
        continue;
      }

      System.out.printf("본문 조회 (efYd): mst=%d, efYd=%d (%s)%n",
          sample.mst(), sample.efYd(), sample.lsNm());

      EfYdLawContentRequest request = EfYdLawContentRequest.builder()
          .mst(sample.mst())
          .efYd(sample.efYd())
          .build();

      ContentApiResult<StatuteContentDto> result = client.getContentByEfYd(request);
      assertNotNull(result);

      if (result.hasError()) {
        System.out.printf("  -> API 에러 발생%n");
        errorCount++;
      } else if (result.content().isPresent()) {
        StatuteContentDto content = result.content().get();
        var basicInfo = content.getBasicInfo();
        int articleCount = content.getArticles() != null ? content.getArticles().size() : 0;
        System.out.printf("  -> 성공: 법령명=%s, 조문수=%d%n",
            basicInfo != null ? basicInfo.getLsNm() : "N/A", articleCount);
        successCount++;
      } else {
        System.out.printf("  -> 실패: 본문 없음%n");
        failCount++;
      }
      // Statute Content API는 응답시간이 1초 이상이므로 추가 딜레이 불필요
    }

    System.out.printf("%n######## 본문 테스트 (시행일자 기준) 완료 ########%n");
    System.out.printf("성공=%d, 실패=%d, API에러=%d%n", successCount, failCount, errorCount);
    System.out.println("######## END 본문 테스트 (시행일자) ########");
  }

  /** unexpected 필드 감지 및 수집 */
  private void checkUnexpectedFields(List<StatuteListDto> items) {
    for (StatuteListDto item : items) {
      var unexpected = item.getUnexpected();
      if (unexpected != null && !unexpected.isEmpty()) {
        unexpectedCount++;
        unexpectedDetails.add(String.format("mst=%d, 법령명=%s: %s",
            item.getMst(), item.getLsNm(), unexpected));
      }
    }
  }

  /** 목록에서 1개 샘플링 (첫 번째 항목) */
  private void collectSamples(List<StatuteListDto> items, int count) {
    if (count > 0) {
      StatuteListDto item = items.get(0);
      ContentSample sample = new ContentSample(
          item.getMst(),
          item.getEfYd(),
          item.getAncYd(),
          item.getLsId(),
          item.getLsNm()
      );
      // 중복 방지
      if (samples.stream().noneMatch(s -> s.mst().equals(sample.mst()))) {
        samples.add(sample);
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
        LocalDate.of(1948, 7, 1),   // 제헌헌법 시기
        LocalDate.of(1960, 1, 1),
        LocalDate.of(1980, 1, 1),
        LocalDate.of(2000, 1, 1),
        LocalDate.of(2020, 1, 1),
        LocalDate.now().minusMonths(1)
    };

    for (LocalDate date : testDates) {
      LocalDate periodEnd = date.plusMonths(1).minusDays(1);

      StatuteListRequest request = StatuteListRequest.builder()
          .page(1)
          .display(10)
          .promulgationDateFrom(toDateInt(date))
          .promulgationDateTo(toDateInt(periodEnd))
          .build();

      ListApiResult<StatuteListDto> result = client.search(request);
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
