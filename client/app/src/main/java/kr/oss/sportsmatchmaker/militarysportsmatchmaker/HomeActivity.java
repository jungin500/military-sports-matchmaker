package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.*;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;


public class HomeActivity extends AppCompatActivity implements OnClickListener {

    //Helper
    private SessionManager smgr;
    private Proxy proxy;
    private HashMap<String, String> prof;
    private SimpleAdapter menuAdapter;

    //Widgets
    private TextView textWelcome;
    private Button logoutButton;
    private TextView textQStatus;
    private Button matching;
    private Button reserve;
    private Button note;
    private Button edit;
    private ImageView homepro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        // initialize session on create
        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());

        // define widgets
        logoutButton = (Button) findViewById(R.id.logout);
        textWelcome = (TextView) findViewById(R.id.home_welcome);

        matching = (Button) findViewById(R.id.searchmatching);
        matching.setOnClickListener(this);

        reserve = (Button) findViewById(R.id.reserveplace);
        reserve.setOnClickListener(this);

        note = (Button) findViewById(R.id.notepad);
        note.setOnClickListener(this);

        edit = (Button) findViewById(R.id.profileedit);
        edit.setOnClickListener(this);

        final String id = smgr.getProfile().get(SessionManager.ID);
        homepro = (ImageView) findViewById(R.id.homeprofile);
        homepro.setImageResource(R.drawable.img_defaultface);

        // get image and set it. if no image, set default image.
        updateTextWelcome();
        updateProfileImage();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smgr.logout();
                finish();
            }
        });

        // queue status message
        displayMatchStatus();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.searchmatching:
                if (smgr.getMatchStatus()){
                    Toast.makeText(getApplicationContext(), "현재 큐의 승락상태를 확인합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent4 = new Intent(getApplicationContext(), QueListActivity.class);
                    startActivity(intent4);
                    return;
                }
                Toast.makeText(getApplicationContext(), "종목을 고르시면 \n자동으로 팀원과 상대방을 찾아드립니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ChooseSportActivity.class);
                startActivity(intent);
                break;
            case R.id.reserveplace:
                Toast.makeText(getApplicationContext(), "이미 사람을 다 모으셨나요? \n장소를 잡아드립니다.", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(getApplicationContext(), ReservePlaceActivity.class);
                startActivity(intent1);
                break;
            //프로필 수정
            case R.id.profileedit:
                Toast.makeText(getApplicationContext(), "개인 프로필 정보를 변경합니다.", Toast.LENGTH_SHORT).show();
                Intent intent3 = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent3);
                break;
            //장소 고르기
            case R.id.notepad:
                Toast.makeText(getApplicationContext(), "임시..", Toast.LENGTH_SHORT).show();
                Intent intent5 = new Intent(getApplicationContext(), MatchCompleteActivity.class);
                startActivity(intent5);
                break;
            default:
                break;//임시 사진 선택
        }
    }

    // 매치 상태를 보고 UI를 바꿔준다.
    private void displayMatchStatus(){
        textQStatus = (TextView) findViewById(R.id.home_qstatus);
        final String id = smgr.getProfile().get(SessionManager.ID);

        proxy.getUserInfo(new JsonHttpResponseHandler(){
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    final String match_status = response.getString("match_status");
                    // Case 1: 매치=큐가 없다=대기중이지 않다.
                    if (match_status.equals("ready")){
                        textQStatus.setText("현재 대기중인 시합이 없습니다. \n큐에 들어가보세요!");
                        smgr.changeMatchStatus(false, null);
                        smgr.changeStadiumName(null);
                        matching.setText("전투체육 같이 할 사람 찾기");
                        matching.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                    }
                    // Case 2: 대기중인 매치가 있다. (수락 여부는 밑에서)
                    else {
                        matching.setText("큐 상태 보기");
                        int colorCode = Color.parseColor("#000099") ;
                        matching.setBackgroundColor(colorCode);
                        proxy.getUserMatch(new JsonHttpResponseHandler(){
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    boolean success = response.getBoolean("result");
                                    if (success) {
                                        JSONObject match = response.getJSONObject("match");
                                        // set match status and match id on session manager.
                                        smgr.changeMatchStatus(true, match.getString("matchId"));
                                        JSONObject stadium = match.getJSONObject("stadium");
                                        String stadium_name = stadium.getString("name");
                                        smgr.changeStadiumName(stadium_name);

                                        // Case 2의 코드 부분.
                                        JSONArray acceptPlayers = match.getJSONArray("players");
                                        JSONArray pendingPlayers = match.getJSONArray("pendingPlayers");
                                        int accnum = acceptPlayers.length();
                                        int pendnum = pendingPlayers.length();

                                        // Case 2-1. 매치 수락 대기중이다.
                                        if (match_status.equals("pending")){
                                            textQStatus.setText("큐 초대가 있습니다.\n큐 상태를 확인하고 수락/거절 여부를 선택해주세요.");
                                        }
                                        // Case 2-2. 매치를 수락했고 수락 대기인원이 있다.
                                        else if (pendnum > 0){
                                            textQStatus.setText(String.valueOf(accnum + pendnum) + "명 중 " + String.valueOf(pendnum) + "명이 수락 대기중입니다.\n큐 상태를 확인하세요.");
                                        }
                                        // Case 2-3. 매치를 수락했고 수락 대기인원이 없다 => 경기 찾는 중이다.
                                        else {
                                            textQStatus.setText("경기를 찾는 중입니다.\n큐 상태를 확인하세요.");
                                        }
                                        proxy.prepareMatchingTeamStadium(smgr.getStadiumName(), new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                try {
                                                    boolean result = response.getBoolean("result");
                                                    // Case 3. 경기를 찾았다!!!
                                                    if (result) {
                                                        textQStatus.setText("경기를 찾았습니다!\n경기 정보를 확인하세요.");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        Log.e("TAG", "DATA CORRUPT; match not ready but no match");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "데이터 오류입니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                textQStatus.setText("매치 정보를 가져오지 못했습니다. 다시 접속해주세요.");
                            }

                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    // 다시 홈으로 돌아온 경우.
    protected void onResume() {
        super.onResume();
        smgr.checkSession();
        updateTextWelcome();
        updateProfileImage();
        displayMatchStatus();
    }

    private void updateTextWelcome(){
        prof = smgr.getProfile();
        String user_name = prof.get(SessionManager.NAME);
        String user_rank = prof.get(SessionManager.RANK);
        textWelcome.setText("환영합니다, " + user_name + " " + user_rank + "님.\n오늘도 즐거운 하루 되세요!");
    }

    private void updateProfileImage(){
        // get image and set it. if no image, set default image.
        proxy.getUserInfo(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success){
                        if (response.getBoolean("profile_image")){
                            proxy.getProfPic(response.getString("id"), new FileAsyncHttpResponseHandler(getApplicationContext()) {
                                public void onSuccess(int i, Header[] headers, File file){
                                    String filePath = file.getAbsolutePath();
                                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                                    homepro.setImageDrawable(rbd);
                                }
                                @Override
                                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                    Log.e("TAG", "Error: file open failed");
                                }
                            });
                        }
                        else {
                            homepro.setImageResource(R.drawable.img_defaultface);
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "회원정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
