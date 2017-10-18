package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

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

        // get client id
        final String id = smgr.getProfile().get(SessionManager.ID);

        // initialize widgets
        final EditText playerNumber = (EditText) findViewById(R.id.playerNumber);
        Button playerShow = (Button) findViewById(R.id.player_show);
        CheckBox sameTeam = (CheckBox) findViewById(R.id.sameTeam);
        Button enterQueue = (Button) findViewById(R.id.enterQueue);

        Intent intent = getIntent();
        final int maxPlayer = intent.getIntExtra(ChooseSportActivity.EXTRA_PNUM, 0);
        final String gameType = intent.getStringExtra(ChooseSportActivity.EXTRA_SPORTTYPE);

        final int[] numPlayer = {0};

        String[] name = null;
        listDataArray = new ArrayList<ListData>();

        ListView listview = (ListView) findViewById(R.id.listview1);
        final CustomAdapter customAdapter = new CustomAdapter(this, R.layout.list_btn_sty, listDataArray);
        listview.setAdapter(customAdapter);

        playerShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String numstr = playerNumber.getText().toString();
                if (numstr == ""){
                    playerNumber.setError("숫자를 입력하세요.");
                    return;
                }
                int num = Integer.parseInt(playerNumber.getText().toString());
                if (num > maxPlayer) {
                    Toast.makeText(getApplicationContext(), "사람 숫자가 너무 많습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (num < numPlayer[0]){
                    for (int i = numPlayer[0] - 1; i >= num; i--){
                        listDataArray.remove(i);
                    }
                }
                else {
                    for (int i = 0; i < num - numPlayer[0]; i++){
                        ListData data = new ListData("img_defaultface.png", "선수를 추가시켜주세요.", "", "정보 입력\n(선택)");
                        listDataArray.add(data);
                    }
                }
                numPlayer[0] = num;
                customAdapter.notifyDataSetChanged();
            }
        });


        enterQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(id);
                for (int i = 1; i < numPlayer[0]; i++){
                    stringBuilder.append("|" + id);
                }
                requestMatch(gameType, stringBuilder.toString());
            }
        });
    }

    // current session player (checked with cookie) queue for game gameType, with participant array .
    private void requestMatch(String gameType, String participants) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("activityType", gameType);
        params.put("players", participants);
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
                Toast.makeText(getApplicationContext(), "큐 잡기를 실패했습니다. 금방 고치겠습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
