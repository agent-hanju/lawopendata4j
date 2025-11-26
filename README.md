# lawopendata4j

대한민국 법령정보 Open API (law.go.kr) Java 클라이언트 라이브러리

## 개요

`lawopendata4j`는 [법제처 법령정보 Open API](https://www.law.go.kr/LSO/openApi.do)를 Java로 쉽게 사용할 수 있도록 지원하는 클라이언트 라이브러리입니다.

### 지원 API

| API | 설명 | Request 클래스 |
|-----|------|---------------|
| 법령 (eflaw, law) | 법령 목록/본문/연혁 조회 | `StatuteListRequest`, `StatuteContentRequest`, `StatuteHistoryRequest` |
| 판례 (prec) | 판례 목록/본문 조회 | `PrecedentListRequest`, `PrecedentContentRequest` |
| 헌재결정례 (detc) | 헌법재판소 결정례 조회 | `ConstitutionalListRequest`, `ConstitutionalContentRequest` |
| 법령해석례 (expc) | 법령해석례 조회 | `InterpretationListRequest`, `InterpretationContentRequest` |
| 행정심판례 (decc) | 행정심판례 조회 | `AdministrativeListRequest`, `AdministrativeContentRequest` |
| 법령용어 (lstrm) | 법령용어 조회 | `TermListRequest`, `TermContentRequest`, `ArticleTermLinkRequest`, `TermRelationRequest` |
| 위원회결정문 | 위원회결정문 조회 | (레거시 API) |

## API 키 발급

법령정보 Open API를 사용하려면 API 키(OC)가 필요합니다.

1. [법제처 Open API 페이지](https://www.law.go.kr/LSO/openApi.do)에서 회원가입
2. API 키 신청 후 발급받은 OC 코드 사용

## 설치

### Gradle

```groovy
implementation 'com.github.agent-hanju:lawopendata4j:0.0.1'
```

### Maven

```xml
<dependency>
    <groupId>com.github.agent-hanju</groupId>
    <artifactId>lawopendata4j</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 사용법

### 기본 사용법

```java
import kr.go.law.LawOpenDataClient;
import kr.go.law.config.LawOpenDataProperties;

// Properties 생성
LawOpenDataProperties properties = LawOpenDataProperties.builder()
    .apiKey("YOUR_API_KEY")
    .build();

// 클라이언트 생성
LawOpenDataClient client = new LawOpenDataClient(properties);
```

### 스프링부트에서 사용하기

#### 1. application.yml 설정

```yaml
law:
  opendata:
    api-key: ${LAW_API_KEY:your-api-key-here}
```

#### 2. Configuration 클래스 작성

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

#### 3. 서비스에서 사용

```java
import kr.go.law.LawOpenDataClient;
import kr.go.law.common.response.ListApiResult;
import kr.go.law.statute.dto.StatuteDto;
import kr.go.law.statute.request.StatuteListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LawService {

    private final LawOpenDataClient lawClient;

    public ListApiResult<StatuteDto> searchStatutes(String query) {
        StatuteListRequest request = StatuteListRequest.builder()
            .query(query)
            .display(20)
            .build();

        return lawClient.statute().search(request);
    }
}
```

#### 4. 컨트롤러에서 사용

```java
import kr.go.law.common.response.ListApiResult;
import kr.go.law.statute.dto.StatuteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/law")
@RequiredArgsConstructor
public class LawController {

    private final LawService lawService;

    @GetMapping("/statutes")
    public ListApiResult<StatuteDto> searchStatutes(
        @RequestParam String query) {
        return lawService.searchStatutes(query);
    }
}
```

### 일반 Java 프로젝트에서 사용하기

```java
import kr.go.law.LawOpenDataClient;
import kr.go.law.config.LawOpenDataProperties;
import kr.go.law.statute.request.StatuteListRequest;
import kr.go.law.statute.dto.StatuteDto;

public class LawSearchExample {
    public static void main(String[] args) {
        // 클라이언트 생성
        LawOpenDataProperties properties = LawOpenDataProperties.builder()
            .apiKey("YOUR_API_KEY")
            .build();

        LawOpenDataClient client = new LawOpenDataClient(properties);

        // 법령 검색
        StatuteListRequest request = StatuteListRequest.builder()
            .query("개인정보")
            .display(10)
            .build();

        var result = client.statute().search(request);

        System.out.println("총 " + result.totalCount() + "건 검색됨");

        for (StatuteDto statute : result.items()) {
            System.out.println(statute.getLsNm() + " - " + statute.getAncYd());
        }
    }
}
```

---

## 법령 API (Statute)

### 법령 목록 조회

```java
StatuteListRequest request = StatuteListRequest.builder()
    .page(1)
    .display(100)
    .sort(StatuteSortOption.PROMULGATION_DATE_DESC)  // 공포일 내림차순
    .query("개인정보")
    .promulgationDateFrom(20240101)
    .promulgationDateTo(20241231)
    .statuses(List.of(StatuteStatus.CURRENT))  // 현행 법령만
    .build();

StatuteApiClient.ListApiResult result = client.statute().search(request);

// 결과 처리
List<StatuteDto> statutes = result.statutes();
int totalCount = result.totalCount();
String rawJson = result.rawData();  // 원본 JSON 데이터
```

#### StatuteListRequest 파라미터

| 파라미터 | 타입 | API 파라미터 | 설명 |
|----------|------|--------------|------|
| `page` | Integer | page | 페이지 번호 (기본값: 1) |
| `display` | Integer | display | 페이지당 표시 건수 (기본값: 20, 최대: 100) |
| `sort` | StatuteSortOption | sort | 정렬 옵션 |
| `query` | String | query | 법령명 검색어 |
| `promulgationDateFrom` | Integer | ancYd | 공포일 시작 (YYYYMMDD) |
| `promulgationDateTo` | Integer | ancYd | 공포일 종료 (YYYYMMDD) |
| `effectiveDateFrom` | Integer | efYd | 시행일 시작 (YYYYMMDD) |
| `effectiveDateTo` | Integer | efYd | 시행일 종료 (YYYYMMDD) |
| `lawId` | Integer | LID | 법령ID (6자리, 자동 zero-padding) |
| `statuses` | List<StatuteStatus> | nw | 법령 상태 (CURRENT/HISTORY/SCHEDULED) |
| `organizationCode` | Integer | org | 소관부처코드 (7자리, 자동 zero-padding) |
| `kindCode` | String | knd | 법령종류코드 (A0101=헌법, A0102=법률 등) |

#### StatuteSortOption

| 옵션 | 설명 |
|------|------|
| `LAW_NAME_ASC` / `LAW_NAME_DESC` | 법령명 오름차순/내림차순 |
| `PROMULGATION_DATE_ASC` / `PROMULGATION_DATE_DESC` | 공포일 오름차순/내림차순 |
| `EFFECTIVE_DATE_ASC` / `EFFECTIVE_DATE_DESC` | 시행일 오름차순/내림차순 |

### 법령 본문 조회

```java
StatuteContentRequest request = StatuteContentRequest.builder()
    .statuteSerialNumber(253527)  // 법령일련번호 (필수)
    .build();

StatuteApiClient.ContentApiResult result = client.statute().getContent(request, null);
Optional<StatuteDto> statute = result.statute();
```

### 조문 개정 이력 조회

```java
StatuteHistoryRequest request = StatuteHistoryRequest.builder()
    .lawId(2132)
    .revisionDate(20240101)
    .page(1)
    .build();

StatuteApiClient.HistoryApiResult result = client.statute().searchHistory(request, null);
Map<Integer, ArticleDto> articles = result.articles();
```

---

## 판례 API (Precedent)

### 판례 목록 조회

```java
PrecedentListRequest request = PrecedentListRequest.builder()
    .page(1)
    .display(100)
    .sort(PrecedentSortOption.DECISION_DATE_DESC)  // 선고일 내림차순
    .query("손해배상")
    .searchScope(SearchScope.CASE_NAME)  // 판례명에서 검색
    .decisionDateFrom(20240101)
    .decisionDateTo(20241231)
    .courtType(CourtType.SUPREME_COURT)  // 대법원 판례만
    .build();

PrecedentApiClient.ListApiResult result = client.precedent().search(request);
```

#### PrecedentListRequest 파라미터

| 파라미터 | 타입 | API 파라미터 | 설명 |
|----------|------|--------------|------|
| `page` | Integer | page | 페이지 번호 (기본값: 1) |
| `display` | Integer | display | 페이지당 표시 건수 (기본값: 20, 최대: 100) |
| `sort` | PrecedentSortOption | sort | 정렬 옵션 |
| `query` | String | query | 검색어 |
| `searchScope` | SearchScope | search | 검색 범위 (CASE_NAME/CONTENT) |
| `decisionDateFrom` | Integer | prncYd | 선고일 시작 (YYYYMMDD) |
| `decisionDateTo` | Integer | prncYd | 선고일 종료 (YYYYMMDD) |
| `decisionDate` | Integer | date | 특정 선고일 (YYYYMMDD) |
| `courtType` | CourtType | org | 법원 종류 (SUPREME_COURT/LOWER_COURT) |
| `courtName` | String | curt | 법원명 |
| `caseNumber` | String | nb | 사건번호 |
| `referenceLaw` | String | JO | 참조법령명 |
| `dataSource` | String | datSrcNm | 데이터출처명 |

### 판례 본문 조회

```java
PrecedentContentRequest request = PrecedentContentRequest.builder()
    .id(123456)  // 판례ID (필수)
    .build();

PrecedentApiClient.ContentApiResult result = client.precedent().getContent(request);
```

---

## 헌재결정례 API (Constitutional)

### 헌재결정례 목록 조회

```java
ConstitutionalListRequest request = ConstitutionalListRequest.builder()
    .page(1)
    .display(50)
    .sort(ConstitutionalSortOption.DECISION_DATE_DESC)
    .query("위헌")
    .decisionDateFrom(20240101)
    .build();

ConstitutionalApiClient.ListApiResult result = client.constitutional().search(request);
```

### 헌재결정례 본문 조회

```java
ConstitutionalContentRequest request = ConstitutionalContentRequest.builder()
    .id(12345)  // 필수
    .build();

ConstitutionalApiClient.ContentApiResult result = client.constitutional().getContent(request);
```

---

## 법령해석례 API (Interpretation)

### 법령해석례 목록 조회

```java
InterpretationListRequest request = InterpretationListRequest.builder()
    .page(1)
    .display(50)
    .sort(InterpretationSortOption.INTERPRETATION_DATE_DESC)
    .query("행정절차")
    .build();

InterpretationApiClient.ListApiResult result = client.interpretation().search(request);
```

### 법령해석례 본문 조회

```java
InterpretationContentRequest request = InterpretationContentRequest.builder()
    .id(12345)  // 필수
    .build();

InterpretationApiClient.ContentApiResult result = client.interpretation().getContent(request);
```

---

## 행정심판례 API (Administrative)

### 행정심판례 목록 조회

```java
AdministrativeListRequest request = AdministrativeListRequest.builder()
    .page(1)
    .display(50)
    .sort(AdministrativeSortOption.ADJUDICATION_DATE_DESC)
    .query("취소")
    .build();

AdministrativeApiClient.ListApiResult result = client.administrative().search(request);
```

### 행정심판례 본문 조회

```java
AdministrativeContentRequest request = AdministrativeContentRequest.builder()
    .id(12345)  // 필수
    .build();

AdministrativeApiClient.ContentApiResult result = client.administrative().getContent(request);
```

---

## 법령용어 API (Term)

### 법령용어 목록 조회

```java
TermListRequest request = TermListRequest.builder()
    .page(1)
    .display(100)
    .sort(TermSortOption.RELEVANCE_DESC)
    .query("청원")
    .build();

TermApiClient.ListApiResult result = client.term().search(request);
```

### 법령용어 본문 조회

```java
TermContentRequest request = TermContentRequest.builder()
    .query("청원")  // 필수
    .termSerialNumbers(List.of("12345", "67890"))  // 선택
    .build();

TermApiClient.ContentApiResult result = client.term().getContent(request);
```

### 조문-법령용어 연계 조회

```java
ArticleTermLinkRequest request = ArticleTermLinkRequest.builder()
    .lawId(1233)
    .articleNumber(400)  // 제4조 → 000400
    .build();

TermApiClient.ArticleLinkApiResult result = client.term().getArticleTermLink(request);
```

### 법령용어-일상용어 관계 조회

```java
TermRelationRequest request = TermRelationRequest.builder()
    .query("청원")
    .relationType(TermRelationType.SYNONYM)  // 동의어
    .build();

TermApiClient.RelationApiResult result = client.term().getTermRelation(request);
```

#### TermRelationType

| 옵션 | 코드 | 설명 |
|------|------|------|
| `SYNONYM` | 140301 | 동의어 |
| `ANTONYM` | 140302 | 반의어 |
| `HYPERNYM` | 140303 | 상위어 |
| `HYPONYM` | 140304 | 하위어 |
| `RELATED` | 140305 | 연관어 |

---

## DTO 스키마

원본 API 응답 필드명과 DTO 필드명의 매핑 정보입니다. `←` 뒤는 원본 API 필드명입니다.

### StatuteDto (법령)

```
StatuteDto {
  mst: Integer                      // 법령일련번호 (PK)        ← 법령일련번호
  efYd: Integer                     // 시행일 (YYYYMMDD)       ← 시행일자
  lsId: Integer                     // 법령 ID                 ← 법령ID
  statusCode: String                // 현행/연혁 코드          ← 현행연혁코드 (List API only)
  lawType: String                   // 자법/타법 구분          ← 자법타법여부 (List API only)
  lsNm: String                      // 법령명 (한글)           ← 법령명한글 / 기본정보.법령명_한글
  lsNmHanja: String                 // 법령명 (한자)           ← 법령명한자 / 기본정보.법령명_한자
  lsNmAbbr: String                  // 법령 약칭              ← 법령약칭명 / 기본정보.법령명약칭
  ancYd: Integer                    // 공포일 (YYYYMMDD)      ← 공포일자
  ancNo: Integer                    // 공포 번호              ← 공포번호
  enactmentType: String             // 제정/개정 구분          ← 제개정구분명 / 기본정보.제개정구분
  orgCd: List<String>               // 소관부처 코드           ← 소관부처코드 (콤마구분 → List)
  org: List<String>                 // 소관부처명              ← 소관부처명 (콤마구분 → List)
  knd: String                       // 법령 종류명             ← 법령구분명 / 기본정보.법종구분.content
  kndCd: String                     // 법령 종류 코드          ← 기본정보.법종구분.법종구분코드 (Content only)
  isAnc: String                     // 공포 법령 여부          ← 기본정보.공포법령여부 (Content only)
  pyeonjangjeolgwan: Integer        // 편장절관 구분           ← 기본정보.편장절관 (Content only)
  decisionBody: String              // 의결 기관              ← 기본정보.의결구분 (Content only)
  proposerType: String              // 제안 구분              ← 기본정보.제안구분 (Content only)
  phoneNumber: String               // 연락처                 ← 기본정보.전화번호 (Content only)
  language: String                  // 언어                   ← 기본정보.언어 (Content only)
  appendixEditYn: String            // 별표 편집 여부          ← 기본정보.별표편집여부 (Content only)
  contactInfo: List<Department>     // 연락 부서              ← 기본정보.연락부서.부서단위 (Content only)
  coEnactments: List<CoEnactment>   // 공동부령               ← 기본정보.공동부령정보 (Content only)
  amendment: String                 // 개정문                 ← 개정문.개정문내용 (Content only)
  amendmentReason: String           // 제개정 이유            ← 제개정이유.제개정이유내용 (Content only)
  addenda: List<Addendum>           // 부칙 목록              ← 부칙.부칙단위 (Content only)
  appendices: List<Appendix>        // 별표 목록              ← 별표.별표단위 (Content only)
  articles: Map<Integer, ArticleDto> // 조문 (joKey → ArticleDto)
}
```

### Department (연락 부서)

```
Department {                        // ← 기본정보.연락부서.부서단위
  부서키: String                     // 부서 고유키             ← 부서키
  소관부처코드: Integer               // 소관부처 코드           ← 소관부처코드
  소관부처명: String                  // 소관부처명             ← 소관부처명
  부서명: String                     // 부서명 (설명 포함)      ← 부서명
  부서연락처: String                  // 연락처                 ← 부서연락처
}
```

### CoEnactment (공동부령)

```
CoEnactment {                       // ← 기본정보.공동부령정보
  no: Integer                       // 순번                   ← no
  공포번호: Integer                   // 공포번호               ← 공포번호
  공동부령구분: String                // 구분명                 ← 공동부령구분.content
  공동부령구분코드: String            // 구분코드               ← 공동부령구분.구분코드
}
```

### Addendum (부칙)

```
Addendum {                          // ← 부칙.부칙단위
  부칙키: Long                       // 부칙 고유키             ← 부칙키
  부칙공포일자: Integer               // 공포일 (YYYYMMDD)      ← 부칙공포일자
  부칙공포번호: Integer               // 공포번호               ← 부칙공포번호
  부칙내용: String                   // 부칙 본문              ← 부칙내용
}
```

### Appendix (별표)

```
Appendix {                          // ← 별표.별표단위
  별표키: String                     // 별표 고유키             ← 별표키
  별표구분: String                   // 구분 (별표/서식)        ← 별표구분
  별표번호: Integer                  // 별표 번호              ← 별표번호
  별표가지번호: Integer               // 별표 가지번호          ← 별표가지번호
  별표제목: String                   // 제목                   ← 별표제목
  별표제목문자열: String              // 제목 (전체 문자열)      ← 별표제목문자열
  별표내용: String                   // 본문 내용              ← 별표내용
  별표시행일자: Integer               // 시행일 (YYYYMMDD)      ← 별표시행일자
  별표서식파일링크: String            // 서식 파일 링크          ← 별표서식파일링크
  별표서식PDF파일링크: String         // PDF 파일 링크          ← 별표서식PDF파일링크
  별표PDF파일명: String              // PDF 파일명             ← 별표PDF파일명
  별표HWP파일명: String              // HWP 파일명             ← 별표HWP파일명
  별표이미지파일명: List<String>      // 이미지 파일명 목록      ← 별표이미지파일명
}
```

### ArticleDto (조문)

```
ArticleDto {                        // ← 조문.조문단위
  mst: Integer                      // 소속 법령 일련번호 (라이브러리에서 설정)
  joKey: Integer                    // 조문 키 (PK)           ← 조문키
  lsId: Integer                     // 소속 법령 ID (라이브러리에서 설정)
  ancYd: Integer                    // 소속 법령 공포일 (라이브러리에서 설정)
  ancNo: Integer                    // 소속 법령 공포번호 (라이브러리에서 설정)
  lsNm: String                      // 소속 법령명 (라이브러리에서 설정)
  joNum: Integer                    // 조 번호                ← 조문번호
  joBrNum: Integer                  // 조 가지번호            ← 조문번호가지번호
  joTitle: String                   // 조문 제목              ← 조문제목
  joType: String                    // 조문 유형              ← 조문시행일자구분
  content: String                   // 조문 본문              ← 조문내용
  hangList: List<Hang>              // 항 목록                ← 조문내용.항
  amendedType: String               // 개정 유형              ← 조문시행일자구분
  amendedDateStr: String            // 개정 시행일 문자열      ← 조문시행일자
  isAmended: String                 // 개정 여부              ← 조문시행일자구분
  isChanged: Boolean                // History API 변경 여부 (라이브러리에서 설정)
  efYd: Integer                     // 조문 시행일            ← 조문시행일자 (Integer 변환)
  prevJoKey: Integer                // 이전 조문 키           ← 조문이전키
  nextJoKey: Integer                // 다음 조문 키           ← 조문다음키
  joRef: String                     // 참고 자료              ← 조문참고자료
  titleChanged: String              // 제목 변경 여부         ← 조문제목변경여부
  isHangul: String                  // 한글 여부              ← 한글여부
}
```

### Hang (항)

```
Hang {                              // ← 조문내용.항
  항번호: String                     // 항 번호                ← 항번호
  항가지번호: String                 // 항 가지번호            ← 항가지번호
  항내용: String                     // 항 본문                ← 항내용
  호: List<Ho>                      // 호 목록                ← 호
  항제개정유형: String               // 제개정 유형            ← 항제개정유형
  항제개정일자: String               // 제개정 일자            ← 항제개정일자
  항제개정일자문자열: String          // 제개정 일자 (문자열)    ← 항제개정일자문자열
}
```

### Ho (호)

```
Ho {                                // ← 항.호
  호번호: String                     // 호 번호                ← 호번호
  호가지번호: String                 // 호 가지번호            ← 호가지번호
  호내용: String                     // 호 본문                ← 호내용
  목: List<Mok>                     // 목 목록                ← 목
}
```

### Mok (목)

```
Mok {                               // ← 호.목
  목번호: String                     // 목 번호                ← 목번호
  목가지번호: String                 // 목 가지번호            ← 목가지번호
  목내용: String                     // 목 본문                ← 목내용
}
```

### PrecedentDto (판례)

```
PrecedentDto {
  precId: Integer                   // 판례 일련번호 (PK)      ← 판례일련번호 / 판례정보일련번호
  caseNumber: String                // 사건 번호              ← 사건번호
  caseName: String                  // 사건명                 ← 사건명
  caseTypeName: String              // 사건 종류명            ← 사건종류명
  caseTypeCode: String              // 사건 종류 코드          ← 사건종류코드
  courtName: String                 // 법원명                 ← 법원명
  courtCode: String                 // 법원 코드              ← 법원종류코드
  decisionDate: Integer             // 선고일 (YYYYMMDD)      ← 선고일자
  declaration: String               // 선고 내용              ← 선고
  decisionType: String              // 판결 유형              ← 판결유형
  decisionSummary: String           // 판결 요지              ← 판결요지 (Content only)
  summary: String                   // 판시 사항              ← 판시사항 (Content only)
  content: String                   // 판례 전문              ← 판례내용 (Content only)
  dataSource: String                // 데이터 출처            ← 데이터출처명 (List only)
  articleReferences: List<ArticleReferenceDto>    // 참조 조문  ← 참조조문 (파싱)
  precedentReferences: List<PrecedentReferenceDto> // 참조 판례 ← 참조판례 (파싱)
}
```

### ArticleReferenceDto (참조 조문)

`참조조문` 문자열을 파싱하여 생성됩니다.

```
ArticleReferenceDto {
  rawText: String                   // 원본 텍스트 (예: "민법 제750조")
  textIndex: int                    // 원문 인덱스 (쉼표 기준)
  lawName: String                   // 법령명 (예: "민법")
  joKey: Integer                    // 조문키 (joNum * 100 + joBrNum)
  referenceDate: Integer            // 참조 기준 날짜 (YYYYMMDD)
  isOlderThan: boolean              // true: referenceDate 이전, false: 포함
}
```

### PrecedentReferenceDto (참조 판례)

`참조판례` 문자열을 파싱하여 생성됩니다.

```
PrecedentReferenceDto {
  rawText: String                   // 원본 텍스트 (예: "대법원 2020. 1. 1. 선고 2019다12345 판결")
  textIndex: int                    // 원문 인덱스
  courtName: String                 // 법원명 (예: "대법원")
  caseNumber: String                // 사건번호 (예: "2019다12345")
  decisionDate: Integer             // 선고일 (YYYYMMDD)
}
```

### unexpectedFieldMap

모든 DTO에는 `unexpectedFieldMap: Map<String, String>` 필드가 있습니다.
API 응답에 포함되었지만 DTO에 정의되지 않은 필드들이 이 Map에 저장됩니다.

```java
StatuteDto statute = result.statute().orElseThrow();
Map<String, String> unexpected = statute.getUnexpectedFieldMap();
if (unexpected != null && !unexpected.isEmpty()) {
    log.warn("Unexpected fields: {}", unexpected);
}
```

---

## 공통 사항

### 응답 구조

모든 API 응답은 다음 구조를 따릅니다:

```java
// 목록 조회 결과
record ListApiResult(
    String rawData,         // 원본 JSON 문자열
    List<SomeDto> items,    // 파싱된 DTO 목록
    int totalCount          // 전체 건수
) {}

// 본문 조회 결과
record ContentApiResult(
    String rawData,              // 원본 JSON 문자열
    Optional<SomeDto> content    // 파싱된 DTO (실패 시 empty)
) {}
```

### 파라미터 기본값 처리

- 파라미터를 설정하지 않으면(null) API 측 기본값이 적용됩니다
- 각 파라미터의 API 기본값은 Javadoc 주석에 명시되어 있습니다
- 예: `page`의 API 기본값은 1, `display`의 API 기본값은 20

### ID 자동 변환

숫자 형태의 ID 파라미터는 자동으로 zero-padding 처리됩니다:

```java
// lawId: 1233 → "001233" (6자리)
// organizationCode: 1371000 → "1371000" (7자리)
// articleNumber: 400 → "000400" (6자리)
```

### 날짜 형식

날짜 파라미터는 YYYYMMDD 형태의 Integer로 입력합니다:

```java
.promulgationDateFrom(20240101)  // 2024년 1월 1일
.promulgationDateTo(20241231)    // 2024년 12월 31일
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

- [법제처 Open API 안내](https://www.law.go.kr/LSO/openApi.do)
- [법제처 Open API 명세](https://www.law.go.kr/LSO/openApiGuide.do)
