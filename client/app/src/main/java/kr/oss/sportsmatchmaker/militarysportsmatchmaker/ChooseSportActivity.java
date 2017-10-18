package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;


public class ChooseSportActivity extends AppCompatActivity implements OnClickListener {
    public static final String EXTRA_PNUM = "EXTRA_PNUM";
    public static final String EXTRA_SPORTTYPE = "EXTRA_SPORTTYPE";

    public static final int PNUM_FOOTBALL = 11;
    public static final int PNUM_BASKETBALL = 5;
    public static final int PNUM_JOKGU = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_choose_sport);

        ImageButton football = (ImageButton) findViewById(R.id.football);
        ImageButton basketball = (ImageButton) findViewById(R.id.basketball);
        ImageButton footwear = (ImageButton) findViewById(R.id.jokgu);

        football.setOnClickListener(this);
        basketball.setOnClickListener(this);
        footwear.setOnClickListener(this);
    }

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
