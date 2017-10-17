package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
<<<<<<< HEAD
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
=======
import android.widget.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

>>>>>>> 59b5d425ff663d10ca5091f9bf0f1170838c2f62
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MatchSettingActivity extends AppCompatActivity {

    // current session
    private SessionManager smgr;

    // declare widgets
    private ArrayList<ListData> listDataArray;
    private EditText playerNumber;
    private CheckBox sameTeam;
    private Button enterQueue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        smgr = new SessionManager(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setting);

        Intent intent = getIntent();
        final int playernum = intent.getIntExtra(ChooseSportActivity.EXTRA_PNUM, 0);
        final String gameType = intent.getStringExtra(ChooseSportActivity.EXTRA_SPORTTYPE);
        String[] name = null;

<<<<<<< HEAD
        for(int i =1;i<=playernum;i++){
            ListData data = new ListData("01.jpg", "선수 추가", "");
=======
        // initialize
        listDataArray =  new ArrayList<ListData>();
        for(int i =0;i<playernum;i++){
            ListData data = new ListData("0"+i+".jpg", i+"name", i+"textview");
>>>>>>> 59b5d425ff663d10ca5091f9bf0f1170838c2f62
            listDataArray.add(data);
        }

        ListView listview = (ListView) findViewById(R.id.listview1);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.list_btn_sty, listDataArray);
        listview.setAdapter(customAdapter);
<<<<<<< HEAD
=======

        final EditText playerNumber = (EditText) findViewById(R.id.playerNumber);
        CheckBox sameTeam = (CheckBox) findViewById(R.id.sameTeam);
        Button enterQueue = (Button) findViewById(R.id.enterQueue);


        enterQueue.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pNum = Integer.parseInt(playerNumber.getText().toString());
                if (pNum > playernum) {
                    Toast.makeText(getApplicationContext(), "사람 숫자가 너무 많습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    joinQueue(smgr.getProfile().get(SessionManager.ID), gameType, pNum);
                }

            }
        });

>>>>>>> 59b5d425ff663d10ca5091f9bf0f1170838c2f62
    }
}

/*
    public String[] setnum(String[] player ,int num){
        player = new String[num];

        for(int i=0;i<num;i++){
            player[i] = "선수추가"+i;
        }

        return player;
<<<<<<< HEAD
    }*/
=======
    }

    // player with ID id joins the queue for game gameType, with player numbers pNum.
    private void joinQueue(String id, String gameType, int pNum) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("activityType", gameType);
        params.put("number", pNum);
        client.setCookieStore(smgr.myCookies);
        String queueURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/requestMatch";
        client.post(queueURL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success) {
                        Toast.makeText(getApplicationContext(), "큐 진입 성공!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), response.getString("reason"), Toast.LENGTH_SHORT).show();
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
>>>>>>> 59b5d425ff663d10ca5091f9bf0f1170838c2f62
