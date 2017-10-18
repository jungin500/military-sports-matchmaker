package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class EditProfileActivity extends AppCompatActivity {

    public ArrayList<String> ranks;
    public ArrayList<String> sexes;
    // widgets
    private TextView idView;
    private EditText pwView;
    private EditText pwView2;
    private EditText nameView;
    private EditText unitView;
    private EditText favView;
    private EditText descView;
    private Spinner rankView;
    private Spinner sexView;
    private Button submitButton;

    // id uniqueness check flag
    private Boolean idFlag;

    private SessionManager smgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        initializeSpinner();
        idFlag = true;

        // connect widgets
        idView = (TextView) findViewById(R.id.editProfile_id);
        pwView = (EditText) findViewById(R.id.editProfile_pw);
        pwView2 = (EditText) findViewById(R.id.editProfile_pw2);
        nameView = (EditText) findViewById(R.id.editProfile_name);
        unitView = (EditText) findViewById(R.id.editProfile_unit);
        favView = (EditText) findViewById(R.id.editProfile_favorite);
        descView = (EditText) findViewById(R.id.editProfile_desc);

        // set spinner to rank
        rankView = (Spinner) findViewById(R.id.editProfile_rank);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ranks);
        rankView.setAdapter(adapter);

        // set spinner to sex
        sexView = (Spinner) findViewById(R.id.editProfile_sex);
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexes);
        sexView.setAdapter(adapterSex);

        smgr = new SessionManager(getApplicationContext());
        final String id = smgr.getProfile().get(smgr.ID);
        idView.setText("군번: " + id + " (수정 불가)");

        //TODO: 회원정보를 받아서 표시.
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String getInfoURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/getUserInfo";
        client.setCookieStore(smgr.myCookies);
        client.get(getInfoURL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success){
                        nameView.setText(response.getString("name"));
                        unitView.setText(response.getString("unit"));
                        favView.setText(response.getString("favoriteEvent"));
                        descView.setText(response.getString("description"));
                        rankView.setSelection(RankHelper.numRanks() - 1 -  response.getInt("rank"));
                        sexView.setSelection(response.getString("gender").equals("여성") ? 0 : 1);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "회원정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
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
        /*
        //TODO: submitButton 수정
        submitButton = (Button) findViewById(R.id.editProfile_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String pw = pwView.getText().toString();
                String pw2 = pwView2.getText().toString();
                String name = nameView.getText().toString();
                String unit = unitView.getText().toString();
                String fav = favView.getText().toString();
                String desc = descView.getText().toString();
                String rank = rankView.getSelectedItem().toString();
                String sex = sexView.getSelectedItem().toString();
                boolean pwChanged = !pw.equals("");
                // 제대로 다 입력했는지 확인.
                if (pwChanged){
                    if (!pw.equals(pw2)){
                        pwView2.setError("비밀번호가 일치하지 않습니다.");
                        pwView2.requestFocus();
                        return;
                    }
                    if (pw.length() < 6){
                        pwView.setError("비밀번호가 너무 짧습니다.");
                        pwView.requestFocus();
                        return;
                    }
                }
                if (name.equals("")){
                    nameView.setError("이름을 입력해주십시오.");
                    nameView.requestFocus();
                    return;
                }
                if (unit.equals("")){
                    unitView.setError("소속부대를 입력해주십시오.");
                    unitView.requestFocus();
                    return;
                }
                if (fav.equals("")){
                    favView.setError("좋아하는 운동을 입력해주십시오.");
                    favView.requestFocus();
                    return;
                }
                if (desc.equals("")){
                    descView.setError("자기소개를 입력해주십시오.");
                    descView.requestFocus();
                    return;
                }

                int rankid = RankHelper.rankToInt(rank);
                int sexid = (sex.equals("여성") ? 0 : 1);

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("id", id);
                params.put("password", pw);
                params.put("name", name);
                params.put("rank", rankid);
                params.put("unit", unit);
                params.put("gender",sexid);
                params.put("favoriteEvent",fav);
                params.put("description",desc);

                client.setCookieStore(smgr.myCookies);
                //TODO: change URL
                String registerURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/registerUser";
                client.post(registerURL, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            boolean result = response.getBoolean("result");
                            //TODO: success in editing profile - modify sesson manager and go to HomeActivity!!
                            if (result){
                                // HomeActivity로 돌아가기.
                            }
                            else {
                                String error = response.getString("reason");
                                if (error.equals("MissingValuesException")) {
                                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                                else if (error.equals("AlreadyExistingException")){
                                    idView.setError("이미 존재하는 군번입니다.");
                                    idView.requestFocus();
                                    idFlag = true;
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }

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
        });
        */
    }

    //initialize spinners
    private void initializeSpinner(){
        // spinner for rank
        ranks = new ArrayList<String>();
        // add all ranks to arraylist
        for(int i = 0; i < RankHelper.numRanks(); i++){
            ranks.add(RankHelper.ranks[RankHelper.numRanks() - 1 - i]);
        }
        // spinner for sex
        sexes = new ArrayList<String>();
        sexes.add("여성");
        sexes.add("남성");
    }

}
