package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Context;
import com.loopj.android.http.*;

import java.io.ByteArrayInputStream;

public class Proxy {
    public static final String SERVER_URL = "http://10.53.128.122:14402";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private Context context;
    private SessionManager smgr;

    public Proxy(Context context){
        this.context = context;
        smgr = new SessionManager(context);
    }

    /* POST loginUser
     * input - "id" : id, "pw" : pw
     * output
     * "result" : bool
     * "reason" : String containing reason: PasswordMismatch, NoSuchUserException, MultipleUserException
     */
    public void login(String id, String pw, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        String loginURL = SERVER_URL + "/process/loginUser";
        client.setCookieStore(smgr.myCookies);
        client.post(loginURL, params, handler);
    }

    //POST idCheck
    public void idCheck(String id, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id",id);
        String checkURL = SERVER_URL + "/process/checkExistingUser";
        client.post(checkURL, params, handler);
    }

    //POST signup
    public void signup(String id, String pw, String name, int rankid, String unit,
                       int sexid, String fav, String desc, byte[] byteImage,
                       JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        params.put("name", name);
        params.put("rank", rankid);
        params.put("unit", unit);
        params.put("gender",sexid);
        params.put("favoriteEvent",fav);
        params.put("description",desc);
        if (byteImage != null) {
            params.put("profPic", new ByteArrayInputStream(byteImage), "profPic.PNG");
        }

        String registerURL = SERVER_URL + "/process/registerUser";
        client.post(registerURL, params, handler);
    }

    /* GET getUserInfo
     * uses current session data (no param), returns user details (minus password).
     */
    public void getUserInfo(JsonHttpResponseHandler handler){
        String getInfoURL = SERVER_URL + "/process/getUserInfo";
        client.setCookieStore(smgr.myCookies);
        client.get(getInfoURL, handler);
    }

    /* POST getUserDetail
     * input - "id" : user_id (String)
     * output - JSONObject with the following parameters:
     * "results" : 성공 여부 (bool, 성공시 true)
     * "name" : 사용자 이름 (String)
     * "rank" : 사용자 계급 (int)
     * "profile_picture" : 프로필 사진 여부 (bool)
     * "reason" (실패시에) : 실패 이유 (String)
     */
    public void getUserDetail(String queryid, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", queryid);
        client.setCookieStore(smgr.myCookies);
        String searchURL = SERVER_URL + "/process/getUserDetails";
        client.post(searchURL, params, handler);
    }

    /*
     * POST getUsersDetails
     * output:
     * "result", "data" (JSONArray of users), "complete", "omittedUsers" (JSONArray of strings)
     */
    public void getUsersDetails(String[] users, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("users", users);
        client.setCookieStore(smgr.myCookies);
        String searchURL = SERVER_URL + "/process/getUsersDetails";
        client.post(searchURL, params, handler);
    }

    /*
     * POST updateUserInfo
     */
    public void updateUserInfo(String id, String pw, String name, int rankid, String unit,
                               int sexid, String fav, String desc, byte[] byteImage,
                                       JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        params.put("name", name);
        params.put("rank", rankid);
        params.put("unit", unit);
        params.put("gender",sexid);
        params.put("favoriteEvent",fav);
        params.put("description",desc);
        if (byteImage != null) {
            params.put("profPic", new ByteArrayInputStream(byteImage), "profPic.PNG");
        }
        client.setCookieStore(smgr.myCookies);
        String registerURL = SERVER_URL + "/process/updateUserInfo";
        client.post(registerURL, params, handler);
    }

    /*
     * Match related proxy functions
     */

    /*
     * POST requestMatch
     * creates a new match with gameType and participants.
     */

    public void requestMatch(String gameType, String participants, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("activityType", gameType);
        params.put("players", participants);
        client.setCookieStore(smgr.myCookies);
        String requestURL = SERVER_URL + ":"+ "/process/requestMatch";
        client.post(requestURL, params, handler);
    }

    /*
     * GET getUserMatch. TODO: get or post?
     * grabs current session and returns user's match information.
     * output parameters
     * "result": 결과 (true/false)
     * "match": 성공시 Match 정보를 담은 JSONObject.
        - activityType: 경기 종류
        - players: 플레이어 목록 (배열)
        - stadium: 경기장 이름
        - start_at: 매치 생성 시간
        - matchId: 매치 고유 ID
        - initiatorId: 매치 생성자 ID
        - is_pending: 사용자가 대기중인지 수락했는지 여부.
     * "reason": 실패 시 사유 (result = false)
        - NoSuchMatchException
        - NotLoggedInException
     */
    public void getUserMatch(JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        String getMatchURL = SERVER_URL + "/process/getUserMatch";
        client.setCookieStore(smgr.myCookies);
        client.get(getMatchURL, handler);
    }

    //POST delete match
    public void deleteMatch(String matchId, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("matchId", matchId);
        String quitMatchURL = SERVER_URL + "/process/deleteMatch";
        client.setCookieStore(smgr.myCookies);
        client.post(quitMatchURL, params, handler);
    }

    // POST decide whether to match or not
    public void decideMatch(String isParticipating, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("isParticipating", isParticipating);
        String decideMatchURL = SERVER_URL + "/process/decideMatch";
        client.setCookieStore(smgr.myCookies);
        client.post(decideMatchURL, params, handler);
    }

    /*
     * Profile picture related functions
     */

    //GET
    //request parameter: id (String)
    //response: file
    public void getProfPic(String userid, FileAsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("userid", userid);
        String getProfPicURL = SERVER_URL + "/process/getProfileImage";
        client.setCookieStore(smgr.myCookies);
        client.get(getProfPicURL, params, handler);
    }

    /*
     * Stadium related funtions
     */

    public void prepareMatchingTeamStadium(String stadium_name, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("name", stadium_name);
        String prepareStadiumURL = SERVER_URL + "/process/prepareMatchingTeamStadium";
        client.setCookieStore(smgr.myCookies);
        client.post(prepareStadiumURL, params, handler);
    }

    // 사용자의 부대 안에 있는 stadium을 전부 GET함.
    public void getUserStadium(JsonHttpResponseHandler handler){
        String getUserStadiumURL = SERVER_URL + "/process/getUserStadium";
        client.setCookieStore(smgr.myCookies);
        client.get(getUserStadiumURL, handler);
    }

}
