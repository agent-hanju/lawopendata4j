# lawopendata4j

[국가법령정보 공동활용](https://open.law.go.kr) Open API Java 클라이언트 라이브러리

> **주의: 이 라이브러리는 현재 개발 중입니다.**
>
> API 구조, DTO 필드명, 메서드 시그니처 등이 예고 없이 변경될 수 있습니다.
> 프로덕션 환경에서 사용 시 버전을 고정하고, 업데이트 전 변경사항을 확인하세요.

## 개요

`lawopendata4j`는 법제처에서 제공하는 [국가법령정보 공동활용](https://open.law.go.kr) 사이트의 Open API를 Java로 쉽게 사용할 수 있도록 지원하는 클라이언트 라이브러리입니다.

### 지원 API

| API               | 설명                     | Request 클래스                                                         |
| ----------------- | ------------------------ | ---------------------------------------------------------------------- |
| 법령 (eflaw, law) | 법령 목록/본문/연혁 조회 | `StatuteListRequest`, `StatuteContentRequest`, `StatuteHistoryRequest` |
| 판례 (prec)       | 판례 목록/본문 조회      | `PrecedentListRequest`, `PrecedentContentRequest`                      |

> 헌재결정례, 법령해석례, 행정심판례, 법령용어 등은 아직 지원하지 않습니다.

## API 키 발급

**중요**: 국가법령정보 공동활용 Open API를 사용하려면 다음 조건을 모두 충족해야 합니다:

1. **API 키(OC) 발급**: [국가법령정보 공동활용](https://open.law.go.kr)에서 회원가입 후 API 키 신청
2. **서버 IP 등록**: API 요청을 보낼 서버의 IP 주소를 사전에 등록해야 합니다. 등록되지 않은 IP에서의 요청은 차단됩니다.

자세한 내용은 [국가법령정보 공동활용 안내](https://open.law.go.kr) 페이지를 참고하세요.

## 설치

### Gradle

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.agent-hanju:lawopendata4j:0.0.4'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.agent-hanju</groupId>
    <artifactId>lawopendata4j</artifactId>
    <version>0.0.4</version>
</dependency>
```

---

## 빠른 시작

### 클라이언트 생성

```java
import kr.go.law.LawOpenDataClient;

// API 키만으로 간단히 생성
LawOpenDataClient client = new LawOpenDataClient("YOUR_API_KEY");

// 또는 Builder 패턴 사용
LawOpenDataClient client = LawOpenDataClient.builder()
    .oc("YOUR_API_KEY")
    .connectionTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(60))
    .maxRetries(5)
    .build();
```

### 법령 목록 조회

```java
import kr.go.law.statute.request.StatuteListRequest;
import kr.go.law.statute.dto.StatuteListDto;
import kr.go.law.common.response.ListApiResult;

StatuteListRequest request = StatuteListRequest.builder()
    .query("개인정보")
    .display(20)
    .build();

ListApiResult<StatuteListDto> result = client.statute().search(request);

System.out.println("총 " + result.totalCount() + "건");
for (StatuteListDto statute : result.items()) {
    System.out.println(statute.getLsNm() + " - " + statute.getAncYd());
}
```

### 법령 본문 조회

```java
import kr.go.law.statute.request.StatuteContentRequest;
import kr.go.law.statute.dto.StatuteContentDto;
import kr.go.law.common.response.ContentApiResult;

StatuteContentRequest request = StatuteContentRequest.builder()
    .mst(253527)  // 법령일련번호
    .build();

ContentApiResult<StatuteContentDto> result = client.statute().getContent(request);

result.content().ifPresent(dto -> {
    System.out.println("법령명: " + dto.getBasicInfo().getLsNm());
    System.out.println("조문 수: " + dto.getArticles().size());
});
```

### 판례 목록 조회

```java
import kr.go.law.precedent.request.PrecedentListRequest;
import kr.go.law.precedent.dto.PrecedentListDto;

PrecedentListRequest request = PrecedentListRequest.builder()
    .query("손해배상")
    .display(20)
    .build();

ListApiResult<PrecedentListDto> result = client.precedent().search(request);

for (PrecedentListDto prec : result.items()) {
    System.out.println(prec.getCaseName() + " - " + prec.getCaseNumber());
}
```

### 판례 본문 조회

```java
import kr.go.law.precedent.request.PrecedentContentRequest;
import kr.go.law.precedent.dto.PrecedentContentDto;

// 기본 조회
PrecedentContentRequest request = PrecedentContentRequest.builder()
    .id(123456)
    .build();
ContentApiResult<PrecedentContentDto> result = client.precedent().getContent(request);

// 또는 dataSource 기반 조회 (목록 API의 dataSource 값 활용)
ContentApiResult<PrecedentContentDto> result = client.precedent().getContent(precId, dataSource);
```

---

## 클라이언트 설정

### 설정 옵션

| 옵션                | 타입     | 기본값 | 설명                     |
| ------------------- | -------- | ------ | ------------------------ |
| `oc`                | String   | -      | **필수**. API 키         |
| `connectionTimeout` | Duration | 10초   | 서버 연결 타임아웃       |
| `readTimeout`       | Duration | 30초   | 응답 읽기 타임아웃       |
| `maxRetries`        | int      | 3      | 요청 실패 시 재시도 횟수 |
| `retryDelay`        | Duration | 1초    | 재시도 간 대기 시간      |
| `maxConnection`     | int      | 5      | 커넥션 풀 최대 연결 수   |
| `keepAliveDuration` | Duration | 5분    | 유휴 연결 유지 시간      |

### 커스텀 설정 예시

```java
import java.time.Duration;
import kr.go.law.LawOpenDataClient;
import kr.go.law.config.LawOpenDataProperties;

LawOpenDataProperties properties = LawOpenDataProperties.builder()
    .oc("YOUR_API_KEY")
    .connectionTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(60))
    .maxRetries(5)
    .retryDelay(Duration.ofMillis(500))
    .maxConnection(10)
    .keepAliveDuration(Duration.ofMinutes(10))
    .build();

LawOpenDataClient client = new LawOpenDataClient(properties);
```

### 커스텀 ObjectMapper / OkHttpClient 사용

```java
ObjectMapper customMapper = new ObjectMapper();
OkHttpClient customClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .build();

LawOpenDataClient client = new LawOpenDataClient(properties, customMapper, customClient);
```

---

## 스프링부트 연동

### application.yml

```yaml
law:
  opendata:
    oc: ${LAW_API_KEY}
    connection-timeout: 10s
    read-timeout: 30s
    max-retries: 3
    retry-delay: 1s
    max-connection: 5
    keep-alive-duration: 5m
```

### Configuration

```java
import kr.go.law.LawOpenDataClient;
import kr.go.law.config.LawOpenDataProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LawOpenDataConfig {

    @Bean
    @ConfigurationProperties(prefix = "law.opendata")
    public LawOpenDataProperties lawOpenDataProperties() {
        return LawOpenDataProperties.builder().build();
    }

    @Bean
    public LawOpenDataClient lawOpenDataClient(LawOpenDataProperties properties) {
        return new LawOpenDataClient(properties);
    }
}
```

### Service에서 사용

```java
@Service
@RequiredArgsConstructor
public class LawService {

    private final LawOpenDataClient lawClient;

    public List<StatuteListDto> searchStatutes(String query) {
        StatuteListRequest request = StatuteListRequest.builder()
            .query(query)
            .display(20)
            .build();

        return lawClient.statute().search(request).items();
    }
}
```

---

## 응답 구조

### ListApiResult (목록 조회)

```java
ListApiResult<StatuteListDto> result = client.statute().search(request);

result.rawData();      // 원본 JSON 문자열
result.items();        // List<T> 파싱된 DTO 목록
result.totalCount();   // 전체 건수
result.page();         // 현재 페이지
result.display();      // 페이지당 건수
result.hasError();     // 에러 여부
```

### ContentApiResult (본문 조회)

```java
ContentApiResult<StatuteContentDto> result = client.statute().getContent(request);

result.rawData();      // 원본 JSON 문자열
result.content();      // Optional<T> 파싱된 DTO
result.hasError();     // 에러 여부

// 사용 예시
result.content().ifPresent(dto -> {
    // dto 처리
});
```

---

## 주요 DTO

### 법령 (Statute)

| DTO                 | 용도                       |
| ------------------- | -------------------------- |
| `StatuteListDto`    | 목록 API 응답              |
| `StatuteContentDto` | 본문 API 응답              |
| `StatuteHistoryDto` | 연혁 API 응답              |
| `ArticleContentDto` | 조문 (본문 API 내 포함)    |
| `Hang`, `Ho`, `Mok` | 항/호/목 (조문 하위 구조)  |
| `Addendum`          | 부칙                       |
| `Appendix`          | 별표                       |
| `CoOrdinanceInfo`   | 공동부령 정보              |

### 판례 (Precedent)

| DTO                   | 용도          |
| --------------------- | ------------- |
| `PrecedentListDto`    | 목록 API 응답 |
| `PrecedentContentDto` | 본문 API 응답 |

---

## 공통 사항

### 날짜 형식

날짜 파라미터는 `YYYYMMDD` 형태의 Integer로 입력합니다:

```java
.promulgationDateFrom(20240101)  // 2024년 1월 1일
.promulgationDateTo(20241231)    // 2024년 12월 31일
```

### ID 자동 변환

숫자 형태의 ID 파라미터는 자동으로 zero-padding 처리됩니다:

```java
// lawId: 1233 → "001233" (6자리)
// organizationCode: 1371000 → "1371000" (7자리)
```

### unexpected 필드

모든 DTO는 `BaseDto`를 상속하며, API 응답에 포함되었지만 DTO에 정의되지 않은 필드들은 `unexpected` Map에 저장됩니다:

```java
Map<String, String> unexpected = dto.getUnexpected();
if (unexpected != null && !unexpected.isEmpty()) {
    log.warn("Unexpected fields: {}", unexpected);
}
```

---

## 의존성

- Java 17+
- OkHttp 4.x
- Jackson 2.x
- Lombok

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 참고

- [국가법령정보 공동활용](https://open.law.go.kr)
- [법제처 법령정보 Open API 안내](https://www.law.go.kr/LSO/openApi.do)
