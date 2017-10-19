package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ReservePlaceActivity extends AppCompatActivity implements OnClickListener{

    Spinner BL; //대대

    //TODO: Replace with DB
    String[] BLarr = {"대대선택","1대대","2대대","3대대","4대대"};

    String Bl[] = {"1대대","1대대","3대대","4대대"};
    String sports[] = {"축구","축구","족구","축구"};
    String stadium[] = {"1대대축구경기장","1대대2부축구경기장","3대대족구경기장","4대대축구경기장"};
    TextView selectStadium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reserve_place);

        selectStadium = (TextView) findViewById(R.id.select);
        ImageButton scoccer = (ImageButton) findViewById(R.id.scoccer);
        /*
        ImageButton basketball = (ImageButton) findViewById(R.id.basketball);
        ImageButton footwear = (ImageButton) findViewById(R.id.footwear);
        ImageButton free = (ImageButton) findViewById(R.id.free);
        */

        BL = (Spinner)findViewById(R.id.BLSpinner); // 대대 스피너

        ArrayAdapter<String> adapterSP1 = new ArrayAdapter<>(ReservePlaceActivity.this, android.R.layout.simple_list_item_1, BLarr);

        BL.setAdapter(adapterSP1);

        scoccer.setOnClickListener(this);
    }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.scoccer:
                    AlertDialog.Builder scoccerAlert = new AlertDialog.Builder(
                            ReservePlaceActivity.this);
                    scoccerAlert.setTitle("선택가능 경기장");

                    final ArrayAdapter<String> scoccerAdapter = new ArrayAdapter<String>(
                            ReservePlaceActivity.this,
                            android.R.layout.select_dialog_singlechoice);

                    int count = 0;

                    for(int i=0;i<4;i++) {
                        if (BL.getSelectedItem().toString() == Bl[i] && sports[i] == "축구") {
                            scoccerAdapter.add(stadium[i]);
                            count++;
                        }
                    }

                    scoccerAlert.setAdapter(scoccerAdapter,new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {String aa = scoccerAdapter.getItem(id);
                                                                selectStadium.setText(aa.toString());}
                            });

                    if(count==0)
                        Toast.makeText(ReservePlaceActivity.this,"선택가능 경기장이 없습니다.",Toast.LENGTH_SHORT).show();
                    else {
                        scoccerAlert.show();
                        count=0;
                    }
                    break;

                //case R.id.basketball:
                //case R.id.footwear:
                case R.id.free:
                    Intent intent3 = new Intent(getApplicationContext(), NotificationExamActivity.class);
            }
    }
}
