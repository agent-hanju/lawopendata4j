# 금융규제·법령해석포털 API 명세서

이 수집 프로젝트에서 사용하는 금융위원회 금융규제·법령해석포털(better.fsc.go.kr)의 비조치의견서 및 법령해석 관련 Request/Response 명세

---

## 개요

**기관**: 금융위원회
**포털명**: 금융규제·법령해석포털
**도메인**: `https://better.fsc.go.kr`
**수집 대상**: 비조치의견서(1,653건), 법령해석요청(2,442건) - 2025-01 기준
**특징**:
- DataTables 기반 서버사이드 페이징
- AJAX API로 목록 조회 (JSON 응답)
- 상세 페이지는 서버사이드 렌더링 (HTML 파싱 필요)
- 세션/쿠키 불필요 (퍼블릭 API)

---

## 1. 비조치의견서 (OpinionList)

### 1.1 목록 조회 API

**Endpoint**: `https://better.fsc.go.kr/fsc_new/replyCase/selectReplyCaseOpinionList.do`

**Method**: `POST`

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명 | 예시 |
| -------- | ------ | ---- | ---- | ---- |
| `start` | number | O | 시작 오프셋 (0-based) | `0`, `10`, `20` |
| `length` | number | O | 페이지당 항목 수 (최대 100 권장) | `10`, `100` |
| `draw` | number | O | DataTables 요청 식별자 | `1` |
| `searchKeyword` | string | X | 검색 키워드 | - |
| `searchCondition` | string | X | 검색 조건 | - |
| `searchStatus` | string | X | 처리 상태 필터 | `완료` |
| `searchCategory` | string | X | 카테고리 필터 | `전자금융` |
| `searchReplyRegDateStart` | string | X | 등록일 시작 (YYYY-MM-DD) | `2025-01-01` |
| `searchReplyRegDateEnd` | string | X | 등록일 종료 (YYYY-MM-DD) | `2025-12-31` |

**페이징 방식**:
- `start=0, length=10`: 1페이지 (1-10번)
- `start=10, length=10`: 2페이지 (11-20번)
- `start=100, length=100`: 2페이지 (101-200번)

#### Response

```typescript
interface OpinionListResponse {
  recordsTotal: number;       // 전체 레코드 수 (1653)
  recordsFiltered: number;    // 필터된 레코드 수
  data: OpinionListItem[];
}

interface OpinionListItem {
  rownumber: number;          // 역순 번호 (최신이 높은 번호)
  opinionIdx: number;         // 비조치의견서 일련번호 (PK, 상세 조회 시 필요)
  category: string;           // 카테고리 (전자금융, 공통, 보험, 은행 등)
  title: string;              // 제목
  opinionNumber: string;      // 문서번호 (예: "250048")
  status: string;             // 처리 상태 (완료)
  dpNm: string;               // 부서명 (대부분 빈 문자열)
}
```

**응답 예시**:
```json
{
  "recordsTotal": 1653,
  "recordsFiltered": 1653,
  "data": [
    {
      "rownumber": 1653,
      "opinionIdx": 2289,
      "category": "전자금융",
      "title": "지자체 세출e뱅킹 시스템 인프라 구성 관련 망분리 예외 해당 여부",
      "opinionNumber": "250048",
      "status": "완료",
      "dpNm": ""
    },
    {
      "rownumber": 1652,
      "opinionIdx": 2286,
      "category": "공통",
      "title": "연체이력 정보 공유 제한을 통한 서민·소상공인 신용회복지원 방안 관련 비조치의견서 직권발급",
      "opinionNumber": "250046",
      "status": "완료",
      "dpNm": ""
    }
  ]
}
```

---

### 1.2 상세 페이지 조회

**Endpoint**: `https://better.fsc.go.kr/fsc_new/replyCase/OpinionDetail.do`

