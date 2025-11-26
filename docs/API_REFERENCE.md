# 국가법령정보 Open API 레퍼런스

이 문서는 국가법령정보 공동활용 Open API의 우선순위 API 목록과 요청/응답 형태를 정리합니다.

## 기본 정보

- **Base URL (목록 조회)**: `http://www.law.go.kr/DRF/lawSearch.do`
- **Base URL (본문 조회)**: `http://www.law.go.kr/DRF/lawService.do`
- **공통 필수 파라미터**:
  - `OC`: API 인증키 (이메일 ID 부분, 예: `user@example.com` → `user`)
  - `target`: 조회 대상 (API별 상이)
  - `type`: 응답 형식 (`JSON`, `XML`, `HTML`)

---

## 법령 관련 API

### 1. 현행법령(시행일) 목록 조회

| 항목         | 값                                      |
| ------------ | --------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawSearch.do` |
| **target**   | `eflaw`                                 |
| **Method**   | GET                                     |

#### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                                 |
| --------- | ------ | ---- | ---------------------------------------------------- |
| `OC`      | string | O    | API 인증키                                           |
| `target`  | string | O    | `eflaw` (고정)                                       |
| `type`    | string | O    | 응답 형식 (`JSON`/`XML`/`HTML`)                      |
| `display` | int    | X    | 결과 개수 (default=20, max=100)                      |
| `page`    | int    | X    | 페이지 번호 (default=1)                              |
| `sort`    | string | X    | 정렬옵션 (lasc/ldes/dasc/ddes/nasc/ndes/efasc/efdes) |
| `query`   | string | X    | 법령명 검색어                                        |
| `ancYd`   | string | X    | 공포일자 범위 (YYYYMMDD~YYYYMMDD)                    |
| `efYd`    | string | X    | 시행일자 범위 (YYYYMMDD~YYYYMMDD)                    |
| `LID`     | string | X    | 법령ID                                               |
| `nw`      | int    | X    | 1:연혁, 2:시행예정, 3:현행 (조합 가능: 1,2,3)        |
| `org`     | string | X    | 소관부처코드                                         |
| `knd`     | string | X    | 법령종류코드                                         |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=eflaw&type=JSON&display=20&page=1
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=eflaw&type=JSON&ancYd=20240101~20240131
```

---

### 2. 일자별 조문 개정 이력 목록 조회

| 항목         | 값                                      |
| ------------ | --------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawSearch.do` |
| **target**   | `lsJoHstInf`                            |
| **Method**   | GET                                     |

#### Request Parameters

| 파라미터    | 타입   | 필수 | 설명                                         |
| ----------- | ------ | ---- | -------------------------------------------- |
| `OC`        | string | O    | API 인증키                                   |
| `target`    | string | O    | `lsJoHstInf` (고정)                          |
| `type`      | string | O    | 응답 형식 (`JSON`/`XML`)                     |
| `regDt`     | int    | X    | 조문 개정일 (YYYYMMDD)                       |
| `fromRegDt` | int    | X    | 조회기간 시작일 (YYYYMMDD)                   |
| `toRegDt`   | int    | X    | 조회기간 종료일 (YYYYMMDD)                   |
| `ID`        | int    | X    | 법령ID                                       |
| `JO`        | int    | X    | 조문번호 (000400 = 제4조, 000202 = 제2조의2) |
| `org`       | string | X    | 소관부처코드                                 |
| `page`      | int    | X    | 페이지 번호 (default=1)                      |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=lsJoHstInf&type=JSON&regDt=20240101
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=lsJoHstInf&type=JSON&ID=1233&regDt=20240101
```

---

