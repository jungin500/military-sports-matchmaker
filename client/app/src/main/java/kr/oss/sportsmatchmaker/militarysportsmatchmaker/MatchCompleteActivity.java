package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MatchCompleteActivity extends AppCompatActivity implements OnClickListener{

    SessionManager smgr;
    Proxy proxy;


    //widget
    TextView stadium;
    ImageButton buttonTeam1;
    ImageButton buttonTeam2;
    TextView teamName1;
    TextView teamProfile1;
    TextView teamName2;
    TextView teamProfile2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match_complete);

        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());

        final TextView stadium = (TextView) findViewById(R.id.stadium);
        final TextView teamName1 = (TextView) findViewById(R.id.teamname1);
        final TextView teamProfile1 = (TextView) findViewById(R.id.teamprofile1);
        final TextView teamName2 = (TextView) findViewById(R.id.teamname2);
        final TextView teamProfile2 = (TextView) findViewById(R.id.teamprofile2);

        buttonTeam1 = (ImageButton) findViewById(R.id.team1);
        buttonTeam1.setOnClickListener(this);
        buttonTeam2 = (ImageButton) findViewById(R.id.team2);
        buttonTeam2.setOnClickListener(this);

        proxy.prepareMatchingTeamStadium(smgr.getStadiumName(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    // 꽉 차서 매칭 성공
                    if (result) {
                        stadium.setText("장소: " + smgr.getStadiumName() + ", 시간: 2017년 10월 20일 16시 00분");

                        //TODO: size 여러개의 team. 일단은 size 1들만 있다고 하자.
                        JSONArray leftTeam = response.getJSONArray("leftTeam");
                        JSONArray rightTeam = response.getJSONArray("rightTeam");

                        ArrayList<String> leftTeamPlayers = new ArrayList<String>();
                        ArrayList<String> rightTeamPlayers = new ArrayList<String>();
                        String leftTeamOne = null;
                        String rightTeamOne = null;

                        // debugging purpose
                        StringBuilder lsb = new StringBuilder();
                        StringBuilder rsb = new StringBuilder();
                        for (int i = 0; i < leftTeam.length(); i++) {
                            String currPlayer = leftTeam.getJSONObject(i).getJSONArray("players").getString(0);
                            leftTeamPlayers.add(currPlayer);
                            if ((leftTeamOne == null) && (!currPlayer.equals("anon"))){
                                leftTeamOne = currPlayer;
                            }
                            lsb.append(currPlayer + "|");
                        }
                        for (int i = 0; i < rightTeam.length(); i++) {
                            String currPlayer = rightTeam.getJSONObject(i).getJSONArray("players").getString(0);
                            rightTeamPlayers.add(currPlayer);
                            if ((rightTeamOne == null) && (!currPlayer.equals("anon"))){
                                rightTeamOne = currPlayer;
                            }
                            rsb.append(currPlayer + "|");
                        }
                        String ls = lsb.toString();

                        String rs = rsb.toString();



                        final String leftTeamLeader = leftTeamOne;
                        final String rightTeamLeader = rightTeamOne;
                        final String[] leftArray = leftTeamPlayers.toArray(new String[leftTeamPlayers.size()]);
                        final String[] rightArray = rightTeamPlayers.toArray(new String[rightTeamPlayers.size()]);



                        // leftTeam UI 초기화.
                        proxy.getUserDetail(leftTeamLeader, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    boolean result = response.getBoolean("result");
                                    if (result){
                                        String leaderRank = RankHelper.intToRank(response.getInt("rank"));
                                        String leaderName = response.getString("name");

                                        teamName1.setText("왼쪽 팀");
                                        teamProfile1.setText(leaderRank + " " + leaderName + " 외 " + String.valueOf(leftArray.length - 1) + "명");

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

                        // Right Team UI 초기화
                        proxy.getUserDetail(rightTeamLeader, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    boolean result = response.getBoolean("result");
                                    if (result){
                                        String leaderRank = RankHelper.intToRank(response.getInt("rank"));
                                        String leaderName = response.getString("name");

                                        teamName2.setText("오른쪽 팀");
                                        teamProfile2.setText(leaderRank + " " + leaderName + " 외 " + String.valueOf(rightArray.length - 1) + "명");

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
                                        Log.e("TAG2", "ERROR");
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
                            return;
                        }
                        else if (reason.equals("PreparingNotReadyException")){
                            Toast.makeText(getApplicationContext(), "아직 경기를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            Log.e("Unidentified error:", reason);
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClick(View v){

        switch(v.getId()){
            case R.id.team1 :
                AlertDialog.Builder alertdialog1 = new AlertDialog.Builder(MatchCompleteActivity.this);
                alertdialog1.setTitle("첫번째 팀");
                alertdialog1.setMessage("내용 ~~~~");
                alertdialog1.show();
                break;

            case R.id.team2 :
                AlertDialog.Builder alertdialog2 = new AlertDialog.Builder(MatchCompleteActivity.this);
                alertdialog2.setTitle("두번째 팀");
                alertdialog2.setMessage("내용 ~~~~");
                alertdialog2.show();
                break;
        }
    }
}
