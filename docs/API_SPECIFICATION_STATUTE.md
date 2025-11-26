# 국가법령정보 공동활용 Open API 법령 명세서

이 수집 프로젝트에서 사용하는 국가법령정보 공동활용 Open API(open.law.go.kr)의 법령 관련 Request/Response 명세

## 현행법령(시행일) 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                   | 예시                |
| --------- | ------ | ---- | -------------------------------------- | ------------------- |
| `OC`      | string | O    | API 인증키                             | -                   |
| `target`  | string | O    | 조회 대상                              | `eflaw`             |
| `type`    | string | X    | 응답 형식 (기본 XML)                   | `JSON`              |
| `display` | number | X    | 페이지당 표시 건수 (기본 20, 최대 100) | 100                 |
| `sort`    | string | X    | 정렬 방식 (기본 `lasc`)                | `dasc`, `ddes`      |
| `ancYd`   | string | X    | 공포일자 범위 (YYYYMMDD~YYYYMMDD)      | `20200101~20201231` |
| `page`    | number | X    | 페이지 번호 (1-based, 기본 1)          | 1                   |

### Response

```typescript
interface LawListResponse {
  law: LawListItem | LawListItem[];
  totalCnt: string; // number
  // 이하 무시
  page: string; // number
  target: 'eflaw';
  resultMsg: 'success';
  section: 'lawNm';
  numOfRows: string; // number
}

interface LawListItem {
  id: string; //무시
  법령일련번호: string;
  법령ID: string;
  법령명한글: string;
  법령명한자: string;
  법령약칭명: string;
  법령구분명: string;
  공포일자: string; // YYYYMMDD
  공포번호: string;
  시행일자: string; // YYYYMMDD
  소관부처명: string;
  소관부처코드: string;
  제개정구분명: string;
  현행연혁코드: string;
  자법타법여부: string;
  공동부령정보: '' | CoEnactment | CoEnactment[];
}

interface CoEnactment {
  no: string;
  공포번호: string;
  공동부령구분: {
    content: string;
    구분코드: string;
  };
}
```

---

## 일자별 조문 개정 이력 목록 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawSearch.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                          | 예시         |
| -------- | ------ | ---- | ----------------------------- | ------------ |
| `OC`     | string | O    | API 인증키                    | -            |
| `target` | string | O    | 조회 대상                     | `lsJoHstInf` |
| `type`   | string | X    | 응답 형식 (기본 XML)          | `JSON`       |
| `ID`     | number | X    | 법령ID                        | 2132         |
| `regDt`  | number | X    | 조문 개정일(YYYYMMDD)         | 20200101     |
| `page`   | number | X    | 페이지 번호 (1-based, 기본 1) | 1            |

### Response

```typescript
interface JoListResponse {
  law: JoListItem | JoListItem[];
  totalCnt: string; // number
  // 이하 무시
  page: string; // number
  target: 'lsJoHstInf';
  numOfRows: string; // number
}

interface JoListItem {
  id: string; //무시
  조문정보: {
    jo: JoBaseItem | JoBaseItem[];
  };
  법령정보: {
    법령일련번호: string;
    법령ID: string;
    법령명한글: string;
    법령구분명: string;
    공포일자: string; // YYYYMMDD
    공포번호: string;
    시행일자: string; // YYYYMMDD
    소관부처명: string;
    소관부처코드: string;
    제개정구분명: string;
  };
}

interface JoBaseItem {
  num: string; // 무시
  조문변경이력상세링크: string; // 무시
  조문링크: string; // 무시
  변경사유: string; // 이전에 사용하던 API의 현행법령 본문 조회의 조문재개정유형에 해당.
  조문번호: string; // 실제로는 (000100, 000201 형태의)조문키가 들어온다.
  조문시행일: string; // YYYYMMDD
  조문개정일: string; // YYYYMMDD
}
```

---

## 현행법령(공포일) 본문 조회

**Endpoint**: `http://www.law.go.kr/DRF/lawService.do`

**Method**: `GET`

### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                | 예시   |
| -------- | ------ | ---- | ------------------- | ------ |
| `OC`     | string | O    | API 인증키          | -      |
| `target` | string | O    | 조회 대상           | `law`  |
| `type`   | string | X    | 응답 형식(기본 XML) | `JSON` |
| `MST`    | string | O    | 법령일련번호        | 9129   |
| `LN`     | number | X    | 공포번호            | 14827  |

#### Response

```typescript
interface LawContentResponse {
  기본정보: BasicInfo;
  조문?: {
    조문단위: Article | Article[];
  };
  재개정이유?: {
    재개정이유내용: string[][];
  };
  개정문?: {
    개정문내용: string[][];
  };
  부칙?: {
    부칙단위: Addendum | Addendum[];
  };
  별표?: {
    별표단위: Appendix | Appendix[];
  };
}

interface BasicInfo {
  법령ID: string;
  시행일자: string; // YYYYMMDD
  공포일자: string; // YYYYMMDD
  공포번호: string;
  언어: string;
  제개정구분: string;
  법령명_한글: string;
  법령명_한자: string;
  법령명약칭: string;
  편장절관: string;
  법종구분: {
    content: string;
    법종구분코드: string;
  };
  의결구분: string;
  제안구분: string;
  공동부령정보: '' | CoEnactment | CoEnactment[];
  공포법령여부: 'Y' | 'N';
  소관부처: {
    content: string; // 콤마로 구분
    소관부처코드: string; // 콤마로 구분
  };
  전화번호: string; // 콤마로 구분
  연락부서: '' | Department | Department[];
  별표편집여부?: 'Y' | 'N';
}

interface Department {
  부서키: string;
  소관부처코드: string;
  소관부처명: string;
  부서명: string;
  부서연락처: string;
}

interface Article {
  조문키: string; // 무시, 프로젝트 내에서의 000100(1조), 000201(2조의 1)과 같은 조문키 표기와 달리 0001001(1조), 0002001(조의1)과 같은 별개의 sequence를 준다.
  조문번호: string;
  조문가지번호: string;
  조문내용: string;
  조문제목: string;
  항?: Paragraph | Paragraph[];
  // 조문제개정유형: string;
  조문여부: string;
  조문변경여부: 'Y' | 'N';
  조문이동이전: string;
  조문이동이후: string;
  조문시행일자: string;
  조문참고자료: string;
  제명변경여부: 'Y' | 'N';
  한글법령여부: 'Y' | 'N';
}

interface Paragraph {
  항번호?: string;
  항가지번호?: string;
  항내용?: string;
  호?: Item | Item[];
  // 항제개정유형: string;
  항제개정일자: string;
}

interface Item {
  호번호?: string;
  호가지번호?: string;
  호내용?: string;
  목?: SubItem | SubItem[];
}

interface SubItem {
  목번호: string;
  목가지번호?: string;
  목내용: string | string[][];
}

interface Addendum {
  부칙키: string;
  부칙공포일자: string; // YYYYMMDD
  부칙내용: string[][];
  부칙공포번호: string;
}

interface Appendix {
  별표키: string;
  별표구분: string;
  별표번호: string;
  별표가지번호: string;
  // 별표제목문자열: string;
  별표내용: string[][];
  별표서식파일링크: string;
  별표서식PDF파일링크: string;
  // 별표시행일자: string; // YYYYMMDD
  별표제목: string;
  별표PDF파일명: string;
  별표HWP파일명: string;
  별표이미지파일명: string | string[];
}
```
