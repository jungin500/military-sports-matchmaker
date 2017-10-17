# armyportal-k

# 어플 개발 Roadmap

- 전투체육 매치

- 역할 분담
	1. 김범수: Frontend 개발, 디자인
	2. 임대인: Frontend 개발, 디자인
    3. 안정인: Backend 개발

- 서버

	1. GET 처리: 현재 진행중인 매치 목록 
		- 매치 완료 시점(예, 16:00) 전/후로 구분하여 전달 
		- 매치 완료 시점 이전에는 현재 상태를 확인. √
		- 매치 완료 시점 이후에는 진행중인 매치 및 인원 확인.
		- 취소 사람 발생시 사람 더 들어올 수 있게 큐 진입 가능하게 + dynamic하게 매치 잡기.
		- 필요 시 특정 시간에 매치 남은 자리가 있다는 사실 통보 (몇 자리 안남은 매치 홍보)
			- 클라이언트에서 불시에 GET 요청, 해당 요청에 따라 남는 자리 갯수를 보여줌.
			- (Firebase 구 GCM과 같은 기술 활용 가능 BUT 시간?)

	2. POST 처리: 매치 참가자 데이터 작성 √
		- 새로운 매치 또는 기존 매치에 참가하고자 하는 참가자에 대한 정보를 받아 처리 √
		- 처리함과 동시에 매치 리스트 업데이트, 중간에 작성중인 매치가 있다면 해당 클라이언트에 통보 √
		  (해당 매치는 이미 완료되었습니다) → 클라이언트에서 구현
		- 각 매치 참가자 데이터를 받아 위와 같이 Parse 한 뒤 MongoDB에 저장. √

	3. POST 처리: 로그인, 회원가입
		- 간부 사용자와 용사 사용자를 구분하여 인증
		- 인증 과정에서의 암호화 사용 (SHA) √
		- Passport 모듈 사용 가능 but 시간? x

- 클라이언트 사이드 (프런트엔드)
	1. 주 기능
		- 매일 지정시간까지 운동 종목에 대해서 요청을 받음.
		- 요청을 받고 같은 영내 사람들끼리 매치를 돌려줌

	2. 군번/pw로 로그인, 회원가입
		필요 정보: 소속대 (중대 단위?)

	3. 만들 액티비티
		- 로그인 창
		- 회원가입 창
			- 프로필로 받을 것:
				- 군번 = 아이디  
				- 비밀번호  
				- 이름/계급/성별  
				- 소속 (매칭용이므로 매우 중요)  
				- 매치 가능 부대  
				- 자기소개  
		- 메인 화면  
    		a. 현재 큐 상태 (큐 없음, 큐 잡는 중, 매치 잡힘)
			- 큐 없음의 경우 딱히 할거 없음
			- "큐 잡는 중"의 경우 큐 취소 기능 넣어야 (큐 신청 기한 전까지)
			- ?? 신청기한 지나면 취소 불가??

    		b. 큐 잡기 버튼  
    		c. 전투체육 일지 (달력 보여주기)  
    		d. 개인 프로필 / 수정  

		- 종목 선택 창 (메인에서 큐 잡기 버튼 눌러서 들어감)  
			a. 종목 찾기
			- 종목: 축구 족구 농구 커스텀 (커스텀:: 게시판 => 따로 액티비티)

			b. 종목 클릭시 밑에 인원수 + 희망 포지션 입력.
			- 대표자 외 n명, 농구/축구/족구 희망 포지션.

			c. 소속부대, 인원수, 큐 확인 팝업?
		- 매치 정보 (매치 잡혔을때 "매치 상태" 눌러서 들어감.)  
			- 매치 잡힌 사람 명단 (ListView) 및 클릭시 개인 공개 프로필.
			- 취소 및 확정 버튼
			- 매치 끝나고 나면 승/패 기능도?
		- 사람 프로필 보기 (매치 정보에서 눌러서 들어가기)
		- 프로필 수정 창 (회원가입과 유사)
	
	3-5. 알림

	4. 세션 매니지먼트 (로그인 상태 유지)
		- SharedPreference 활용
		https://www.androidhive.info/2012/08/android-session-management-using-shared-preferences/
		여기서 다 해준다!

