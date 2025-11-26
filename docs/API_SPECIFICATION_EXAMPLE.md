## 헌재결정례 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                  | 예시                                    |
| --------- | ------ | ---- | ------------------------------------- | --------------------------------------- |
| `OC`      | string | O    | API 인증키                            | -                                       |
| `target`  | string | O    | 조회 대상                             | `detc`                                  |
| `type`    | string | X    | 응답 형식(기본 XML)                   | `JSON`                                  |
| `display` | number | X    | 페이지당 표시 건수(기본 20, 최대 100) | `100`                                   |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)         | `1`                                     |
| `edYd`    | string | X    | 종국일자 범위 (YYYYMMDD~YYYYMMDD)     | `20200101~20201231`                     |
| `sort`    | string | X    | 정렬순서                              | `efasc` (오름차순) / `efdes` (내림차순) |

**sort 옵션** (종국일자 기준):

- `efasc`: 종국일자 오름차순 (오래된 것부터, reverse=false)
- `efdes`: 종국일자 내림차순 (최신 것부터, reverse=true, 기본값)

### Response

```typescript
interface ConstitutionalDecisionListResponse {
  totalCnt: string;
  page: string;
  Detc: ConstitutionalDecisionListItem | ConstitutionalDecisionListItem[];
}

interface ConstitutionalDecisionListItem {
  헌재결정례일련번호: string;
  사건명: string;
  사건번호: string;
  종국일자: string; // YYYYMMDD or "0" (미종결)
  사건종류명: string;
  사건종류코드: string;
  헌재결정례상세링크: string;
}
```

---

## 헌재결정례 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명               | 예시    |
| -------- | ------ | ---- | ------------------ | ------- |
| `OC`     | string | O    | API 인증키         | -       |
| `target` | string | O    | 조회 대상          | `detc`  |
| `type`   | string | O    | 응답 형식          | `JSON`  |
| `ID`     | string | O    | 헌재결정례일련번호 | `58400` |

### Response

```typescript
interface ConstitutionalDecisionContentResponse {
  헌재결정례일련번호: string;
  사건번호: string;
  사건명: string;
  종국일자: string; // YYYYMMDD
  사건종류명: string;
  사건종류코드: string;
  재판부구분코드: string;
  판시사항: string; // 실제로는 "사건 법률조항" + 판시사항 혼합
  참조조문: string;
  참조판례: string;
  // 주의: 응답 필드가 불규칙함 (rawJson 저장 필수)
}
```

---

## 법령해석례 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                  | 예시                                  |
| --------- | ------ | ---- | ------------------------------------- | ------------------------------------- |
| `OC`      | string | O    | API 인증키                            | -                                     |
| `target`  | string | O    | 조회 대상                             | `expc`                                |
| `type`    | string | X    | 응답 형식(기본 XML)                   | `JSON`                                |
| `display` | number | X    | 페이지당 표시 건수(기본 20, 최대 100) | `100`                                 |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)         | `1`                                   |
| `regYd`   | string | X    | 등록일자 범위 (YYYYMMDD~YYYYMMDD)     | `20200101~20201231`                   |
| `sort`    | string | X    | 정렬순서                              | `dasc` (오름차순) / `ddes` (내림차순) |

**sort 옵션** (등록일자 기준):

- `dasc`: 회신일자 오름차순 (오래된 것부터, reverse=false)
- `ddes`: 회신일자 내림차순 (최신 것부터, reverse=true, 기본값)

### Response

```typescript
interface LegalInterpretationListResponse {
  totalCnt: string;
  page: string;
  expc: LegalInterpretationListItem | LegalInterpretationListItem[];
}

interface LegalInterpretationListItem {
  법령해석례일련번호: string;
  안건명: string;
  안건번호: string;
  회신일자: string; // YYYYMMDD or "" (미회신)
  회신기관명: string;
  회신기관코드: string;
  질의기관명: string;
  질의기관코드: string;
  법령해석례상세링크: string;
}
```

---

## 법령해석례 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명               | 예시     |
| -------- | ------ | ---- | ------------------ | -------- |
| `OC`     | string | O    | API 인증키         | -        |
| `target` | string | O    | 조회 대상          | `expc`   |
| `type`   | string | O    | 응답 형식          | `JSON`   |
| `ID`     | string | O    | 법령해석례일련번호 | `330471` |

### Response

```typescript
interface LegalInterpretationContentResponse {
  법령해석례일련번호: string;
  안건명: string;
  안건번호: string;
  회신일자: string; // YYYYMMDD
  회신기관명: string;
  회신기관코드: string; // 해석기관코드
  질의기관명: string;
  질의기관코드: string;
  등록일시: string; // YYYYMMDD
  회답: string; // 회신 내용 (요약)
  해석: string; // 전문 내용 (매우 긴 텍스트)
}
```

