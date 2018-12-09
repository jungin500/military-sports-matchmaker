# 전투적인 매치메이커 (전투체육 매칭 어플리케이션)
[![military-sports-matchmaker](https://img.youtube.com/vi/lZ-a35NXur4/0.jpg)](https://www.youtube.com/watch?v=lZ-a35NXur4)

## 개요
	군 전투체육 시간에, 같은 운동을 하고 싶은 사람들을 맺어주는 매칭 어플리케이션

그날 하고 싶은 운동을 선택하고 버튼 몇 번 누르고 기다리면 어플리케이션이 자동으로 영내의 사람들을 팀으로 맺이주고 운동장과 경기 시간을 배정해준다.

## 모티브
- 전투체육 시간 전후로 병사들은 구기종목 (축구, 족구, 농구 등) 운동을 할 인원을 찾아다니느라 시간을 많이 소비함
- 간부들의 경우, 구기종목에 관심이 많아도 부대 내 리그전 등이 아니면 전투체육 시간에 단체로 참여하거나, 참여할 사람을 모으는 일이 흔치 않음
- 인기 게임 (오버워치, 리그 오브 레전드)에서 버튼 몇번으로 자동으로 게임 상대방을 배정해주는 것에 착안

## 대상 장병
- 전투체육 시간에 축구를 하고 싶지만 사람이 모자라서 못 하는 병사들
- 특정 운동을 하고 싶지만 사무실 사람들만으로는 인원이 부족하고, 다른 사무실까지 가서 물어보기엔 눈치보이는 간부들
- 영내 타 부대원과 실력을 겨뤄보고 싶은 분대/소대/중대

## 주요 기능
- "매치메이킹": 축구/농구/족구 등 운동을 선택하고 "큐"에 들어가면 자동으로 그날 전투체육때 같이 게임할 영내 사람들을 "매치"하고 해당일 운동장을 배정한다.
	- 같은 팀으로 뛰고 싶은 사람들을 모아서 "그룹" 단위로 큐에 들어갈수도 있다.
- "운동장 예약": 축구장, 농구장 등 수요가 많은 운동장을 클릭 몇번으로 예약하는 기능을 제공.
- 군번으로 로그인, 한번 로그인하면 휴대폰을 꺼도 로그아웃 전까지 세션이 유지되어 편리
- 군내 DB와 연동하여 자동으로 "영내 운동장"들과 연동 (희망)
- 프로필 사진 업로드 (구현), 전투체육 일지 자동 관리 등 (구현 가능).

## 장점 및 기대 효과
1. 직관적인 UX: 이해하기 쉬운, "게임"같은 인터페이스
2. 실력 보정: 축적되는 경기 기록을 바탕으로 "실력"을 측정해서 비슷한 실력의 인원끼리 매치 가능
3. 시간 절약: 축구를 하기 위해 사람 22명을 모을 필요 없이 버튼 몇번으로 편리하게 참여 가능
4. 부대원 단결 도모: 분대/소대 단위 팀 구성 후 매치를 통해 부대원 단합 도모 및 선의의 경쟁 유도
5. 전투체육 활성화: 병사들 및 간부들의 적극적인 전투체육 참여 여건 조성, 군 장병 체력 증진에 기여

## 설치 (사용자)
1. 빌드된 `apk`파일을 안드로이드 기기에 설치한 뒤 홈에서 `전투체육 매치메이커` 어플리케이션 아이콘을 실행한다.

2. 가입한 뒤 로그인하여 동기나 지인들과 함께 이용한다. 매치가 완료되면 직접 들어가 확인할 수 있다.

3. 현재 DB에 다음과 같은 경기장이 존재한다. 해당하는 대대 이름으로 가입하면 해당 대대와 경기에 맞는(매치되는) 경기장으로 매칭이 진행된다.

	| 대대명 | 경기장  | 가능종목 |
	|:------:|:-------:|:-------- |
	| 1대대  | 경기장1 | 축구	   |
	| 1대대  | 경기장2 | 농구     |
	| 1대대  | 경기장3 | 족구     |

## 설치 (개발자)
### 서버 (Node.js 및 MongoDB 연동 방법)
1. node를 설정한다. (/server 폴더에서)

		npm install

2. 서버의 필수 프로그램들을 설치한다. 다음 패키지들을 npm을 이용하여 설치한다.

		npm install -g gulp pm2
		// Ubuntu에서 구동할 경우 앞에 sudo를 붙여줌으로써 root 권한으로 실행한다.

3. MongoDB 서버를 시작한다. Windows의 경우 다음 명령어를 입력하여 MongoD를 시작한다. **단, DB를 저장할 위치는 실제 존재하는 폴더여야 한다 (자동 생성하지 않는다).**

		mognod --dbpath <DB를 저장할 위치>

	또는 Ubuntu 환경에서(apt로 설치한 패키지)는 다음 명령어로 서버를 구동한다.

		sudo service mongod start

4. DB의 위치를 수정하기 위해 환경 변수로 다음과 같은 값을 지정한다.

		DB=mongod://localhost:27017/matching

5. Node.js 서버의 경우 `pm2`(packange manager)를 이용하여 구동한다

		pm2 start index.js --name "military-sports-matchmaker"

	또는, 환경변수를 적용한 1회성 구동을 위해 다음과 같이 할 수 있다.
	
		DB=mongod://localhost:27017/matching PORT=5012 node index.js

6. 홈페이지를 이용한 DB 관리를 진행할 수 있다. 다음 사이트에서 관리를 진행한다.

		http://<서버 IP주소>:5012/public/index.html

### 클라이언트
1. `client` 폴더를 Android Studio로 로드한 뒤, `client/app/src/main/java/kr/oss/sportsmatchmaker/militarysportsmatchmaker/Proxy.java`를 다음과 같이 수정한다.

		// 9번 줄
		public static final String SERVER_URL = "http://<서버를 실행한 IP 또는 도메인>:<포트>"

	로컬에서 테스팅 목적으로 실행할 목적이라면 (AVD 이용), 다음과 같이 설정할 수 있다.

		public static final String SERVER_URL = "http://localhost:포트"

2. 빌드한 APK Package를 설치한 뒤 실행한다.


--------

## 프로그래밍

- 역할 분담
	1. 김범수: Frontend 개발: client-server protocol, Java 담당
	2. 임대인: Frontend 개발: UX 디자인, xml / layout 담당
    3. 안정인: Backend 개발: 데이터베이스 개발 및 관리, client 요청 처리 담당

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



백엔드 아키텍쳐
데이터는 크게 3종류로 구성
- user
	회원가입으로 생성

- match
	user의 requestMatch POST로 생성
	stadium id를 속성으로 받음
	회원이 삭제 가능

- stadium
	고정 속성: 종목, 소속부대, 이름, 최대수용인원, 최소수용인원
	가변 속성: 현재인원, 현재 link된 모든 match id

백엔드에서 구현되어야 할 사항:
- requestMatch POST의 param에 stadium id를 넣으면,
	새로 생성한 match를 그 경기장에 넣음.

- getStadium POST (user가 자기 소속부대 안에 있는 운동장 찾는 것)
	input param: userid, activityType
	output:
			result : Boolean
			if success
				StadiumList: Array of Stadiums
			    userid의 부대에 있는 param activityType의 모든 운동장
			if fail
				reason : String

- getMatch POST (user가 현재 소속인 match 찾는 것)
	input param: userid
	output:
			result : boolean
			if success
				match : match (match 정보 전부 다)
				stadium : stadium (stadium 정보 전부 다)
			if fail
				reason : string

- quitMatch GET (user가 현재 소속인 match 취소하는 것)
	input param: userid
	output:
			result: boolean
			if success nothing
			if fail
				reason: string

포트 번호
- 50+자기번호
- 팀 대표 번호 1명