**Method**: `POST` (또는 `GET`)

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명 | 예시 |
| -------- | ------ | ---- | ---- | ---- |
| `opinionIdx` | number | O | 비조치의견서 일련번호 | `2289` |
| `muNo` | number | O | 메뉴 번호 | `86` |
| `stNo` | number | O | 사이트 번호 | `11` |
| `actCd` | string | O | 액션 코드 | `R` (읽기) |

#### Response

**응답 형식**: HTML (서버사이드 렌더링)

**파싱 대상**:
- **제목**: 페이지 제목 영역
- **처리구분**: 회신 등
- **소관부서**: 금융위원회, 금융감독원 등
- **회신일**: YYYY-MM-DD 형식
- **첨부파일**: `a[href*="displayFile.do"]` 셀렉터로 추출
- **회신문구**: 본문 내용

**첨부파일 다운로드 URL 형식**:
```
/fsc_new/file/displayFile.do?filePath={경로}&orgFileName={원본파일명}&sysFileName={시스템파일명}
```

**예시**:
```
https://better.fsc.go.kr/fsc_new/file/displayFile.do?filePath=/2025/11/13&orgFileName=비조치의견서 요청에 대한 회신(지자체 세출e뱅킹시스템 인프라 구성 관련 '망분리 적용 예외사항' 해당여부).hwp&sysFileName=dea892569b9745cfad4c1ba39f9c162e.hwp
```

---

## 2. 법령해석요청 (LawreqList)

### 2.1 목록 조회 API

**Endpoint**: `https://better.fsc.go.kr/fsc_new/replyCase/selectReplyCaseLawreqList.do`

**Method**: `POST`

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명 | 예시 |
| -------- | ------ | ---- | ---- | ---- |
| `start` | number | O | 시작 오프셋 (0-based) | `0`, `10`, `20` |
| `length` | number | O | 페이지당 항목 수 (최대 100 권장) | `10`, `100` |
| `draw` | number | O | DataTables 요청 식별자 | `1` |
| `searchKeyword` | string | X | 검색 키워드 | - |
| `searchCondition` | string | X | 검색 조건 | - |
| `searchStatus` | string | X | 처리 상태 필터 | `완료` |
| `searchCategory` | string | X | 카테고리 필터 | `보험` |
| `searchReplyRegDateStart` | string | X | 등록일 시작 (YYYY-MM-DD) | `2025-01-01` |
| `searchReplyRegDateEnd` | string | X | 등록일 종료 (YYYY-MM-DD) | `2025-12-31` |

#### Response

```typescript
interface LawreqListResponse {
  recordsTotal: number;       // 전체 레코드 수 (2442)
  recordsFiltered: number;    // 필터된 레코드 수
  data: LawreqListItem[];
}

interface LawreqListItem {
  rownumber: number;          // 역순 번호 (최신이 높은 번호)
  lawreqIdx: number;          // 법령해석 일련번호 (PK, 상세 조회 시 필요)
  category: string;           // 카테고리 (보험, 은행 등, 빈 문자열도 있음)
  title: string;              // 제목
  lawreqNumber: string;       // 문서번호 (예: "250207")
  status: string;             // 처리 상태 (완료)
  dpNm: string;               // 부서명 (숫자 또는 빈 문자열)
}
```

**응답 예시**:
```json
{
  "recordsTotal": 2442,
  "recordsFiltered": 2442,
  "data": [
    {
      "rownumber": 2442,
      "lawreqIdx": 5241,
      "category": "",
      "title": "새도약기금 관련 양수인 평가 생략 가능 여부(수기 건 일련번호 259002)",
      "lawreqNumber": "250207",
      "status": "완료",
      "dpNm": "7"
    },
    {
      "rownumber": 2440,
      "lawreqIdx": 5204,
      "category": "보험",
      "title": "PF대출계약의 제3자 연대보증 관련하여, 금융소비자보호법의 적용 여부 및 해석에 대한 유권해석을 요청 드리려 합니다.",
      "lawreqNumber": "250176",
      "status": "완료",
      "dpNm": "7"
    }
  ]
}
```

---