- 클라이언트-서버 소통 프로토콜
	1. 로그인

		- 클라이언트 request: `/process/loginUser`으로 POST
		- 폼: id(String), password(String).
		- 서버 response:
			1. 성공 시  
				{"result":true,"id":"17-76001439","name":"안정인"}
			2. 실패 시 (아이디가 없음)  
				{"result":false,"reason":"NoSuchUserException"}
			3. 실패 시 (비밀번호가 다름)  
				{"result":false,"reason":"PasswordMismatch"}

		로그인 정보는 서버의 Session에 저장한다.
		Local의 쿠키가 접근 가능해야 함.

	2. 회원가입
		- 클라이언트 request:
			`/process/registerUser`으로 POST를 보낸다.
		- 폼:
			id 군번 (String)
			password 비번 (String)
			name 이름 (String)
			rank 계급 (숫자, 0부터 n까지) (Number)
			unit 소속부대 (중대까지) (String)
			gender 성별 (Number, 0은 남자, 1은 여자)
			favoriteEvent 좋아하는 종목(|으로 구분 or 배열로 구분) (String or Array)
			description 자기소개 (String)
		- 서버 response:
			1. 회원가입 성공시 (로그인 성공시와 동일)  
				{"result":true,"id":"17-76001439","name":"안정인"}
			2. 실패 시 (이미 있는 아이디)  
				{"result":false,"reason":"AlreadyExistingException"}
			3. 실패 시 (다 채워지지 않은 폼)  
				{"result":false,"reason":"MissingValuesException"}

	2-1. 회원가입 중복체크
	- 클라이언트 request:
		`/process/checkExistingUser` 로 POST를 보낸다. 폼은 id.
	- 서버 response: 
		{
			result: true|false (boolean)
		}

	3. 큐 신청
	클라이언트 request: POST (`/process/requestMatch`)
		폼:
		id 군번 (String)
		activityType 종목 (String)
		maxUsers 사람 숫자 (Number)
		matchId 매치 ID (기존 매치에 들어가기를 희망하는 경우)
		                (새로운 매치는 해당 없음)
		// position 희망 포지션 (??)
		부가기능: 군번 여러개 한번에 받아서 프로필 띄워주기.
	
	서버 response:
		{
			result: true|false (Boolean)
			failed_reason: 실패시 사유 (String)
			activityId: <해당 종목 session의 고유 ID> (String)
		}

	4. 자리 있는지 / queue 잡혔는지 확인
	클라이언트 request: GET (`/process/getMatchList`)
		폼:
		id 군번
		activityId 종목 고유 ID
	
	서버 response:

		{
			result: true|false (boolean)
		}


- 부가기능
	1. 이미 만들어진 매치로 등록, 경기장 예약 시스템
	2. 잡힌 "매치"에 서로 의견이나 댓글 달 수 있게 (몇시까지 모여주세요 등)
	3. 전적 / 전투체육 일지 매니저. 달력 혹은 타임라인 형태로 전투체육 기록을 봄. 뜀걸음의 경우 안드로이드가 뛴 거리 측정해줄수도 있음! 다른 사람 보고 그사람이랑 했던 전투체육 기록 쭉 나오기도 함?
	4. 탈주자 신고/처벌 시스템 (LeaverBuster) -> 며칠간 사용 제한, 수회 이상시 지휘관 구두경고 건의
	5. 커스텀 매치 게시판
	6. 지휘관/지휘자에게 부여하는 부대 vs 부대 매치 기능 (꿀잼각)
	7. 잡힌 매치에 공지하기 및 댓글달기

- 매치메이킹 알고리즘 고려요소
	1. 나이대가 비슷하게 나오도록 + 성비 맞춰주기 (혼성 yes? no?)
	2. 짬순 (중령급 이상은 웬만하면 경기 잡아주자)
	3. 개인별로 만나기 싫은 회원 밴 기능
	4. MMR / 실력 고려 배치 (전 경기 전적으로 판단)
		참조: https://www.google.co.kr/url?sa=t&rct=j&q=&esrc=s&source=web&cd=2&ved=0ahUKEwihlorgnPXWAhWBkpQKHbp5DPgQFgg4MAE&url=https%3A%2F%2Fwww.microsoft.com%2Fen-us%2Fresearch%2Fproject%2Ftrueskill-ranking-system%2F&usg=AOvVaw2_tTuMp0sa9GqP_KeoY_qR
		혹은 단순 ELO 시스템도 OK




- 총 정원의 갯수를 확인할 수 있어야 한다?
- 