### 3. 현행법령(공포일) 본문 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `law`                                    |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                            |
| -------- | ------ | ---- | ------------------------------- |
| `OC`     | string | O    | API 인증키                      |
| `target` | string | O    | `law` (고정)                    |
| `type`   | string | O    | 응답 형식 (`JSON`/`XML`/`HTML`) |
| `MST`    | int    | O    | 법령일련번호                    |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=law&type=JSON&MST=1110
```

---

### 4. 관계법령 조회 (위임법령)

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `law` + `conType`                        |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                            |
| --------- | ------ | ---- | ------------------------------- |
| `OC`      | string | O    | API 인증키                      |
| `target`  | string | O    | `law` (고정)                    |
| `type`    | string | O    | 응답 형식                       |
| `MST`     | int    | O    | 법령일련번호                    |
| `conType` | string | O    | 관계유형 (ENTRUST: 위임법령 등) |

**Note**: 이 API의 정확한 동작은 실제 응답으로 검증 필요

---

### 5. 법령용어 목록 조회

| 항목         | 값                                      |
| ------------ | --------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawSearch.do` |
| **target**   | `lstrm`                                 |
| **Method**   | GET                                     |

#### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                                  |
| --------- | ------ | ---- | ------------------------------------- |
| `OC`      | string | O    | API 인증키                            |
| `target`  | string | O    | `lstrm` (고정)                        |
| `type`    | string | O    | 응답 형식 (`JSON`/`XML`)              |
| `display` | int    | X    | 결과 개수 (default=20, max=100)       |
| `page`    | int    | X    | 페이지 번호 (default=1)               |
| `sort`    | string | X    | 정렬 (rasc: 오름차순, rdes: 내림차순) |
| `query`   | string | X    | 검색어                                |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=lstrm&type=JSON&display=20&sort=rasc
```

---

### 6. 법령용어 본문 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `lstrm`                                  |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터  | 타입   | 필수 | 설명                     |
| --------- | ------ | ---- | ------------------------ |
| `OC`      | string | O    | API 인증키               |
| `target`  | string | O    | `lstrm` (고정)           |
| `type`    | string | O    | 응답 형식 (`JSON`/`XML`) |
| `query`   | string | O    | 법령용어 검색어          |
| `trmSeqs` | string | X    | 법령용어 일련번호        |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=lstrm&type=JSON&query=청원
```

---

### 7. 조문-법령용어 연계 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `joRltLstrm`                             |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                      |
| -------- | ------ | ---- | ------------------------- |
| `OC`     | string | O    | API 인증키                |
| `target` | string | O    | `joRltLstrm` (고정)       |
| `type`   | string | O    | 응답 형식 (`JSON`/`XML`)  |
| `ID`     | string | X    | 법령ID                    |
| `JO`     | string | X    | 조문번호 (000400 = 제4조) |
| `query`  | string | X    | 법령용어 검색어           |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=joRltLstrm&type=JSON&ID=1233&JO=000400
```

---

### 8. 법령용어-일상용어 연계 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `lstrmRlt`                               |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터   | 타입   | 필수 | 설명                                                                                     |
| ---------- | ------ | ---- | ---------------------------------------------------------------------------------------- |
| `OC`       | string | O    | API 인증키                                                                               |
| `target`   | string | O    | `lstrmRlt` (고정)                                                                        |
| `type`     | string | O    | 응답 형식 (`JSON`/`XML`)                                                                 |
| `query`    | string | X    | 법령용어 검색어                                                                          |
| `MST`      | string | X    | 법령용어 일련번호                                                                        |
| `trmRltCd` | string | X    | 용어관계코드 (140301:동의어, 140302:반의어, 140303:상위어, 140304:하위어, 140305:연관어) |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=lstrmRlt&type=JSON&query=청원
```

---

## 판례 관련 API

### 1. 판례 목록 조회

| 항목         | 값                                      |
| ------------ | --------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawSearch.do` |
| **target**   | `prec`                                  |
| **Method**   | GET                                     |

#### Request Parameters

| 파라미터   | 타입   | 필수 | 설명                                      |
| ---------- | ------ | ---- | ----------------------------------------- |
| `OC`       | string | O    | API 인증키                                |
| `target`   | string | O    | `prec` (고정)                             |
| `type`     | string | O    | 응답 형식 (`JSON`/`XML`/`HTML`)           |
| `display`  | int    | X    | 결과 개수 (default=20, max=100)           |
| `page`     | int    | X    | 페이지 번호 (default=1)                   |
| `sort`     | string | X    | 정렬옵션 (lasc/ldes/dasc/ddes/nasc/ndes)  |
| `query`    | string | X    | 검색어                                    |
| `search`   | int    | X    | 검색범위 (1:판례명, 2:본문검색)           |
| `prncYd`   | string | X    | 선고일자 범위 (YYYYMMDD~YYYYMMDD)         |
| `date`     | int    | X    | 선고일자 (YYYYMMDD)                       |
| `org`      | string | X    | 법원종류 (400201:대법원, 400202:하위법원) |
| `curt`     | string | X    | 법원명                                    |
| `nb`       | string | X    | 사건번호                                  |
| `JO`       | string | X    | 참조법령명                                |
| `datSrcNm` | string | X    | 데이터출처명                              |

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=prec&type=JSON&display=20&page=1
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=prec&type=JSON&prncYd=20240101~20240131
```

