# 국가법령정보 공동활용 Open API 법령용어 명세서

이 수집 프로젝트에서 사용하는 국가법령정보 공동활용 Open API(open.law.go.kr)의 키워드/법령용어 관련 Request/Response 명세

---

## 키워드/법령용어 API

### 법령용어 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

#### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                   | 예시    |
| --------- | ------ | ---- | -------------------------------------- | ------- |
| `OC`      | string | O    | API 인증키                             | -       |
| `target`  | string | O    | 조회 대상                              | `lstrm` |
| `type`    | string | X    | 응답 형식 (기본 XML)                   | `JSON`  |
| `display` | number | X    | 페이지당 표시 건수 (기본 20, 최대 100) | `100`   |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)          | `1`     |
| `sort`    | string | X    | 정렬 방식                              | `rasc`  |

**sort 옵션**:

- `rasc`: 법령용어명 오름차순 (ㄱ-ㅎ)
- `rdes`: 법령용어명 내림차순 (ㅎ-ㄱ)

#### Response

```typescript
interface LawTermListResponse {
  LsTrmSearch: {
    resultMsg: 'success';
    resultCode: '00';
    키워드: string; // "*" (전체 조회 시)
    page: string; // number
    target: 'lstrm';
    totalCnt: string; // number (총 72,926개)
    section: 'tn_lstrm_list';
    numOfRows: string; // number
    lstrm: LawTermListItem | LawTermListItem[];
  };
}

interface LawTermListItem {
  id: string; // 무시 (페이지 내 순서)
  법령용어ID: string; // 법령용어 일련번호 (PK로 사용)
  법령용어명: string;
  법령종류코드: string; // "010101"
  사전구분코드: string; // "011403" (법령한영사전)
  법령용어상세링크: string; // "/DRF/lawService.do?OC=dykim2098&target=lstrm&trmSeqs=13396&mobile=&type=XML"
  법령용어상세검색: string; // "/LSW/lsTrmInfoR.do?trmSeqs=13396&mobile="
}
```

**사전구분코드**:

- `011401`: 법령용어사전
- `011402`: 법령정의사전
- `011403`: 법령한영사전

**주의사항**:

- 총 72,926개의 법령용어 존재 (2025-01 기준)
- 페이징 처리 필수 (display 최대 100)
- `법령용어ID` = `trmSeqs` = 법령용어 일련번호 (Entity PK로 사용)

---

### 조문-법령용어 연계 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                                         | 예시         |
| -------- | ------ | ---- | -------------------------------------------- | ------------ |
| `OC`     | string | O    | API 인증키                                   | -            |
| `target` | string | O    | 조회 대상                                    | `joRltLstrm` |
| `type`   | string | X    | 응답 형식 (기본 XML)                         | `JSON`       |
| `ID`     | string | X    | 법령 ID                                      | `1233`       |
| `JO`     | string | X    | 조번호 (000400 = 4조)                        | `000400`     |
| `query`  | string | X    | 법령용어 검색어 (MST 또는 ID 중 하나는 필수) | `경과규정`   |

**조번호 형식**:

- 4조: `000400`
- 2조의2: `000202`

#### Response

```typescript
interface JoLawTermResponse {
  joRltLstrmService: {
    검색결과개수: string; // number
    키워드: string;
    target: 'joRltLstrm';
    법령조문: JoLawTermItem;
  };
}

interface JoLawTermItem {
  id: string; // 무시
  조가지번호: string;
  법령명: string;
  조번호: string; // "0004"
  조문내용: string;
  연계용어: JoLawTermLink | JoLawTermLink[];
}

interface JoLawTermLink {
  id: string; // 무시
  용어구분: string; // "핵심용어" | "선정용어"
  용어구분코드: string; // "140101" | "140102"
  법령용어명: string;
  비고: string;
  용어간관계링크: string; // "/DRF/lawService.do?OC=test&target=lstrmRlt&type=XML&MST=667701"
  용어연계조문링크: string; // "/DRF/lawService.do?OC=test&target=lstrmRltJo&type=XML&MST=667701"
}
```

**용어구분코드**:

- `140101`: 핵심용어
- `140102`: 선정용어

---

### 법령용어-일상용어 연계 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

#### Request Parameters

| 파라미터   | 타입   | 필수 | 설명                                              | 예시       |
| ---------- | ------ | ---- | ------------------------------------------------- | ---------- |
| `OC`       | string | O    | API 인증키                                        | -          |
| `target`   | string | O    | 조회 대상                                         | `lstrmRlt` |
| `type`     | string | X    | 응답 형식 (기본 XML)                              | `JSON`     |
| `query`    | string | X    | 법령용어 검색어 (MST 또는 query 중 하나는 필수)   | `청원`     |
| `MST`      | string | X    | 법령용어 일련번호 (MST 또는 query 중 하나는 필수) | `667701`   |
| `trmRltCd` | string | X    | 용어관계코드 필터                                 | `140304`   |