---

## 행정심판례 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                  | 예시                                  |
| --------- | ------ | ---- | ------------------------------------- | ------------------------------------- |
| `OC`      | string | O    | API 인증키                            | -                                     |
| `target`  | string | O    | 조회 대상                             | `decc`                                |
| `type`    | string | X    | 응답 형식(기본 XML)                   | `JSON`                                |
| `display` | number | X    | 페이지당 표시 건수(기본 20, 최대 100) | `100`                                 |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)         | `1`                                   |
| `rslYd`   | string | X    | 의결일자 범위 (YYYYMMDD~YYYYMMDD)     | `20200101~20201231`                   |
| `sort`    | string | X    | 정렬순서                              | `dasc` (오름차순) / `ddes` (내림차순) |

**sort 옵션** (의결일자 기준):

- `dasc`: 의결일자 오름차순 (오래된 것부터, reverse=false)
- `ddes`: 의결일자 내림차순 (최신 것부터, reverse=true, 기본값)

### Response

```typescript
interface AdministrativeAppealListResponse {
  totalCnt: string;
  page: string;
  decc: AdministrativeAppealListItem | AdministrativeAppealListItem[];
}

interface AdministrativeAppealListItem {
  행정심판재결례일련번호: string;
  사건명: string;
  사건번호: string;
  의결일자: string; // YYYYMMDD or "" (미의결)
  처분일자: string; // YYYYMMDD or ""
  재결청: string;
  처분청: string;
  재결구분코드: string;
  재결구분명: string;
  행정심판례상세링크: string;
}
```

---

## 행정심판례 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                   | 예시     |
| -------- | ------ | ---- | ---------------------- | -------- |
| `OC`     | string | O    | API 인증키             | -        |
| `target` | string | O    | 조회 대상              | `decc`   |
| `type`   | string | O    | 응답 형식              | `JSON`   |
| `ID`     | string | O    | 행정심판재결례일련번호 | `223311` |

### Response

```typescript
interface AdministrativeAppealContentResponse {
  행정심판례일련번호: string;
  사건명: string;
  사건번호: string;
  재결례유형코드: string;
  재결례유형명: string;
  재결청: string;
  의결일자: string; // YYYYMMDD
  처분청: string;
  처분일자: string; // YYYYMMDD or ""
  청구취지: string;
  재결요지: string;
  주문: string;
  이유: string;
}
```

#### example

