package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


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
    private Button quitMatchButton;
    private Button matching;
    private Button reserve;
    private Button note;
    private Button edit;
    private ImageView homepro;
    private ListView homeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // initialize session on create
        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());

        // define widgets
        logoutButton = (Button) findViewById(R.id.logout);
        textWelcome = (TextView) findViewById(R.id.home_welcome);
        quitMatchButton = (Button) findViewById(R.id.home_quitMatch);

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

        quitMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing if not in match
                if (!smgr.getMatchStatus()){
                    return;
                }
                String matchId = smgr.getMatchId();

                proxy.deleteMatch(matchId, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if(response.getBoolean("result")){
                                displayMatchStatus();
                            }
                            else{
                                String reason = response.getString("reason");
                                if (reason.equals("ForbiddenOperationException")){
                                    Toast.makeText(getApplicationContext(), "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                                else if (reason.equals("NoSuchMatchException")){
                                    Toast.makeText(getApplicationContext(), "진행중인 큐가 없습니다..", Toast.LENGTH_SHORT).show();
                                }
                                else if (reason.equals("NotLoggedInException")){
                                    Toast.makeText(getApplicationContext(), "로그인되어있지 않습니다.", Toast.LENGTH_SHORT).show();
                                    smgr.checkSession();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "실패했습니다. 오류 종류: " + reason, Toast.LENGTH_SHORT).show();
                                    Log.e("deleteMatch error", reason);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        // add adapter to listview. Long boring stuff, so factor into separate method.
        //homeMenu = (ListView) findViewById(R.id.home_menu);
        //setHomeMenu(homeMenu);
        //homeMenu.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.searchmatching:
                if (smgr.getMatchStatus()){
                    Toast.makeText(getApplicationContext(), "이미 시합 대기중이십니다.", Toast.LENGTH_SHORT).show();
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
            default:
                Toast.makeText(getApplicationContext(), "전우님의 전투체육 참여 현황을\n편리하게 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                Intent intent4 = new Intent(getApplicationContext(), SelectProfileActivity.class);
                startActivity(intent4);
                break;
            //임시 사진 선택
        }
    }

    private void displayMatchStatus(){
        textQStatus = (TextView) findViewById(R.id.home_qstatus);
        proxy.getUserMatch(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success) {
                        JSONObject match = response.getJSONObject("match");
                        String gameTypeEng = match.getString("activityType");
                        String gameTypeKor = "족구";
                        if (gameTypeEng.equals("football"))
                            gameTypeKor = "축구";
                        else if (gameTypeEng.equals("basketball"))
                            gameTypeKor = "농구";
                        JSONArray players = match.getJSONArray("players");
                        String numPlayers = String.valueOf(players.length());

                        //TODO: is IS_PENDING added?
                        textQStatus.setText("현재 " + numPlayers + "명과 " + gameTypeKor + " 시합 대기중입니다.");
                        smgr.changeMatchStatus(true);
                        smgr.setMatchId(match.getString("matchId"));

                        //TODO: GET RID OF THIS UGLY BUTTON
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) quitMatchButton.getLayoutParams();
                        params.weight = 0.25f;
                        quitMatchButton.setLayoutParams(params);
                    }
                    else {
                        String reason = response.getString("reason");
                        if (reason.equals("NoSuchMatchException")) {
                            textQStatus.setText("현재 대기중인 시합이 없습니다. 찾아보세요!");
                            smgr.changeMatchStatus(false);
                            smgr.setMatchId("null");
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) quitMatchButton.getLayoutParams();
                            params.weight = 0f;
                            quitMatchButton.setLayoutParams(params);
                        }
                        else {
                            textQStatus.setText("오류: " + reason);
                        }
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

    @Override
    // 회원정보 수정후 돌아옴.
    protected void onResume() {
        super.onResume();
        smgr.checkSession();
        updateTextWelcome();
        updateProfileImage();
    }

    private void updateTextWelcome(){
        prof = smgr.getProfile();
        String user_name = prof.get(SessionManager.NAME);
        String user_rank = prof.get(SessionManager.RANK);
        textWelcome.setText("환영합니다, " + user_name + " " + user_rank + "님");
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
                                    Log.e("TAG", String.valueOf(file.length()));
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
/*
    private void setHomeMenu(ListView homeMenu){
        ArrayList<HashMap<String, String>> hashMapMenuList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> menu1 = new HashMap<String, String>();
        menu1.put("line1", "전투체육 같이 할 사람 찾기");
        menu1.put("line2", "종목을 고르시면 자동으로 팀원과 상대방을 찾아드립니다.");
        hashMapMenuList.add(menu1);
        HashMap<String, String> menu2 = new HashMap<String, String>();
        menu2.put("line1", "전투체육 활동 등록 및 장소 예약");
        menu2.put("line2", "이미 사람을 다 모으셨나요? 장소를 잡아드립니다.");
        hashMapMenuList.add(menu2);
        HashMap<String, String> menu3 = new HashMap<String, String>();
        menu3.put("line1", "전투체육 일지");
        menu3.put("line2", "전우님의 전투체육 참여 현황을 편리하게 볼 수 있습니다.");
        hashMapMenuList.add(menu3);
        HashMap<String, String> menu4 = new HashMap<String, String>();
        menu4.put("line1", "프로필 수정");
        menu4.put("line2", "개인 프로필 정보를 변경합니다.");
        hashMapMenuList.add(menu4);
        String[] from = {"line1", "line2"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        menuAdapter = new SimpleAdapter(this, hashMapMenuList, android.R.layout.simple_list_item_2, from, to);
        homeMenu.setAdapter(menuAdapter);
    }
*/
/*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.home_menu:
                switch(position){
                    case 0:
                        if (smgr.getMatchStatus()){
                            Toast.makeText(getApplicationContext(), "이미 시합 대기중이십니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent(getApplicationContext(), ChooseSportActivity.class);
                        startActivity(intent);
                        break;
                    //TODO: 구현
                    case 1:
                        Intent intent1 = new Intent(getApplicationContext(), ReservePlaceActivity.class);
                        startActivity(intent1);
                        break;
                    //프로필 수정
                    case 3:
                        Intent intent3 = new Intent(getApplicationContext(), EditProfileActivity.class);
                        startActivity(intent3);
                        break;
                    //장소 고르기
                    default:
                        Intent intent4 = new Intent(getApplicationContext(), SelectProfileActivity.class);
                        startActivity(intent4);
                        break;
                    //임시 사진 선택
                }
                break;
        }
    }
    */

}
