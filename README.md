# 프로젝트 구조
* Adapter
    * 프레젠테이션, 외부 모듈 및 설정 파일
    * Controller, Spring Security, Validator, Request/Response DTO
    * JWT 토큰 인증 구조
* Application
    * 서비스, in/out 포트
    * Service, Mapper, UseCase, Port
    * 프레젠테이션 계층과 도메인 계층 연결
* Domain
    * 도메인, 레포지토리 및 비즈니스 로직
    * Entity, Repository, QueryDsl, DB Converter
    * 유저, 휴가, 휴가 신청 모델 및 처리 로직
* Common
    * 모든 계층에서 사용하는 공통 클래스 및 유틸성 함수
    * Exception, DTO, Enum

# 모델
### 유저
* `MemberEntity`
* 이메일 주소(username), 비밀번호(credential), 최근 로그인 일시, 생성일, 변경일
### 휴가
* `VacationEntity` (상속 대상 테이블)
* `FixedVacationEntity` (매년마다 고정으로 발생되는 휴가)
* 유저 : 휴가 = 1 : N
### 휴가 신청 내역
* `VacationHistoryEntity`
* 휴가 : 휴가 신청 내역 = 1 : N

# 추가한 환경 변수
```text
working:
    hour:
        day: 8
        half: 4
        quarter: 2
```
* 하루 근무 시간을 8시간으로 설정 (반차: 4시간, 반반차: 2시간)
* `WorkingHourProperties` 설정 클래스

```text
jwt:
  header: Authorization
  secret: ...
  token-validity-in-seconds: 86400
```
* JWT 토큰 관련 설정
* 헤더명 / 시크릿 키 / 토큰 유효 시간(초)

# API
### 회원가입
```text
URL : /api/v1/auth/signup
METHOD : POST
BODY : { 
    "email": "test@test.com",                             | Required. 이메일 주소 (username)
    "password": "test"                                    | Required. 비밀번호 (credential)
}
RESPONSE : {
    "code": "CREATED",                                    | 성공 응답 코드 (OK, CREATED)
    "data": {
        "id": 1,                                          | 회원 ID 
        "email": "test@test.com"                          | 이메일 주소 
    }
}
```
### 로그인
```text
URL : /api/v1/auth/login
METHOD : POST
BODY : {
    "email": "test@test.com",                             | Required. 이메일 주소
    "password": "test"                                    | Required. 비밀번호
}
RESPONSE : {
    "code": "OK",                                         | 성공 응답 코드 (OK, CREATED)
    "data": {
        "id": 1,                                          | 회원 ID 
        "email": "test@test.com",                         | 이메일 주소 
        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdW..."   | Access Token (JWT)
    }
}
```

### 휴가 리스트 조회
```text
URL : /api/v1/vacation/search/fixed
METHOD : GET
PARAMS : targetYear: Int?,                                | Optional. 대상 연도
         offset: Long = 0L,                               | Optional. OFFSET : 기본값 0
         limit: Long = 20L                                | Optional. LIMIT : 기본값 20
RESPONSE : {
    "code": "OK",
    "data": {
        "total": 1,
        "items": [
            {
                "id": 1,                                  | 휴가 ID
                "memberId": 1,                            | 회원 ID
                "vacationHistoryIds": [1, 2, 3],          | 휴가 신청 ID 리스트
                "targetYear": 2023,                       | 대상 연도
                "days": 15.0,                             | 최초 휴가 일수
                "remainingDays": 10.25,                   | 잔여 일수
                "type": "FIXED",                          | 휴가 유형 : FIXED(고정)
                "createdAt": "2023-04-30T05:41:52..."     | 생성 일시
            }
        ],
        "offset": 0,
        "limit": 20
    }
}
```

### 휴가 상세 조회
```text
URL : /api/v1/vacation/search/fixed/{id}
METHOD : GET
PARAMS : targetYear: Int?,                     | Optional. 대상 연도
         offset: Long = 0L,                    | Optional. OFFSET : 기본값 0
         limit: Long = 20L                     | Optional. LIMIT : 기본값 20
RESPONSE : {
    "code": "OK",                              | 성공 응답 코드 (OK, CREATED)
    "data": {
        "id": 1,                               | 휴가 ID
        "memberId": 1,                         | 회원 ID
        "vacationHistoryIds": [1, 2, 3],       | 휴가 신청 ID 리스트
        "targetYear": 2023,                    | 연차 대상 연도
        "days": 15.0,                          | 최초 휴가 일수
        "remainingDays": 11.0,                 | 잔여 일수
        "type": "FIXED",                       | 휴가 유형 : FIXED(고정)
        "createdAt": "2023-04-30T05:41:52..."  | 생성 일시
    }
}
```
### 휴가 신청
```text
URL : /api/v1/vacation/history
METHOD : POST
BODY : {
    "vacationId": 1,                           | Required. 휴가 ID
    "type": "DAY",                             | Required. 휴가 신청 유형 : DAY(연차), HALF(반차), QUARTER(반반차)
    "startAt": "2023-04-30T00:00:00",          | Required. 시작 일시
    "endAt": "2023-05-03T23:59:59",            | Optional. 종료 일시 (연차인 경우 필수)
    "days": 4,                                 | Optional. 사용 일수 (연차인 경우 필수. 연차인 경우 휴가 일수를 기준으로 잔여일수 차감)
    "comment": "안식"                           | Optional. 휴가 신청 코멘트
}
RESPONSE : {
    "code": "CREATED",                         | 성공 응답 코드 (OK, CREATED)
    "data": {
        "id": 1,                               | 휴가 신청 ID
        "vacationId": 1,                       | 휴가 ID
        "remainingDays": 11.0,                 | 잔여 일수
        "type": "DAY",                         | 휴가 신청 유형, DAY(연차), HALF(반차), QUARTER(반반차)
        "startAt": "2023-04-30T00:00:00",      | 휴가 사용 시작일시
        "endAt": "2023-05-03T23:59:59",        | 휴가 사용 종료일시
        "days": 4.0,                           | 휴가 사용 일수
        "status": "COMPLETED",                 | 휴가 신청 상태 : COMPLETED(완료), CANCELED(취소)
        "comment": "안식"                       | 휴가 신청 코멘트
    }
}
```

