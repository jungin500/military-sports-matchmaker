package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class QueListActivity extends AppCompatActivity {

    private SessionManager smgr;

    private ArrayList<ListData> QueDataArray;
    private boolean[] Que_Yes_or_No = {true,false,true,true,false};
    private Button Yes_or_No;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_que_list);

        smgr = new SessionManager(getApplicationContext());

        final String id = smgr.getProfile().get(SessionManager.ID);
        final String name = smgr.getProfile().get(SessionManager.NAME);
        final String rank = smgr.getProfile().get(SessionManager.RANK);
        final String rankname = rank + " " + name;

        Yes_or_No = (Button) findViewById(R.id.button);

        QueDataArray = new ArrayList<ListData>();

        ListView listview = (ListView) findViewById(R.id.quelist);
        final CustomAdapter2 customAdapter = new CustomAdapter2(this, R.layout.list_btn_sty, QueDataArray);
        listview.setAdapter(customAdapter);

        int temp = getResources().getIdentifier("img_"+id,"drawable","kr.oss.sportsmatchmaker.militarysportsmatchmaker");
        ListData Reader = new ListData(BitmapFactory.decodeResource(getResources(), temp), rankname, id, "승낙");
        QueDataArray.add(Reader);
        for (int i = 1; i < 5; i++){
            ListData data = new ListData(BitmapFactory.decodeResource(getResources(), R.drawable.img_defaultface), "대령"+i, "00"+i, String.valueOf(Que_Yes_or_No[i]));
            QueDataArray.add(data);
        }

        customAdapter.notifyDataSetChanged();
    }
}
