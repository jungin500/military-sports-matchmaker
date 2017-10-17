package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;


public class MatchSettingActivity extends AppCompatActivity {

    private ArrayList<ListData> listDataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setting);

        Intent intent = getIntent();
        int playernum = intent.getIntExtra("BIRTHDAY_KEY", 0);
        String[] name = null;

        // initialize
        listDataArray =  new ArrayList<ListData>();
        for(int i =0;i<playernum;i++){
            ListData data = new ListData("0"+i+".jpg", i+"name", i+"textview");
            listDataArray.add(data);
        }

        ListView listview = (ListView) findViewById(R.id.listview1);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.list_btn_sty, listDataArray);
        listview.setAdapter(customAdapter);
    }


    public String[] setnum(String[] player ,int num){
        player = new String[num];

        for(int i=0;i<num;i++){
            player[i] = "선수추가";
        }

        return player;
    }
}
