package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.*;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MatchSettingActivity extends AppCompatActivity {

    // current session
    private SessionManager smgr;
    private Proxy proxy;

    // declare widgets
    private ArrayList<ListData> listDataArray;
    private EditText playerNumber;
    private CheckBox sameTeam;
    private Button enterQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match_setting);

        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());

        // get client id
        final String id = smgr.getProfile().get(SessionManager.ID);
        final String name = smgr.getProfile().get(SessionManager.NAME);
        final String rank = smgr.getProfile().get(SessionManager.RANK);
        final String rankname = rank + " " + name;
        final String anonName = "anon_"+id;
        // initialize widgets
        final EditText playerNumber = (EditText) findViewById(R.id.playerNumber);
        Button playerShow = (Button) findViewById(R.id.player_show);
        sameTeam = (CheckBox) findViewById(R.id.is_Team);
        Button enterQueue = (Button) findViewById(R.id.enterQueue);

        Intent intent = getIntent();
        final int maxPlayer = intent.getIntExtra(ChooseSportActivity.EXTRA_PNUM, 0);
        final String gameType = intent.getStringExtra(ChooseSportActivity.EXTRA_SPORTTYPE);

        final int[] numPlayer = {0};

        listDataArray = new ArrayList<ListData>();

        ListView listview = (ListView) findViewById(R.id.listview1);
        final CustomAdapter customAdapter = new CustomAdapter(this, R.layout.list_btn_sty, listDataArray);
        listview.setAdapter(customAdapter);

        playerShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String numstr = playerNumber.getText().toString();
                if (numstr.equals("")){
                    playerNumber.setError("숫자를 입력하세요.");
                    return;
                }
                int num = Integer.parseInt(playerNumber.getText().toString());
                if (num <= 0){
                    playerNumber.setError("양수를 입력하세요.");
                    return;
                }
                else if (num > maxPlayer) {
                    playerNumber.setError("사람 숫자가 너무 많습니다.");
                    return;
                }
                // 첫 플레이어는 항상 내 자신, 수정 불가능.
                else if (numPlayer[0] == 0){
                    ListData data = new ListData(BitmapFactory.decodeResource(getResources(), R.drawable.img_defaultface), rankname, id, "방장");
                    listDataArray.add(data);
                    for (int i = 1; i < num; i++){
                        data = new ListData(BitmapFactory.decodeResource(getResources(), R.drawable.img_defaultface), rankname + "의 동료", anonName, "선수 추가\n(선택)");
                        listDataArray.add(data);
                    }
                }
                else if (num < numPlayer[0]){
                    for (int i = numPlayer[0] - 1; i >= num; i--){
                        listDataArray.remove(i);
                    }
                }
                else {
                    for (int i = 0; i < num - numPlayer[0]; i++){
                        ListData data = new ListData(BitmapFactory.decodeResource(getResources(), R.drawable.img_defaultface), rankname + "의 동료", anonName, "선수 추가\n(선택)");
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
                if (numPlayer[0] == 0){
                    Toast.makeText(getApplicationContext(), "사람 숫자를 지정해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();

                int anoncount = 0;
                // anon을 맨 뒤로 보내서 더한다.
                for (int i = 0; i < numPlayer[0]; i++){
                    String currid = listDataArray.get(i).getId();
                    if (currid.equals(anonName)){
                        anoncount++;
                    }
                    else {
                        stringBuilder.append(listDataArray.get(i).getId()+"|");
                    }
                }
                for (int i = 0; i < anoncount; i++){
                    stringBuilder.append(anonName+"|");
                }
                stringBuilder.setLength(stringBuilder.length() - 1);
                requestMatch(gameType, stringBuilder.toString(), sameTeam.isChecked());
            }
        });
    }

    // current session player (checked with cookie) queue for game gameType, with participant array .
    private void requestMatch(String gameType, String participants, boolean is_team) {
        proxy.requestMatch(gameType, participants, is_team, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success) {
                        Toast.makeText(getApplicationContext(), "큐 진입 성공!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        });
    }
}