### 2.2 상세 페이지 조회

**Endpoint**: `https://better.fsc.go.kr/fsc_new/replyCase/LawreqDetail.do`

**Method**: `POST` (또는 `GET`)

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명 | 예시 |
| -------- | ------ | ---- | ---- | ---- |
| `lawreqIdx` | number | O | 법령해석 일련번호 | `5241` |
| `muNo` | number | O | 메뉴 번호 | `85` |
| `stNo` | number | O | 사이트 번호 | `11` |
| `actCd` | string | O | 액션 코드 | `R` (읽기) |

#### Response

**응답 형식**: HTML (서버사이드 렌더링)

**파싱 대상**:
- **제목**: 페이지 제목 영역
- **처리구분**: 회신 등
- **공개여부**: Y/N
- **등록자**: 담당자명
- **회신일**: YYYY-MM-DD 형식
- **첨부파일**: `a[href*="displayFile.do"]` 셀렉터로 추출
- **질의문구**: 질의 내용
- **회신문구**: 회신 내용

---

## 3. 공통 사항

### 3.1 인증 및 헤더

**필수 헤더**:
```
Content-Type: application/x-www-form-urlencoded
Accept: application/json
```

**선택 헤더** (권장):
```
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
X-Requested-With: XMLHttpRequest
```

**인증**: 불필요 (퍼블릭 API)

---

### 3.2 페이징 전략

**전체 데이터 수집 알고리즘**:
```java
// 1단계: 첫 요청으로 totalRecords 확인
Response firstPage = callAPI(start=0, length=100);
int totalRecords = firstPage.recordsTotal;

// 2단계: 페이징 순회
int pageSize = 100; // 한 번에 100개씩
for (int start = 0; start < totalRecords; start += pageSize) {
    Response response = callAPI(start, pageSize);
    for (Item item : response.data) {
        // 상세 페이지 조회
        String html = callDetailAPI(item.opinionIdx or item.lawreqIdx);

        // HTML 파싱 (Jsoup 사용)
        Document doc = Jsoup.parse(html);
        String attachmentUrl = doc.select("a[href*=displayFile.do]").attr("href");

        // 첨부파일 다운로드
        downloadFile("https://better.fsc.go.kr" + attachmentUrl);
    }
}
```

---

### 3.3 에러 처리

**HTTP 상태 코드**:
- `200`: 성공
- `404`: 페이지 없음
- `500`: 서버 오류

**주의사항**:
- 서버 응답이 매우 느릴 수 있음 (30-60초 타임아웃 권장)
- 상세 페이지는 HTML 파싱 필요
- 첨부파일이 없는 게시글도 존재
- `category`와 `dpNm` 필드는 빈 문자열일 수 있음

---

### 3.4 데이터 구조 권장사항

**Entity 설계**:
```java
@Entity
@Table(name = "fsc_opinion")
public class FscOpinion {
    @Id
    private Long opinionIdx;  // API의 opinionIdx

    private Integer rownumber;
    private String category;
    private String title;
    private String opinionNumber;  // 문서번호
    private String status;

    private String department;  // 파싱: 소관부서
    private LocalDate replyDate;  // 파싱: 회신일
    private String replyContent;  // 파싱: 회신문구

    @OneToMany(mappedBy = "opinion")
    private List<FscOpinionAttachment> attachments;
}

@Entity
@Table(name = "fsc_opinion_attachment")
public class FscOpinionAttachment {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private FscOpinion opinion;

    private String orgFileName;
    private String sysFileName;
    private String filePath;
    private String downloadUrl;

    private String localFilePath;  // 다운로드 후 로컬 저장 경로
}
```

---

## 4. 테스트 curl 예시

### 4.1 비조치의견서 목록 조회

```bash
curl -X POST 'https://better.fsc.go.kr/fsc_new/replyCase/selectReplyCaseOpinionList.do' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H 'Accept: application/json' \
  -d 'start=0&length=10&draw=1'
```

### 4.2 비조치의견서 상세 조회

