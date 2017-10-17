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
    - (DatabaseManager.createUser에서의) MongoError
- mongoerror: reason = 'MongoError'일때의 Error 객체

---

## /process/checkLoggedIn (GET)
### Input

### Output (JSON)
- logged_as: 결과 (req.session.userInfo.id 또는 false)

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

## /process/requestMatch (POST)
### Input
- (Session) userInfo: 사용자 정보 (로그인 여부 체크)
- (Session) userInfo.id: 사용자 ID
- activityType: 종목 이름
- players: 사용자 목록 (|로 구분)
- matchId: 매치 ID (매치 고유 ID, 검색 용도로 사용)

### Output (JSON)
- result: 결과(true/false)
- participants: 성공했을 경우, 새로운 참가자 리스트 배열
- reason: false일경우 이유
    - NotLoggedInException
    - MongoError (DB 오류)
- mongoerror: reason = 'MongoError'인 경우 에러

---

## /process/heartbeat (GET)
### Input

### Output (JSON)
- result: true

---

## /process/checkExistingUser (POST)
### Input
- id: 사용자 ID

### Output
- result: 결과 (true/false)
- id: true일경우, 가져온 사용자 ID
- name: true일경우, 가져온 사용자 이름
- DatabaseManager.Model.user.findId의 Callback result