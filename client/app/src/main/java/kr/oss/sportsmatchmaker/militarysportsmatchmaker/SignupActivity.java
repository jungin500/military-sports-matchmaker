package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "kr.oss.sportsmatchmaker.signup.id";
    public static final String EXTRA_PW = "kr.oss.sportsmatchmaker.signup.pw";

    public ArrayList<String> ranks;
    public ArrayList<String> sexes;
    // widgets
    private EditText idView;
    private Button idCheckButton;
    private EditText pwView;
    private EditText pwView2;
    private EditText nameView;
    private EditText unitView;
    private Spinner rankView;
    private Spinner sexView;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initializeSpinner();

        // connect EditText widgets
        idView = (EditText) findViewById(R.id.signup_id);
        pwView = (EditText) findViewById(R.id.signup_pw);
        pwView2 = (EditText) findViewById(R.id.signup_pw2);
        nameView = (EditText) findViewById(R.id.signup_name);
        unitView = (EditText) findViewById(R.id.signup_unit);


        // set spinner to rank
        rankView = (Spinner) findViewById(R.id.signup_rank);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ranks);
        rankView.setAdapter(adapter);
        rankView.setSelection(0);

        // set spinner to sex
        sexView = (Spinner) findViewById(R.id.signup_sex);
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexes);
        sexView.setAdapter(adapterSex);
        sexView.setSelection(0);

        //TODO: ID 중복체크

        signupButton = (Button) findViewById(R.id.signup_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = idView.getText().toString();
                String pw = pwView.getText().toString();
                String pw2 = pwView2.getText().toString();
                String name = nameView.getText().toString();
                String unit = unitView.getText().toString();
                String rank = rankView.getSelectedItem().toString();
                int rankid = ranks.size() - ranks.indexOf(rank) - 2; // 선택.
                String sex = sexView.getSelectedItem().toString();
                int sexid = sexes.size() - sexes.indexOf(sex) - 2;

                // 제대로 다 입력했는지 확인.
                if (id == ""){
                    idView.setError("군번을 입력해주십시오.");
                    idView.requestFocus();
                    return;
                }
                if (pw == ""){
                    pwView.setError("비밀번호를 입력해주십시오.");
                    pwView.requestFocus();
                    return;
                }
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
                if (name == ""){
                    nameView.setError("이름을 입력해주십시오.");
                    nameView.requestFocus();
                    return;
                }
                if (unit == ""){
                    unitView.setError("소속부대를 입력해주십시오.");
                    unitView.requestFocus();
                    return;
                }
                if (rankid >= ranks.size() - 2){
                    TextView errorRank = (TextView) rankView.getSelectedView();
                    errorRank.setError("계급을 선택해주십시오.");
                    errorRank.requestFocus();
                    return;
                }
                if (sexid >= sexes.size() - 2){
                    TextView errorSex = (TextView) sexView.getSelectedView();
                    errorSex.setError("성별을 선택해주십시오.");
                    errorSex.requestFocus();
                    return;
                }

                //TODO: 서버로 정보 보내서 회원가입


                // 로그인창으로 돌아가기
                Intent data = new Intent();
                data.putExtra(EXTRA_ID, id);
                data.putExtra(EXTRA_PW, pw);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    //initialize spinners
    private void initializeSpinner(){
        // spinner for rank
        ranks = new ArrayList<String>();
        ranks.add("계급");
        ranks.add("----");
        ranks.add("대장");
        ranks.add("중장");
        ranks.add("소장");
        ranks.add("준장");
        ranks.add("대령");
        ranks.add("중령");
        ranks.add("소령");
        ranks.add("대위");
        ranks.add("중위");
        ranks.add("소위");
        ranks.add("준위");
        ranks.add("원사");
        ranks.add("상사");
        ranks.add("중사");
        ranks.add("하사");
        ranks.add("병장");
        ranks.add("상병");
        ranks.add("일병");
        ranks.add("이병");
        // spinner for sex
        sexes = new ArrayList<String>();
        sexes.add("성별");
        sexes.add("----");
        sexes.add("남성");
        sexes.add("여성");
    }

}
