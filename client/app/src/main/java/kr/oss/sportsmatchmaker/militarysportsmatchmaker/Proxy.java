package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.util.Log;

import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.*;
import java.lang.Object;

import java.net.HttpURLConnection;
import java.net.URL;

public class Proxy {
    public static final String SERVER_URL = "http://10.53.128.122";
    public static final String SERVER_PORT = "14402";

    private static AsyncHttpClient client;



    public void login(String id, String pw){
        client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("password", pw);
        String loginURL = SERVER_URL + ":" + SERVER_PORT + "/process/loginUser";
        client.post(loginURL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    boolean success = response.getBoolean("result");
                    if (success) {
                        String id = response.getString("id");
                        String name = response.getString("name");
                    }
                    else {
                        String reason = response.getString("reason");
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

        return;
    }

}