**용어관계코드**:

- `140301`: 동의어
- `140302`: 반의어
- `140303`: 상위어
- `140304`: 하위어
- `140305`: 연관어

#### Response

```typescript
interface LawTermRelationResponse {
  lstrmRltService: {
    검색결과개수: string; // number
    키워드: string;
    target: 'lstrmRlt';
    법령용어: LawTermRelationItem;
  };
}

interface LawTermRelationItem {
  id: string; // 무시
  법령용어명: string;
  비고: string;
  연계용어: DailyTermLink | DailyTermLink[];
}

interface DailyTermLink {
  id: string; // 무시
  일상용어명: string;
  용어관계: string; // "동의어" | "반의어" | "상위어" | "하위어" | "연관어"
  용어관계코드: string; // "140301" | "140302" | "140303" | "140304" | "140305"
  용어간관계링크: string; // "/DRF/lawService.do?OC=test&target=dlytrmRlt&type=XML&MST=365245"
  일상용어조회링크: string; // "/DRF/lawSearch.do?OC=test&target=dlytrm&type=XML&query=국민동의청원"
}
```

---

### 법령용어 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                 | 예시    |
| -------- | ------ | ---- | -------------------- | ------- |
| `OC`     | string | O    | API 인증키           | -       |
| `target` | string | O    | 조회 대상            | `lstrm` |
| `type`   | string | X    | 응답 형식 (기본 XML) | `JSON`  |
| `query`  | string | O    | 법령용어 검색어      | `청원`  |

#### Response

```typescript
interface LawTermContentResponse {
  LsTrmService: {
    법령용어코드명: string[]; // ["법령용어사전", "법령정의사전", "법령정의사전", "법령한영사전"]
    법령용어일련번호: string[]; // MST 배열
    법령용어명_한자: string[]; // 한자 표기 배열
    법령용어코드: string[]; // ["011401", "011402", "011402", "011403"]
    법령용어정의: string[]; // 정의 내용 배열 (매우 긴 텍스트)
    법령용어명_한글: string[]; // 한글 표기 배열
    출처: string[]; // 출처 배열
  };
}
```

**법령용어코드**:

- `011401`: 법령용어사전
- `011402`: 법령정의사전
- `011403`: 법령한영사전

**주의사항**:

- 응답이 배열 형식으로 반환됨
- 각 배열의 동일한 인덱스 요소가 하나의 사전 항목을 구성함
- 예: `법령용어코드명[0]`, `법령용어일련번호[0]`, `법령용어정의[0]`은 하나의 사전 항목

---

## 응답 예시

### 법령용어 목록 조회 예시

**요청**:

```
GET http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=lstrm&type=JSON&sort=rasc&display=5
```

**응답**:

```json
{
  "LsTrmSearch": {
    "resultMsg": "success",
    "키워드": "*",
    "page": "1",
    "resultCode": "00",
    "target": "lstrm",
    "lstrm": [
      {
        "id": "1",
        "법령용어상세링크": "/DRF/lawService.do?OC=dykim2098&target=lstrm&trmSeqs=13396&mobile=&type=XML",
        "법령종류코드": "010101",
        "법령용어ID": "13396",
        "법령용어명": "가격결정",
        "법령용어상세검색": "/LSW/lsTrmInfoR.do?trmSeqs=13396&mobile=",
        "사전구분코드": "011403"
      },
      {
        "id": "2",
        "법령용어상세링크": "/DRF/lawService.do?OC=dykim2098&target=lstrm&trmSeqs=13418&mobile=&type=XML",
        "법령종류코드": "010101",
        "법령용어ID": "13418",
        "법령용어명": "가납",
        "법령용어상세검색": "/LSW/lsTrmInfoR.do?trmSeqs=13418&mobile=",
        "사전구분코드": "011403"
      }
    ],
    "totalCnt": "72926",
    "section": "tn_lstrm_list",
    "numOfRows": "5"
  }
}
```

### 조문-법령용어 연계 조회 예시

**요청**:

```
GET http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=joRltLstrm&type=JSON&ID=1233&JO=000400
```

**응답**:

```json
{
  "joRltLstrmService": {
    "검색결과개수": "1",
    "키워드": "",
    "target": "joRltLstrm",
    "법령조문": {
      "id": "1",
      "조가지번호": "00",
      "법령명": "상법시행법",
      "조번호": "0004",
      "연계용어": [
        {
          "용어간관계링크": "/DRF/lawService.do?OC=test&target=lstrmRlt&type=XML&MST=667701",
          "id": "1",
          "용어구분": "핵심용어",
          "법령용어명": "경과규정",
          "비고": "",
          "용어연계조문링크": "/DRF/lawService.do?OC=test&target=lstrmRltJo&type=XML&MST=667701",
          "용어구분코드": "140101"
        }
      ],
      "조문내용": "제4조(시효에 관한 경과규정) ①상법시행당시구법의 규정에 의한 소멸시효기간을 경과하지 아니한 권리에는 상법의 시효에 관한 규정을 적용한다.②전항의 규정은 시효기간이 아닌 법정기간에 준용한다."
    }
  }
}
```

