# API

## /process/registerUser (POST)
### Input
- id: 사용자 군번 (=아이디)
- password: 사용자 비밀번호
- name: 사용자 이름
- rank: 계급 (0부터 이등병)
- unit: 소속부대 (이름)
- gender: 성별 (0=남성)
- favoriteEvent: 좋아하는 종목 (이름들, 현재 사용 X)
- description: 자기소개 (현재 사용 X)

### Output (JSON)
- result: 결과 (true/false)
- reason: 실패 시 사유 
    - MissingValuesException
    - AlreadyExistingException
    - IllegalParametersException
    - (DatabaseManager.createUser에서의) MongoError
- mongoerror: reason = 'MongoError'일때의 Error 객체

---

## /process/loginUser (POST)
### Input
- id: 사용자 군번 (=아이디)
- password: 사용자 비밀번호

### Output
- result: 결과 (true/false)
- id: 사용자 아이디 (true일 경우)
- name: 사용자 이름 (true일 경우)
- rank: 사용자 계급 (true일 경우)
- reason: 실패 이유 (false일 경우)
    - PasswordMismatch
    - NoSuchUserException (findId가 실패할 경우)
    - MultipleUserException (findId가 실패할 경우)

---

## /process/logoutUser (GET)
### Input

### Output (JSON)
- result: 결과 (true)

---

## /process/checkExistingUser (POST)
### Input
- id: 사용자 ID

### Output
- result: 결과 (true/false)
- DatabaseManager.Model.user.findId의 Callback result

---

## /process/searchUserDetails (POST)
### Input
- (Session) userInfo: 로그인 정보 (로그인 여부 체크)
- id: 사용자 ID

### Output
- result: 결과 (true/false)
- name: 성공 시 사용자 이름
- rank: 성공 시 사용자 계급
- reason: 실패 시 사유 (result = false)
    - NoSuchUserException
    - NotLoggedInException

---

## /process/getUserInfo (GET)
### Input
- (Session) userInfo: 로그인 정보 (로그인한 유저인지 체크)
- (Session) userInfo.id: 로그인한 ID (해당 유저의 정보 받아오기 위해 체크)

### Output
- result: 성공 여부 (true/false)
- id: 사용자 ID
- name: 사용자 이름
- rank: 사용자 계급
- gender: 사용자 성별
- unit: 사용자 부대명
- favoriteEvent: 사용자가 좋아하는 운동
- description: 사용자 자기소개
- created_at: 사용자 생성일자
- updated_at: 사용자 마지막 수정일자 (JSON)
- reason: 실패시 사유 (result = false)
    - NotLoggedInException
    - NoSuchUserException (불가능)
    - MultipleUserException (불가능)
    - MongoError
- mongoerror: MongoError 객체 (오류 시)

---

## /process/updateUserInfo (POST)
### Input
- (Session) userInfo: 로그인 정보 (로그인한 유저인지 체크)
- (Session) userInfo.id: 로그인한 ID (해당 유저의 정보 받아오기 위해 체크)
- 아래는 있는 값들만 변경을 한다. (보내지 않거나 데이터가 없으면, eg, "", 수정 X)
    - name: 변경된 사용자 이름
    - rank: 변경된 사용자 계급
    - gender: 변경된 사용자 성별
    - password: 변경할 사용자 비밀번호
    - unit: 변경된 사용자 부대명
    - favoriteEvent: 변경된 사용자가 좋아하는 운동
    - description: 변경된 사용자 자기소개

### Output
- result: 성공 여부 (true/false)
- reason: 실패시 사유 (result = false)

---

## /process/getMatchList (GET)
### Input

### Output (JSON)
- result: 결과 (true/false)
- reason: 실패 사유 (false인 경우)
    - MongoError (DB 오류)
- mongoerror: reason = 'MongoError'인 경우 에러
- docs: 성공한 경우 결과 Document
    - 배열 안의 _doc가 Document

---

## /process/getUserMatch (POST)
### Input
- (Session) userInfo: 로그인 정보 (로그인한 유저인지 체크)
- (Session) userInfo.id: 로그인한 ID (해당 유저의 Match 검색시 활용)

### Output
- result: 결과 (true/false)
- match: 성공시 Match 정보
    - activityType: 경기 종류
    - players: 플레이어 목록 (배열)
    - stadium: 경기장 이름
    - start_at: 매치 생성 시간
    - matchId: 매치 고유 ID
    - initiatorId: 매치 생성자 ID
- reason: 실패 시 사유 (result = false)
    - NoSuchMatchException
    - NotLoggedInException

---

## /process/requestMatch (POST)
### Input
- (Session) userInfo: 사용자 정보 (로그인 여부 체크)
- (Session) userInfo.id: 사용자 ID
- activityType: 종목 이름
- players: 사용자 목록 (|로 구분)

### Output (JSON)
- result: 결과(true/false)
- reason: false일경우 이유
    - NotLoggedInException
    - MatchAlreadyExistsException (이미 매치를 가지고 있는 사용자의 경우)
    - MongoError (DB 오류)
- mongoerror: reason = 'MongoError'인 경우 에러

---

## /process/deleteMatch (POST)
### Input
- (Session) userInfo: 로그인 정보 (로그인한 유저인지 체크)
- (Session) userInfo.id: 로그인한 ID (해당 유저의 Match인지 체크) → initiatorId (매치 만든 사람)으로 저장되어 있음...
- matchId: 매치 ID (삭제할 매치 ID. 해당 유저의 매치 목록은 /process/getUserMatch로 확인)

### Output
- result: 성공 여부(true/false)
- reason: 실패 사유(result = false)
    - ForbiddenOperationException: 내 매치가 아닌 다른 사람의 매치를 삭제하려고 시도했을 경우
    - NoSuchMatchException
    - NotLoggedInException
    - MongoError
- mongoerror: MongoError 객체 (오류 시)

---

## /process/getStadiumList (POST) [DRAFT]
### Input
- (Session) userInfo: 로그인 정보 (로그인한 유저인지 체크)

### Output
- (사용자의 위치에 해당하는 Stadium List)

---

## /process/heartbeat (GET)
### Input

### Output (JSON)
- result: true
