package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Session Managing methods using SharedPreferences
 * SharedPreferences also caches current match data upon POST to getUserMatch
 * and deletes data upon POST to deleteMatch.
 */

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    PersistentCookieStore myCookies;

    // SharedPreference identifier
    private static final String PREF_NAME = "pref_session";

    // Keys
    public static final String IS_LOGGEDIN = "IsLoggedIn";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String RANK = "rank";
    public static final String IN_MATCH = "inMatch";
    public static final String MATCH_ID = "matchId";

    // Session manager constructor. get application context as input.
    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        myCookies = new PersistentCookieStore(context);
    }

    // Create new session using id, name if valid.
    public void createSession(String id, String name, String rank) {
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putString(ID, id);
        editor.putString(NAME, name);
        editor.putString(RANK, rank);
        editor.apply();
    }

    // get login status
    // TODO: CHECK WITH SERVER
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGEDIN, false);
    }

    // get current user info: id, name, rank.
    public HashMap<String, String> getProfile(){
        if (!isLoggedIn()){
            return null;
        }
        HashMap<String, String> prof = new HashMap<String, String>();
        prof.put(ID, pref.getString(ID, null));
        prof.put(NAME, pref.getString(NAME, null));
        prof.put(RANK, pref.getString(RANK, null));
        return prof;
    }

    // clear session data
    public void clearSession(){
        editor.clear();
        editor.apply();
        myCookies.clear();
    }

    // if not logged in, close all activities and return to MainActivity.
    public void logout() {
        if (pref.getString(ID, null) != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            String logoutURL = Proxy.SERVER_URL + "/process/logoutUser";
            client.setCookieStore(myCookies);
            client.get(logoutURL, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    clearSession();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    clearSession();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
            });
        }
    }

    public void changeMatchStatus(boolean inMatch){
        editor.putBoolean(IN_MATCH, inMatch);
        editor.apply();
    }

    public void setMatchId(String id){
        editor.putString(MATCH_ID, id);
        editor.apply();
    }

    public boolean getMatchStatus(){
        return (pref != null) && pref.getBoolean(IN_MATCH, false);
    }


    public String getMatchId(){
        return pref.getString(MATCH_ID, null);
    }

    // from time to time, check login status and if not logged in, clear editor and logout.
    public void checkLogin(){
        checkSession();
        if (!isLoggedIn()){
            logout();
        }
    }

    // check whether session is live, kill everything if session offline.
    public void checkSession(){
        AsyncHttpClient client = new AsyncHttpClient();
        String checkLoginURL = Proxy.SERVER_URL + "/process/getUserInfo";
        client.setCookieStore(myCookies);
        client.get(checkLoginURL, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (!result){
                        Toast.makeText(context.getApplicationContext(), "세션 만료로 로그인 창으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                        clearSession();
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }
}