package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Session Managing methods using SharedPreferences
 */

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    // SharedPreference identifier
    private static final String PREF_NAME = "pref_session";

    // Keys
    public static final String IS_LOGGEDIN = "IsLoggedIn";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SID = "sid";

    // Session manager constructor
    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Create new session using id, name, SID if valid.
    public void createSession(String id, String name, String sid) {
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putString(ID, id);
        editor.putString(NAME, name);
        editor.putString(SID, sid);
        editor.apply();
    }

    // get login status
    // TODO: CHECK WITH SERVER
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGEDIN, false);
    }

    // get current user info: id, name, sid.
    public HashMap<String, String> getProfile(){
        if (!isLoggedIn()){
            return null;
        }
        HashMap<String, String> prof = new HashMap<String, String>();
        prof.put(ID, pref.getString(ID, null));
        prof.put(NAME, pref.getString(NAME, null));
        prof.put(SID, pref.getString(SID, null));
        return prof;
    }

    // clear session data
    public void clearSession(){
        editor.clear();
        editor.apply();
    }

    // if not logged in, close all activities and return to MainActivity.
    public void logout() {
        clearSession();

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    // from time to time, check login status and if not logged in, clear editor and logout.
    public void checkLogin(){
        if (!isLoggedIn()){
            logout();
        }
    }
}