### 법령용어-일상용어 연계 조회 예시

**요청**:

```
GET http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=lstrmRlt&type=JSON&query=청원
```

**응답**:

```json
{
  "lstrmRltService": {
    "검색결과개수": "1",
    "키워드": "청원",
    "target": "lstrmRlt",
    "법령용어": {
      "id": "1",
      "연계용어": [
        {
          "용어간관계링크": "/DRF/lawService.do?OC=test&target=dlytrmRlt&type=XML&MST=365245",
          "id": "1",
          "일상용어명": "국민동의청원",
          "용어관계": "하위어",
          "용어관계코드": "140304",
          "일상용어조회링크": "/DRF/lawSearch.do?OC=test&target=dlytrm&type=XML&query=국민동의청원"
        },
        {
          "용어간관계링크": "/DRF/lawService.do?OC=test&target=dlytrmRlt&type=XML&MST=2117",
          "id": "2",
          "일상용어명": "민원",
          "용어관계": "연관어",
          "용어관계코드": "140305",
          "일상용어조회링크": "/DRF/lawSearch.do?OC=test&target=dlytrm&type=XML&query=민원"
        }
      ],
      "법령용어명": "청원",
      "비고": ""
    }
  }
}
```

### 법령용어 본문 조회 예시

**요청**:

```
GET http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=lstrm&query=청원&type=JSON
```

**응답**:

```json
{
  "LsTrmService": {
    "법령용어코드명": [
      "법령용어사전",
      "법령정의사전",
      "법령정의사전",
      "법령한영사전"
    ],
    "법령용어일련번호": ["29180", "1273987", "5029811", "20409"],
    "법령용어명_한자": ["請願", "청원", "청원", "請願"],
    "법령용어코드": ["011401", "011402", "011402", "011403"],
    "법령용어정의": [
      "국민이 국가기관에 대해서 희망을 요구하는 것을 말하며, 수익권의 일종이다. 국민의 청원권은 현대의 각국 헌법에서 거의 빠짐없이 보장하고 있고, 우리나라 헌법에서도 이를 보장하고 있다(헌법 제26조). 청원사항은 단지 소극적으로 불평의 구제에 그칠 것이 아니라, 적극적으로 국가에 대해서 희망을 요구하는 것도 포함된다. 청원의 대상이 되는 국가기관도 원칙적으로 제한이 있을 수 없고 행정기관·입법기관은 물론 법원에 대해서도 할 수 있지만, 헌법상 인정된 국가기관의 권한을 침해하는 청원은 허용될 수 없다. 국민의 청원권을 규정하고 있는 「헌법」 제26조1항은 '법률이 정하는 바에 의하여'라는 문언이 삽입되어 있어서 청원사항·청원절차와 청원의 대상이 되는 국가기관을 법률로써 제한할 수 있으며 내재적 한계를 벗어난 청원을 법률로써 제한하는 것은 무방하지만, 그 한도를 넘어서 법률로써 제한하면 그 법률은 위헌이 된다. 「헌법」에 의하면 청원은 반드시 문서로써 하여야 하고, 국가기관은 이를 수리하여 심사할 의무만 지고 재결을 해줄 필요가 없는 점에서 행정심판과 차이가 있다. 청원에 관해서는 「국회법」·「지방자치법」 등에 개별적인 규정이 있다.",
      "국민이 국회의원 1인 이상의 소개를 얻어 국가 또는 공공단체의 기관에 대하여 피해의 구제, 공무원의 비위 시정 또는 공무원에 대한 징계나 처벌요구, 법률·명령·규칙의 제·개정 또는 폐지, 공공의 제도 또는 시설의 운영, 기타 공공기관의 권한에 속하는 사항에 관하여 의견제시 및 권리구제를 요청하는 것을 말한다.(국회법 제123∼126조, 청원법 제4조) ",
      " 국민이 국회의원 1인 이상의 소개를 얻어 국가 또는 공공단체 등 기관에 대하여 피해의 구제, 공무원의 비위 시정 또는 공무원에 대한 징계나 처벌요구, 법률ㆍ명령ㆍ규칙의 제ㆍ개정 또는 폐지, 공공의 제도 또는 시설의 운영, 기타 공공기관의 권한에 속하는 사항에 관하여 의견제시 및 권리구제를 요청하는 것을 말한다.(「국회법」 제123조 또는 제126조, 「청원법」 제4조) ",
      "petition"
    ],
    "법령용어명_한글": ["청원", "청원", "청원", "청원"],
    "출처": [
      "대국회 및 당정 협의업무처리 등에 관한지침[방위사업청예규 제51호, 2012.10.26., 일부개정]",
      "대 국회 및 정당 업무처리 훈령[국방부훈령 제2887호, 2024.1.22., 전부개정]"
    ]
  }
}
```