---

### 2. 판례 본문 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `prec`                                   |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                            |
| -------- | ------ | ---- | ------------------------------- |
| `OC`     | string | O    | API 인증키                      |
| `target` | string | O    | `prec` (고정)                   |
| `type`   | string | O    | 응답 형식 (`JSON`/`XML`/`HTML`) |
| `ID`     | int    | O    | 판례일련번호                    |

**Note**: 국세청 판례 본문 조회는 HTML만 가능

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawService.do?OC=dykim2098&target=prec&type=JSON&ID=228541
```

---

### 3. 헌재결정례 목록 조회

| 항목         | 값                                      |
| ------------ | --------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawSearch.do` |
| **target**   | `detc`                                  |
| **Method**   | GET                                     |

#### Request Parameters

판례 목록 조회와 유사 (target만 `detc`로 변경)

#### 샘플 URL

```
http://www.law.go.kr/DRF/lawSearch.do?OC=dykim2098&target=detc&type=JSON&display=20&page=1
```

---

### 4. 헌재결정례 본문 조회

| 항목         | 값                                       |
| ------------ | ---------------------------------------- |
| **Endpoint** | `http://www.law.go.kr/DRF/lawService.do` |
| **target**   | `detc`                                   |
| **Method**   | GET                                      |

#### Request Parameters

| 파라미터 | 타입   | 필수 | 설명                |
| -------- | ------ | ---- | ------------------- |
| `OC`     | string | O    | API 인증키          |
| `target` | string | O    | `detc` (고정)       |
| `type`   | string | O    | 응답 형식           |
| `ID`     | int    | O    | 헌재결정례 일련번호 |

---

### 5. 법령해석례 목록/본문 조회

- **목록 target**: `expc`
- **본문 target**: `expc`

### 6. 행정심판례 목록/본문 조회

- **목록 target**: `decc`
- **본문 target**: `decc`

### 7. 위원회결정문

각 위원회별 target:
| 위원회 | 목록 target | 본문 target |
|--------|-------------|-------------|
| 개인정보보호위원회 | `ppc` | `ppc` |
| 공정거래위원회 | `ftc` | `ftc` |
| 국민권익위원회 | `acr` | `acr` |
| 금융위원회 | `fsc` | `fsc` |
| 노동위원회 | `nlrc` | `nlrc` |
| 국가인권위원회 | `nhrck` | `nhrck` |

---

## 응답 구조 특이사항

### 알려진 이슈

1. **배열 vs 단일 객체**: 결과가 1건일 때 배열이 아닌 단일 객체로 반환되는 경우 있음
2. **빈 문자열**: 값이 없는 필드는 `""` (빈 문자열)로 반환
3. **숫자 타입**: 숫자 필드도 문자열로 반환되는 경우 있음 (예: `"totalCnt": "100"`)
4. **중첩 구조**: 공동부령정보, 연락부서 등은 빈 문자열 또는 객체/배열
5. **날짜 형식**: YYYYMMDD 문자열

### 응답 래퍼 구조

- **목록 조회**: `{ "LawSearch": { ... } }`, `{ "PrecSearch": { ... } }` 등
- **본문 조회**: `{ "법령": { ... } }`, `{ "PrecService": { ... } }` 등

---

## 참고

- 공식 가이드: https://open.law.go.kr/LSO/openApi/guideList.do
- 실제 응답 구조는 테스트를 통해 검증 필요