```json
{
  "PrecService": {
    "행정심판례일련번호": "223311",
    "사건명": "국가유공자등록거부처분취소청구",
    "사건번호": "2001-07502",
    "재결례유형코드": "",
    "재결례유형명": "",
    "재결청": "국민권익위원회",
    "의결일자": "20010925",
    "처분청": "",
    "처분일자": "",
    "청구취지": "피청구인이 2001. 5. 26. 청구인에 대하여 한 국가유공자등록거부처분은 이를 취소한다.",
    "재결요지": "사 건 01-07502 국가유공자등록거부처분취소청구청 구 인 이 ○ ○부산광역시 ○○구 ○○동 189-1141번지피청구인 부산지방보훈청장청구인이 2001. 8. 4. 제기한 심판청구에 대하여 2001년도 제34회 국무총리행정심판위원회는 주문과 같이 의결한다.",
    "주문": "청구인의 청구를 기각한다.",
    "이유": "1. 사건개요청구인은 1952. 3. 20. 육군에 입대하여 ○○사단 소속으로 복무중이던 1954. 5.경 이동중 차량 전복사고로 좌측 팔목에 부상을 입고 군병원에서 입원 치료 후 1956. 11. 20. 만기 제대하였다는 이유로 2000. 6. 27. 국가유공자등록신청을 하였으나, 피청구인은 청구인의 진술외에 부상사실을 입증할 수 있는 병상일지 등 객관적인 자료가 없어 군 공무수행과의 인과관계를 인정하기 곤란하다는 이유로 2001. 5. 26. 청구인에 대하여 국가유공자등록거부처분(이하 “이 건 처분”이라 한다)을 하였다.2. 청구인 주장이에 대하여 청구인은, 1952. 3. 20. 육군에 입대하여 ○○사단 소속으로 복무중이던 1954년 3월 또는 4월경 ○○사단 전체가 강원도 ○○에서 경기도 ○○로 이동하였는데 경기도 △△에 있는 작은 다리를 건너다가 청구인이 타고 있던 차량이 전복되어 좌측 팔이 부러지고 팔목이 탈골되는 부상을 입고 정신을 잃었는 바, 사고 당시 청구인은 15일간 적절한 치료조차 받지 못하고 의무실에서 단지 소독제와 진통제만을 맞으며 방치된 채로 고통을 당한 점, 청구인의 상태가 악화되자 민간병원을 경유하여 ○○육군병원으로 후송되어 깁스를 하였는데 깁스를 풀어보니 손목을 펼 수가 없어 통증을 호소하였으나 물리치료만 받게 한 후 팔목도 움직이지 못하는 상태에서 강원도 △△에 있는 보충대로 보내졌다가 다시 이틀만에 최전방인 ○○사단으로 전속된 점, 청구인은 이후 다시 예비사단인△△사단에서 복무하다가 1956. 11. 20. 만기전역한 점, 청구인이 전역 후 병원과 한의원에서 치료를 하여 굽은 팔목은 많이 나아졌지만 좌측 팔목은 기형과 통증으로 고통을 받고 있는 점, 병상일지 등 관련기록이 없는 것은 국가의 책임인 점 등을 고려할 때, 이 건 처분은 위법ㆍ부당하여 취소되어야 한다고 주장한다.3. 이 건 처분의 위법ㆍ부당여부가. 관계법령국가유공자등예우및지원에관한법률 제4조제1항제6호, 제6조 및 제83조제1항동법시행령 제3조의2, 제8조, 제9조, 제9조의2, 제102조제1항 및 별표 1나. 판 단(1) 청구인 및 피청구인이 제출한 자료조회결과통보, 거주표, 국가유공자등요건관련사실확인서, 진단서, 병적증명서, 심의의결서, 국가유공자비해당결정통지 등 각 사본의 기재내용에 의하면, 다음과 같은 사실을 각각 인정할 수 있다.(가) 병적증명서에 의하면, 청구인은 1952. 3. 20. 육군에 입대하여 1956. 11. 20. 만기전역한 것으로 기재되어 있다.(나) 육군참모총장의 2000. 12. 15.자 국가유공자등요건관련사실확인서에 의하면, 청구인의 상이원인은 “근무중”으로, 상이연월일은 “1953. 5.”로, 현상병명은 “1)좌 원위부 요골 골간관절 아탈구 후유증, 2)퇴행성 관절염”으로 각각 기재되어 있으며, 원상병명은 공란으로 되어 있다.(다) 육군중앙문서관리단장이 2001. 7. 20. 청구인에게 발송한 자료조회 결과통보에 의하면, 청구인의 거주표상 입원사실은 확인되나 청구인의 병상일지는 보관되어 있지 아니하다는 내용이 기재되어 있다.(라) 청구인의 거주표에는 청구인이 1954. 6. 16. ○○육군병원에 입원하였으며, 1954. 7. 13. 제○○정양원에 입원하였다는 내용이 기재되어 있다.(마) 보훈심사위원회는 2001. 4. 27., 청구인이 군복무중 차량전복사고로 좌측 팔목에 부상을 입었다고 진술하고 거주표상 입원기록은 확인되나, 육군본부에서 부상경위 및 병명확인이 불가능하다는 사유로 원상병명을 통보하지 아니한 점, 청구인의 진술외에 부상사실을 입증할 수 있는 병상일지 등 구체적이고 객관적인 자료가 없어 부상경위 및 병명확인이 불가능한 점 등으로 보아 청구인의 현상병명과 공무수행과의 인과관계를 인정하기 곤란하다는 이유로 청구인을 국가유공자등예우및지원에관한법률 소정의 공상군경 요건에 해당하지 아니하는 자로 의결하였고, 이에 피청구인은 2001. 5. 26. 청구인에 대하여 이 건 처분을 하였다.(바) 부산광역시 ○○구에 소재한 ○○병원에서 발행한 2000. 6. 27.자 진단서에 의하면, 청구인의 병명은 “1. 좌 원위부 요골 골간관절 아탈구 후유증, 2. 퇴행성 관절염”으로 기재되어 있고, 향후치료의견으로는 완관절 동통과 운동시 불편이 있을 것으로 사료되고 수술적 가료가 필요할지도 모른다는 내용이 기재되어 있다.(2) 살피건대, 청구인은 군복무시 차량전복사고로 좌측 팔목에 부상을 입었다고 주장하나, 청구인의 부상경위 또는 부상부위 등에 대한 기록이나 병상일지 등의 객관적인 자료가 없어 청구인의 상이가 군복무로 인하여 발생한 것인 지의 여부를 확인할 수 없는 상태에서 청구인의 주장과 입원기록만으로는 청구인의 상이를 공상으로 인정하기 어렵다 할 것이므로, 피청구인의 이 건 처분이 위법ㆍ부당하다고 할 수 없을 것이다.4. 결 론그렇다면, 청구인의 청구는 이유없다고 인정되므로 이를 기각하기로 하여 주문과 같이 의결한다."
  }
}
```
