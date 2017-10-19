package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;


public class ChooseSportActivity extends AppCompatActivity implements OnClickListener {

    public static final String EXTRA_PNUM = "EXTRA_PNUM";
    public static final String EXTRA_SPORTTYPE = "EXTRA_SPORTTYPE";
    private Proxy proxy;

    /*
     해당 운동 최대 가능 인원 수
     */
    public static final int PNUM_FOOTBALL = 11;
    public static final int PNUM_BASKETBALL = 5;
    public static final int PNUM_JOKGU = 5;

    /*
     스와이프 기능을 위한 선언
     */
    private SwipeRefreshLayout mSwipeRefresh;

    /*
     위젯 선언부
     */
    private TextView numFootball;    // 남은 축구경기 가능 인원 수
    private TextView numBasketball;
    private TextView numJokgu;
    private ImageButton football;
    private ImageButton basketball;
    private ImageButton jokgu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         상태바 없애는 코드
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_choose_sport);

        proxy = new Proxy(getApplicationContext());


        /*
         스와이프를 통한 액티비티 새로고침 구현부
         */
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                Intent intent = new Intent(ChooseSportActivity.this, ChooseSportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                mSwipeRefresh.setRefreshing(false);
            }
        });


        /*
         현재 들어가있는 큐가 없음을 확인.
         */
        proxy.getUserInfo(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (! response.getString("match_status").equals("ready")){
                        Toast.makeText(getApplicationContext(), "초대된 큐가 있습니다.",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        /*
         위젯들 사용 위한 선언부
         */
        football = (ImageButton) findViewById(R.id.football);
        basketball = (ImageButton) findViewById(R.id.basketball);
        jokgu = (ImageButton) findViewById(R.id.jokgu);
        numFootball = (TextView) findViewById(R.id.num_football);
        numBasketball = (TextView) findViewById(R.id.num_basketball);
        numJokgu = (TextView) findViewById(R.id.num_jokgu);

        /*
         이미지 버튼의 클릭 이벤트 지정
         */
        football.setOnClickListener(this);
        basketball.setOnClickListener(this);
        jokgu.setOnClickListener(this);

        /*
         각 텍스트 뷰의 내용 지정
         */
        numFootball.setText("빈자리 검색중..");
        numBasketball.setText("빈자리 검색중..");
        numJokgu.setText("빈자리 검색중..");

        /*
         남아있는 경기장 수를 구하기 위한 함수
         */
        proxy.getUserStadium(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // 해당 부대 stadium 존재
                    if (response.getBoolean("result")){
                        int numf = 0;
                        int numb = 0;
                        int numj = 0;
                        JSONArray stadia = response.getJSONArray("stadium");
                        for (int i = 0; i < stadia.length(); i++){
                            JSONObject stadium = stadia.getJSONObject(i);
                            JSONArray stad_types = stadium.getJSONArray("available_type");
                            int iavail = stadium.getInt("max_players") - stadium.getInt("in_players");
                            for (int j = 0; j < stad_types.length(); j++){
                                String ijtype = stad_types.getString(j);
                                if (ijtype.equals("football") && (iavail > numf)) numf = iavail;
                                else if (ijtype.equals("basketball") && (iavail > numb)) numb = iavail;
                                else if (ijtype.equals("jokgu") && (iavail > numj)) numj = iavail;
                            }
                        }
                        if (numf > PNUM_FOOTBALL) numf = PNUM_FOOTBALL;
                        if (numb > PNUM_BASKETBALL) numb = PNUM_BASKETBALL;
                        if (numj > PNUM_JOKGU) numj = PNUM_JOKGU;

                        numFootball.setText(String.valueOf(numf));
                        numBasketball.setText(String.valueOf(numb));
                        numJokgu.setText(String.valueOf(numj));
                    }

                    // 해당 부대 stadium 존재하지 않을 경우
                    else {
                        if (response.getString("reason").equals("NoSuchStadiumException"))
                            Toast.makeText(getApplicationContext(), "소속대에 등록된 운동장이 없습니다. 소속대에 문의하세요.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {

                    Log.e("TAG5", "here");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), String.valueOf(statusCode) + responseString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     버튼 클릭 이벤트 선언
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.football:
                Intent intent1 = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent1.putExtra(EXTRA_PNUM, PNUM_FOOTBALL);
                intent1.putExtra(EXTRA_SPORTTYPE, "football");
                startActivity(intent1);
                break;
            case R.id.basketball:
                Intent intent2 = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent2.putExtra(EXTRA_PNUM,PNUM_BASKETBALL);
                intent2.putExtra(EXTRA_SPORTTYPE, "basketball");
                startActivity(intent2);
                break;
            case R.id.jokgu:
                Intent intent3 = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent3.putExtra(EXTRA_PNUM,PNUM_JOKGU);
                intent3.putExtra(EXTRA_SPORTTYPE, "jokgu");
                startActivity(intent3);
                break;
        }
    }
}
