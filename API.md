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
- DatabaseManager.Model.user.authenticate의 Callback result

---

## /process/logoutUser (GET)
### Input

### Output (JSON)
- result: 결과 (true)

---

## /process/getMatchList (GET)
### Input

### Output (JSON)
- DatabaseManager.Model.matching.getAllMatches의 Callback result

---

## /process/requestMatch (POST)
### Input
- (Session) userInfo: 사용자 정보 (로그인 여부 체크)
- (Session) userInfo.id: 사용자 ID
- activityType: 종목 이름
- maxUsers: 최대 사용자
- matchId: 매치 ID (기존 Match방에 참가하는 경우)

### Output (JSON)
- result: 결과(true/false)
- reason: false일경우 이유
    - NotLoggedInException
    - DatabaseManager.Model.matching.updateMatchParticipants의 Callback result
    - DatabaseManager.Model.matching.createMatch의 Callback result
    - DatabaseManager.Model.matching.findMatch의 Callback result

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