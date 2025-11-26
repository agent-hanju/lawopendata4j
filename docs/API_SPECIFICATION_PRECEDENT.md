# 국가법령정보 공동활용 Open API 판례 명세서

이 수집 프로젝트에서 사용하는 국가법령정보 공동활용 Open API(open.law.go.kr)의 판례 관련 Request/Response 명세

## 판례 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                  | 예시                |
| --------- | ------ | ---- | ------------------------------------- | ------------------- |
| `OC`      | string | O    | API 인증키                            | -                   |
| `target`  | string | O    | 조회 대상                             | `prec`              |
| `type`    | string | X    | 응답 형식(기본 XML)                   | `JSON`              |
| `display` | number | X    | 페이지당 표시 건수(기본 20, 최대 100) | `100`               |
| `prncYd`  | string | X    | 선고일자 범위 (YYYYMMDD~YYYYMMDD)     | `20200101~20201231` |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)         | `1`                 |

### Response

```typescript
interface PrecedentListResponse {
  totalCnt: string;
  page: string;
  prec: PrecedentListItem | PrecedentListItem[];
}

interface PrecedentListItem {
  판례일련번호: string;
  사건명: string;
  사건번호: string;
  선고일자: string; // YYYYMMDD
  법원명: string;
  법원종류코드: string;
  사건종류명: string;
  사건종류코드: string;
  판결유형: string;
  선고: string;
  데이터출처명: string;
}
```

---

## 판례 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명         | 예시     |
| -------- | ------ | ---- | ------------ | -------- |
| `OC`     | string | O    | API 인증키   | -        |
| `target` | string | O    | 조회 대상    | `prec`   |
| `type`   | string | O    | 응답 형식    | `JSON`   |
| `ID`     | string | O    | 판례일련번호 | `607527` |

### Response

```typescript
interface PrecedentContentResponse {
  판례일련번호: string;
  사건번호: string;
  사건명: string;
  판시사항: string;
  판결요지: string;
  판례내용: string;
  사건종류코드: string;
  사건종류명: string;
  법원종류코드: string;
  법원명: string;
  판결유형: string;
  선고: string;
  선고일자: string; // YYYYMMDD
  참조판례: string; // 콤마로 구분
  참조조문: string; // 콤마로 구분
}
```
