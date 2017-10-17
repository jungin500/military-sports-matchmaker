백엔드 아키텍쳐
데이터는 크게 3종류로 구성
- user
	회원가입으로 생성

- match
	user의 requestMatch POST로 생성
	stadium id를 속성으로 받음
	회원이 삭제 가능

- stadium
    stadium은 고정 (새로 생성이나 삭제 x), 속성은 변경 가능
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