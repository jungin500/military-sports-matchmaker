package com.example.administrator.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Match_setting extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setting);

        Intent intent = getIntent();
        int playernum = intent.getIntExtra("BIRTHDAY_KEY", 0);
        String[] name = null;

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, setnum(name, playernum)) ;

        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;

    }


    public String[] setnum(String[] player ,int num){
        player = new String[num];

        for(int i=0;i<num;i++){
            player[i] = "선수추가";
        }

        return player;
    }
}
