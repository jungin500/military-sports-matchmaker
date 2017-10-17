package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class ChooseSportActivity extends AppCompatActivity implements OnClickListener {

    //ImageButton custom = (ImageButton) findViewById(R.id.custom);
    //static String a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_sport);

        ImageButton football = (ImageButton) findViewById(R.id.football);
        ImageButton basketball = (ImageButton) findViewById(R.id.basketball);
        ImageButton footwear = (ImageButton) findViewById(R.id.footwear);

        football.setOnClickListener(this);
        basketball.setOnClickListener(this);
        footwear.setOnClickListener(this);
        //custom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.football:
                Intent intent = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent.putExtra("BIRTHDAY_KEY",11);
                startActivity(intent);
                break;
            case R.id.basketball:
                Intent intent1 = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent1.putExtra("BIRTHDAY_KEY",5);
                startActivity(intent1);
                break;
            case R.id.footwear:
                Intent intent2 = new Intent(getApplicationContext(), MatchSettingActivity.class);
                intent2.putExtra("BIRTHDAY_KEY",5);
                startActivity(intent2);
                break;
        }
    }
}
    /*
    public String[] setnum(String[] player ,int num){
        player = new String[num];

        for(int i=0;i<num;i++){
            player[i] = "팀원" + (i+1);
        }

        return player;
    }
    */
    /*
    public void select(View name, String a){
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        .class);
                startActivity(intent);
            }
        });
    }
    */
