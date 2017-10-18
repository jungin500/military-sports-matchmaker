package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Context;
import com.loopj.android.http.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Proxy {
    public static final String SERVER_URL = "http://10.53.128.122:14402";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private Context context;
    private SessionManager smgr;

    public Proxy(Context context){
        this.context = context;
        smgr = new SessionManager(context);
    }

    //POST login
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
                        int sexid, String fav, String desc, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        params.put("name", name);
        params.put("rank", rankid);
        params.put("unit", unit);
        params.put("gender",sexid);
        params.put("favoriteEvent",fav);
        params.put("description",desc);

        String registerURL = SERVER_URL + "/process/registerUser";
        client.post(registerURL, params, handler);
    }

    public void signup(String id, String pw, String name, int rankid, String unit,
                       int sexid, String fav, String desc, String filePath,
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

        try {
            params.put("profPic", new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String registerURL = SERVER_URL + "/process/registerUser";
        client.post(registerURL, params, handler);
    }

    //GET getUserInfo, using only cookies
    public void getUserInfo(JsonHttpResponseHandler handler){
        String getInfoURL = SERVER_URL + "/process/getUserInfo";
        client.setCookieStore(smgr.myCookies);
        client.get(getInfoURL, handler);
    }

    //POST searchUser
    public void searchUser(String queryid, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", queryid);
        client.setCookieStore(smgr.myCookies);
        String searchURL = SERVER_URL + "/process/searchUserDetails";
        client.post(searchURL, params, handler);
    }

    //POST updateUserInfo
    public void updateUserInfo(String id, String pw, String name, int rankid, String unit,
                               int sexid, String fav, String desc, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        params.put("name", name);
        params.put("rank", rankid);
        params.put("unit", unit);
        params.put("gender",sexid);
        params.put("favoriteEvent",fav);
        params.put("description",desc);
        client.setCookieStore(smgr.myCookies);
        String registerURL = SERVER_URL + "/process/updateUserInfo";
        client.post(registerURL, params, handler);
    }

    //POST updateUserInfo with Profile Picture

    public void updateUserInfo(String id, String pw, String name, int rankid, String unit,
                               int sexid, String fav, String desc, String filePath,
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
        try {
            params.put("profPic", new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.setCookieStore(smgr.myCookies);
        String registerURL = SERVER_URL + "/process/updateUserInfo";
        client.post(registerURL, params, handler);
    }

    /*
     * Match related proxy functions
     */

    //POST requestMatch
    public void requestMatch(String gameType, String participants, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("activityType", gameType);
        params.put("players", participants);
        client.setCookieStore(smgr.myCookies);
        String requestURL = SERVER_URL + ":"+ "/process/requestMatch";
        client.post(requestURL, params, handler);
    }

    //GET get match that user is in (using session id)
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

    /*
     * Profile picture upload and download functions
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

}
