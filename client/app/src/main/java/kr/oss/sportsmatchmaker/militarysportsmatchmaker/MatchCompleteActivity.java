package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.*;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;


public class MatchCompleteActivity extends AppCompatActivity{

    SessionManager smgr;
    Proxy proxy;


    //widget
    ImageView buttonTeam1;
    ImageView buttonTeam2;
    ListView list1;
    ListView list2;

    // list
    ArrayList<ListData2> leftTeamUsers;
    ArrayList<ListData2> rightTeamUsers;


    // flag
    boolean numdiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match_complete);

        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());
        numdiff = false;

        final TextView stadium = (TextView) findViewById(R.id.stadium);
        final TextView teamName1 = (TextView) findViewById(R.id.teamname1);
        final TextView teamProfile1 = (TextView) findViewById(R.id.teamprofile1);
        final TextView teamName2 = (TextView) findViewById(R.id.teamname2);
        final TextView teamProfile2 = (TextView) findViewById(R.id.teamprofile2);

        buttonTeam1 = (ImageView) findViewById(R.id.team1);
        buttonTeam2 = (ImageView) findViewById(R.id.team2);

        list1 = (ListView) findViewById(R.id.team1_list);
        list2 = (ListView) findViewById(R.id.team2_list);

        leftTeamUsers = new ArrayList<ListData2>();
        rightTeamUsers = new ArrayList<ListData2>();

        final CustomAdapter2 leftAdapter = new CustomAdapter2(this, R.layout.list_btn_sty, leftTeamUsers);
        final CustomAdapter2 rightAdapter = new CustomAdapter2(this, R.layout.list_btn_sty, rightTeamUsers);
        list1.setAdapter(leftAdapter);
        list2.setAdapter(rightAdapter);


        proxy.prepareMatchingTeamStadium(smgr.getStadiumName(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    // 꽉 차서 매칭 성공
                    if (result) {
                        stadium.setText("장소: " + smgr.getStadiumName() + ", 시간: 2017년 10월 20일 16시 00분");

                        JSONArray leftTeam = response.getJSONArray("leftTeam");
                        JSONArray rightTeam = response.getJSONArray("rightTeam");

                        String myid = smgr.getProfile().get(SessionManager.ID);
                        boolean myTeamisLeft = false;
                        for (int i = 0; i < leftTeam.length(); i++){
                            JSONArray leftTeami = leftTeam.getJSONObject(i).getJSONArray("players");
                            for (int j = 0; j < leftTeami.length(); j++){
                                String currPlayer = leftTeami.getString(j);
                                if (myid.equals(currPlayer))
                                    myTeamisLeft = true;
                            }
                        }
                        if (! myTeamisLeft){
                            JSONArray temp = leftTeam;
                            leftTeam = rightTeam;
                            rightTeam = temp;
                        }

                        // temporary arraylist storing users
                        ArrayList<String> leftTeamPlayers = new ArrayList<String>();
                        ArrayList<String> rightTeamPlayers = new ArrayList<String>();
                        final ArrayList<String> leftTeamAnons = new ArrayList<String>();
                        final ArrayList<String> rightTeamAnons = new ArrayList<String>();

                        for (int i = 0; i < leftTeam.length(); i++) {
                            JSONArray leftTeami = leftTeam.getJSONObject(i).getJSONArray("players");
                            for (int j = 0; j < leftTeami.length(); j++) {
                                String currPlayer = leftTeami.getString(j);
                                if (currPlayer.split("_")[0].equals("anon")) {
                                    leftTeamAnons.add(currPlayer);
                                }
                                else {
                                    leftTeamPlayers.add(currPlayer);
                                }
                            }
                        }

                        for (int i = 0; i < rightTeam.length(); i++) {
                            JSONArray rightTeami = rightTeam.getJSONObject(i).getJSONArray("players");
                            for (int j = 0; j < rightTeami.length(); j++) {
                                String currPlayer = rightTeami.getString(j);
                                if (currPlayer.split("_")[0].equals("anon")) {
                                    rightTeamAnons.add(currPlayer);
                                }
                                else {
                                    rightTeamPlayers.add(currPlayer);
                                }
                            }
                        }
                        final String[] leftArray = leftTeamPlayers.toArray(new String[leftTeamPlayers.size()]);
                        final String[] rightArray = rightTeamPlayers.toArray(new String[rightTeamPlayers.size()]);

                        final int leftTotalNum = leftArray.length + leftTeamAnons.size();
                        final int rightTotalNum = rightArray.length + rightTeamAnons.size();

                        numdiff = (leftTotalNum != rightTotalNum);
                        // leftTeam UI 불러오기
                        proxy.getUsersDetails(leftArray, new JsonHttpResponseHandler(){
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    if (response.getBoolean("result")){
                                        if (!response.getBoolean("complete")){
                                            Log.e("TAG", "data corrupt: complete gives false on existing ids");
                                        }
                                        JSONArray userData = response.getJSONArray("data");
                                        for (int i=0; i < leftArray.length; i++){
                                            String currid = userData.getJSONObject(i).getString("id");
                                            String currrankname = RankHelper.intToRank(userData.getJSONObject(i).getInt("rank")) + " " + userData.getJSONObject(i).getString("name");
                                            boolean existProfPic = userData.getJSONObject(i).getBoolean("profile_image");
                                            String userStatus = userData.getJSONObject(i).getString("match_status");
                                            ListData2 listData = new ListData2(existProfPic, currrankname, currid, "");
                                            leftTeamUsers.add(listData);
                                        }
                                        // add anons
                                        for (int j = 0; j < leftTeamAnons.size(); j++){
                                            String currAnon = leftTeamAnons.get(j);
                                            ListData2 listData = new ListData2(false, currAnon.split("_")[1]+ " 의 동료", currAnon, "");
                                            leftTeamUsers.add(listData);
                                        }

                                        Collections.sort(leftTeamUsers, new ListData2.data2RankComparator());
                                        leftAdapter.notifyDataSetChanged();
                                        // 리더 UI 초기화.

                                        final String leftTeamLeader = leftTeamUsers.get(0).getId();

                                        // leftTeam 리더 UI 초기화.
                                        proxy.getUserDetail(leftTeamLeader, new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                try {
                                                    boolean result = response.getBoolean("result");
                                                    if (result){
                                                        String leaderRank = RankHelper.intToRank(response.getInt("rank"));
                                                        String leaderName = response.getString("name");

                                                        teamName1.setText("주장: " + leaderRank + " " + leaderName);
                                                        teamProfile1.setText("총원: " + String.valueOf(leftTotalNum) + "명");

                                                        // add picture and notifyDataSetChanged();
                                                        if (response.getBoolean("profile_image")){
                                                            proxy.getProfPic(leftTeamLeader, new FileAsyncHttpResponseHandler(getApplicationContext()) {
                                                                public void onSuccess(int i, Header[] headers, File file){
                                                                    String filePath = file.getAbsolutePath();
                                                                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                                                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                                                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                                                                    buttonTeam1.setImageDrawable(rbd);
                                                                }
                                                                @Override
                                                                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                                                    Log.e("TAG", "Error: file open failed");
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            buttonTeam1.setImageResource(R.drawable.img_defaultleftteam);
                                                        }
                                                    }
                                                    else {
                                                        String errorName = response.getString("reason");
                                                        if (errorName.equals("NoSuchUserException")){
                                                            Toast.makeText(getApplicationContext(), "군번이 없습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else if (errorName.equals("NotLoggedInException")){
                                                            Toast.makeText(getApplicationContext(), "세션이 만료되었습니다. 다시 로그인해주십시오.", Toast.LENGTH_SHORT).show();
                                                            smgr.logout();
                                                        }
                                                        Log.e("TAG1", "ERROR");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        Log.e("TAG", "getUsersDetails failed");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // Right Team UI 초기화
                        proxy.getUsersDetails(rightArray, new JsonHttpResponseHandler(){
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    if (response.getBoolean("result")){
                                        if (!response.getBoolean("complete")){
                                            Log.e("TAG", "data corrupt: complete gives false on existing ids");
                                        }
                                        JSONArray userData = response.getJSONArray("data");
                                        for (int i=0; i < rightArray.length; i++){
                                            String currid = userData.getJSONObject(i).getString("id");
                                            String currrankname = RankHelper.intToRank(userData.getJSONObject(i).getInt("rank")) + " " + userData.getJSONObject(i).getString("name");
                                            boolean existProfPic = userData.getJSONObject(i).getBoolean("profile_image");
                                            String userStatus = userData.getJSONObject(i).getString("match_status");
                                            ListData2 listData = new ListData2(existProfPic, currrankname, currid, "");
                                            rightTeamUsers.add(listData);
                                        }
                                        // add anons
                                        for (int j = 0; j < rightTeamAnons.size(); j++){
                                            String currAnon = rightTeamAnons.get(j);
                                            ListData2 listData = new ListData2(false, currAnon.split("_")[1]+ " 의 동료", currAnon, "");
                                            rightTeamUsers.add(listData);
                                        }

                                        Collections.sort(rightTeamUsers, new ListData2.data2RankComparator());
                                        rightAdapter.notifyDataSetChanged();
                                        // 리더 UI 초기화.

                                        final String rightTeamLeader = rightTeamUsers.get(0).getId();

                                        // rightTeam 리더 UI 초기화.
                                        proxy.getUserDetail(rightTeamLeader, new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                try {
                                                    boolean result = response.getBoolean("result");
                                                    if (result){
                                                        String leaderRank = RankHelper.intToRank(response.getInt("rank"));
                                                        String leaderName = response.getString("name");

                                                        teamName2.setText("주장: " + leaderRank + " " + leaderName);
                                                        teamProfile2.setText("총원: " + String.valueOf(rightTotalNum) + "명");

                                                        // add picture and notifyDataSetChanged();
                                                        if (response.getBoolean("profile_image")){
                                                            proxy.getProfPic(rightTeamLeader, new FileAsyncHttpResponseHandler(getApplicationContext()) {
                                                                public void onSuccess(int i, Header[] headers, File file){
                                                                    String filePath = file.getAbsolutePath();
                                                                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                                                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                                                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                                                                    buttonTeam2.setImageDrawable(rbd);
                                                                }
                                                                @Override
                                                                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                                                    Log.e("TAG", "Error: file open failed");
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            buttonTeam2.setImageResource(R.drawable.img_defaultrightteam);
                                                        }
                                                    }
                                                    else {
                                                        String errorName = response.getString("reason");
                                                        if (errorName.equals("NoSuchUserException")){
                                                            Toast.makeText(getApplicationContext(), "군번이 없습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else if (errorName.equals("NotLoggedInException")){
                                                            Toast.makeText(getApplicationContext(), "세션이 만료되었습니다. 다시 로그인해주십시오.", Toast.LENGTH_SHORT).show();
                                                            smgr.logout();
                                                        }
                                                        Log.e("TAG1", "ERROR");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        Log.e("TAG", "getUsersDetails failed");
                                        // run if all of right team are anons
                                        if (rightTeamAnons.size() > 0) {
                                            for (int j = 0; j < rightTeamAnons.size(); j++) {
                                                String currAnon = rightTeamAnons.get(j);
                                                ListData2 listData = new ListData2(false, currAnon.split("_")[1] + " 의 동료", currAnon, "");
                                                rightTeamUsers.add(listData);
                                            }
                                            rightAdapter.notifyDataSetChanged();
                                            teamName2.setText("주장: " + rightTeamAnons.get(0).split("_")[1] + "의 동료");
                                            teamProfile2.setText("총원: " + String.valueOf(rightTeamAnons.size()) + "명");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        // UI 초기화 끝

                    }

                    // 매칭 실패
                    else{
                        String reason = response.getString("reason");
                        if (reason.equals("NoSuchStadiumException")){
                            Log.e("SMGR_ERROR", "Data corrupt, smgr stadium does not exist");
                        }
                        else if (reason.equals("PreparingNotReadyException")){
                            Toast.makeText(getApplicationContext(), "아직 경기를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.e("Unidentified error:", reason);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                }
        });
    }
}
