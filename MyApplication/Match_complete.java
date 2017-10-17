package com.example.administrator.myapplication;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Match_complete extends AppCompatActivity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_complate);

        ImageButton team1 = (ImageButton) findViewById(R.id.team1);
        team1.setOnClickListener(this);
        ImageButton team2 = (ImageButton) findViewById(R.id.team2);
        team2.setOnClickListener(this);
    }

    public void onClick(View v){

        switch(v.getId()){
            case R.id.team1 :
                AlertDialog.Builder alertdialog1 = new AlertDialog.Builder(Match_complete.this);
                alertdialog1.setTitle("첫번째 팀");
                alertdialog1.setMessage("내용 ~~~~");
                alertdialog1.show();
                break;

            case R.id.team2 :
                AlertDialog.Builder alertdialog2 = new AlertDialog.Builder(Match_complete.this);
                alertdialog2.setTitle("두번째 팀");
                alertdialog2.setMessage("내용 ~~~~");
                alertdialog2.show();
                break;
        }
    }
}
