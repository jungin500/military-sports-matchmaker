# TODO

### 제출 D-Day, 발표 D-1 아침
1. [O] 사용자가 현재 속한 stadium의 인원수 구하기 (최대인원, 현재인원)
2. [O] stadium에서 (activityType, unit) 검색 후 결과에 대해 현재원, 최대 인원 구하기
3. [O] getUserMatch 시 stadiumId와 stadium의 현재원, 총원 구하기
4. [O] 초대 거절 시 `in_players`의 수가 줄어들고 player의 match_ongoing을 삭제함. 그리고 player는 rejected_players로 이동함.

### 점심 (~14:00)
1. [O] 사용자가 requestMatch 할때, players + pendingPlayers 수만큼 stadium의 현재원 수를 늘림
2. [O] decideMatch할 경우 stadium의 현재원을 해당 플레이어의 결정에 따라 늘리거나 줄임.
3. [O] stadium이 모두 채워졌을 경우, requestMatch의 result parameter로,
       and 다른 사람의 경우엔 요청시(/process/prepareMatchingTeamStadium)
       result parameter로 전달 (및 PUSH로 해당 결과 전송 - 추후 구현)

### 오후 (~16:00)
1. [I] 디버깅 작업
2. [ ] 문서화 작업
3. [X] Heroku 이용 클라우드에 업로드/테스트 과정
4. [X] 로그 및 출력 구조화
5. [I] 디버깅 (클라이언트) 작업

### 저녁 (~18:00)
1. 문서 제출 준비z
- github.com/osam2017/{ProjectName}_{Name1}[,_{Name2}, ...]
- 내일 10시까지 제출


### 작년도 문서 분석
- 기능, 작동 여부 외 어플리케이션 활용 가치와 등장 계기
- README.md에 제출해도 되고 text로 제출해도 됨
- 아무도 하지 않았기 때문에?
- "API를 이용해서 만들었기 때문에"?
- YouTube에 특정 ID로 동영상 업로드