### 휴가 신청 리스트 조회
```text
URL : /api/v1/vacation/history/search
METHOD : GET
PARAMS : vacationId: Long?,                 | Optional. 휴가 ID
         type: String?,                     | Optional. 휴가 신청 유형 : DAY(연차), HALF(반차), QUARTER(반반차)
         status: String?,                   | Optional. 휴가 신청 상태 : COMPLETED(완료), CANCELED(취소)
         startAtLoe: LocalDateTime?,        | Optional. 해당 값보다 같거나 작은 시작일시 검색 시 사용
         startAtGoe: LocalDateTime?,        | Optional. 해당 값보다 같거나 큰 시작일시 검색 시 사용
         endAtLoe: LocalDateTime?,          | Optional. 해당 값보다 같거나 작은 종료일시 검색 시 사용
         endAtGoe: LocalDateTime?,          | Optional. 해당 값보다 같거나 큰 종료일시 검색 시 사용
         offset: Long = 0L,                 | Optional. OFFSET : 기본값 0
         limit: Long = 20L                  | Optional. LIMIT : 기본값 20
RESPONSE : {
    "code": "OK",                                    | 성공 응답 코드 (OK, CREATED)
    "data": {                                         
        "total": 3,                                  | 검색 결과 총 개수
        "items": [                                     
            {
                "id": 1,                             | 휴가 신청 ID
                "vacationId": 1,                     | 휴가 ID
                "type": "DAY",                       | 휴가 신청 유형 : DAY(연차), HALF(반차), QUARTER(반반차)
                "startAt": "2023-04-30T00:00:00",    | 휴가 사용 시작일시
                "endAt": "2023-05-03T23:59:59",      | 휴가 사용 종료일시
                "days": 4.0,                         | 휴가 사용 일수
                "status": "COMPLETED",               | 휴가 신청 상태 : COMPLETED(완료), CANCELED(취소)
                "comment": "안식",                    | 휴가 신청 코멘트
                "canceledAt": null,                  | 신청 취소 일시
                "createdAt": "2023-04-30T05:44:20.." | 생성 일시
            },
            {
                "id": 2,
                "vacationId": 1,
                "type": "HALF",
                "startAt": "2023-05-04T00:00:00",
                "endAt": "2023-05-04T04:00:00",
                "days": 0.5,
                "status": "COMPLETED",
                "comment": "안식",
                "canceledAt": null,
                "createdAt": "2023-04-30T05:49:31.617159"
            },
            {
                "id": 3,
                "vacationId": 1,
                "type": "QUARTER",
                "startAt": "2023-05-04T05:00:00",
                "endAt": "2023-05-04T07:00:00",
                "days": 0.25,
                "status": "COMPLETED",
                "comment": "안식",
                "canceledAt": null,
                "createdAt": "2023-04-30T05:50:11.258674"
            }
        ],
        "offset": 0,
        "limit": 20
    }
}
```
### 휴가 신청 취소
```text
URL : /api/v1/vacation/history/cancel/{id}
METHOD : PATCH
RESPONSE : {
    "code": "OK",
    "data": {
        "id": 2,                                    | 취소한 휴가 신청 ID
        "vacationId": 1,                            | 휴가 ID
        "remainingDays": 10.75,                     | 신청 취소 이후 잔여 일수
        "status": "CANCELED",                       | 신청한 휴가 상태 : COMPLETED(완료), CANCELED(취소)
        "canceledAt": "2023-04-30T06:02:12.589176"
    }
}
```

## 플로우

### 휴가 신청하는 경우
1. 회원 가입 (가입 즉시 해당 연도로 고정 휴가 15일 생성)
2. 휴가 리스트 조회 API 호출하여 신청하고 싶은 휴가 확인
3. ex. 2023년도에 본인에게 발생한 휴가에 대해 확인하고 싶은 경우 `?targetYear=2023` 조회
4. 휴가 신청 API 호출 (필수 데이터 : 휴가 신청 ID, 휴가 신청 유형, 시작 일시)
5. 연차의 경우, 시작일시, 종료일시, 신청일수 데이터 필수 (신청일수에 따라 잔여일수 차감되는 구조)
6. 반차/반반차 의 경우, 시작일시 로만 신청일수 계산하여 잔여일수 차감 (반차 4시간, 반반차 2시간)

### 휴가 취소하는 경우
1. 휴가 신청 리스트 조회 API 호출하여 신청 ID 확인
2. ex. 2023.05.01T10:00:00 이후 본인이 신청한 반차 신청 내역을 확인하고 싶은 경우, `?status=COMPLETED&type=HALF&startAtGoe=2023-05-01T10:00:00` 쿼리 조회
3. 휴가 취소 API 호출 (필수 데이터 : 휴가 신청 ID)
4. 휴가 취소 시 해당 사용 일수만큼 잔여일수 증가
