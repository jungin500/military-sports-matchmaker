package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Session Managing methods using SharedPreferences
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

    // Session manager constructor. get application context as input.
    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        myCookies = new PersistentCookieStore(context);
    }

    // Create new session using id, name if valid.
    public void createSession(String id, String name) {
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putString(ID, id);
        editor.putString(NAME, name);
        editor.apply();
    }

    // get login status
    // TODO: CHECK WITH SERVER
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGEDIN, false);
    }

    // get current user info: id, name.
    public HashMap<String, String> getProfile(){
        if (!isLoggedIn()){
            return null;
        }
        HashMap<String, String> prof = new HashMap<String, String>();
        prof.put(ID, pref.getString(ID, null));
        prof.put(NAME, pref.getString(NAME, null));
        return prof;
    }

    // clear session data
    public void clearSession(){
        editor.clear();
        editor.apply();
    }

    // if not logged in, close all activities and return to MainActivity.
    public void logout() {
        if (pref.getString(ID, null) != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            String logoutURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/logoutUser";
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
                    // LOGOUT FAILED

                }
            });


        }



    }

    // from time to time, check login status and if not logged in, clear editor and logout.
    public void checkLogin(){
        if (!isLoggedIn()){
            logout();
        }
    }
}
