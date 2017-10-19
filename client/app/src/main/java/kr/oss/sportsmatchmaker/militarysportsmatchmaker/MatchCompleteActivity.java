package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MatchCompleteActivity extends AppCompatActivity implements OnClickListener{

    SessionManager smgr;
    Proxy proxy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match_complete);

        smgr = new SessionManager(getApplicationContext());
        proxy = new Proxy(getApplicationContext());

        ImageButton team1 = (ImageButton) findViewById(R.id.team1);
        team1.setOnClickListener(this);
        ImageButton team2 = (ImageButton) findViewById(R.id.team2);
        team2.setOnClickListener(this);



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