```bash
curl -X POST 'https://better.fsc.go.kr/fsc_new/replyCase/OpinionDetail.do' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'muNo=86&stNo=11&opinionIdx=2289&actCd=R'
```

### 4.3 법령해석요청 목록 조회

```bash
curl -X POST 'https://better.fsc.go.kr/fsc_new/replyCase/selectReplyCaseLawreqList.do' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H 'Accept: application/json' \
  -d 'start=0&length=10&draw=1'
```

### 4.4 법령해석요청 상세 조회

```bash
curl -X POST 'https://better.fsc.go.kr/fsc_new/replyCase/LawreqDetail.do' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'muNo=85&stNo=11&lawreqIdx=5241&actCd=R'
```

### 4.5 첨부파일 다운로드

```bash
curl -O 'https://better.fsc.go.kr/fsc_new/file/displayFile.do?filePath=/2025/11/13&orgFileName=비조치의견서.hwp&sysFileName=dea892569b9745cfad4c1ba39f9c162e.hwp'
```

---

## 5. Spring Boot 구현 예시

### 5.1 WebClient 설정

```java
@Configuration
public class FscWebClientConfig {

    @Bean
    public WebClient fscWebClient() {
        return WebClient.builder()
            .baseUrl("https://better.fsc.go.kr")
            .defaultHeader(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT,
                MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)) // 16MB
            .build();
    }
}
```

### 5.2 Service 구현

```java
@Service
@RequiredArgsConstructor
public class FscOpinionCollectorService {

    private final WebClient fscWebClient;

    public OpinionListResponse fetchOpinionList(int start, int length) {
        return fscWebClient.post()
            .uri("/fsc_new/replyCase/selectReplyCaseOpinionList.do")
            .bodyValue("start=" + start + "&length=" + length + "&draw=1")
            .retrieve()
            .bodyToMono(OpinionListResponse.class)
            .block();
    }

    public String fetchOpinionDetail(long opinionIdx) {
        return fscWebClient.post()
            .uri("/fsc_new/replyCase/OpinionDetail.do")
            .bodyValue("muNo=86&stNo=11&opinionIdx=" + opinionIdx + "&actCd=R")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    public byte[] downloadAttachment(String fileUrl) {
        return fscWebClient.get()
            .uri(fileUrl)
            .retrieve()
            .bodyToMono(byte[].class)
            .block();
    }
}
```

---

## 6. 주의사항 및 제약사항

1. **응답 속도**: 서버 응답이 매우 느림 (30-120초 소요 가능)
2. **타임아웃 설정**: WebClient/RestTemplate 타임아웃을 최소 60초로 설정
3. **HTML 파싱**: 상세 페이지는 Jsoup 등으로 파싱 필요
4. **첨부파일**: 모든 게시글에 첨부파일이 있는 것은 아님
5. **인코딩**: 파일명에 한글이 포함되어 URL 인코딩 필요
6. **Rate Limiting**: 명시적 제한은 없으나 과도한 요청 시 차단 가능성
7. **데이터 일관성**: `category`, `dpNm` 등 일부 필드는 빈 값일 수 있음
8. **페이징 최대값**: `length` 파라미터의 실제 최대값은 미확인 (100 권장)

---

## 7. 수집 상태 (2025-01 기준)

| 분류 | 총 건수 | 상태 | 비고 |
|------|---------|------|------|
| 비조치의견서 | 1,653건 | 미수집 | - |
| 법령해석요청 | 2,442건 | 미수집 | - |
| **합계** | **4,095건** | **미수집** | - |

---

## 8. 참고 링크

- **비조치의견서**: https://better.fsc.go.kr/fsc_new/replyCase/OpinionList.do?stNo=11&muNo=86&muGpNo=75
- **법령해석요청**: https://better.fsc.go.kr/fsc_new/replyCase/LawreqList.do?stNo=11&muNo=85&muGpNo=75
- **금융규제·법령해석포털**: https://better.fsc.go.kr
