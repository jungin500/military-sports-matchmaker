package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.HashMap;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //Helper
    private SessionManager smgr;
    private HashMap<String, String> prof;
    private SimpleAdapter menuAdapter;

    //Widgets
    private TextView textWelcome;
    private Button logoutButton;
    private TextView textQStatus;
    private ListView homeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        smgr = new SessionManager(getApplicationContext());
        smgr.checkLogin();
        prof = smgr.getProfile();

        // welcome message
        textWelcome = (TextView) findViewById(R.id.home_welcome);
        textWelcome.setText("환영합니다, " + prof.get(SessionManager.NAME) + "님.\n오늘은 어떤 체육활동을 하시겠어요?");

        // logout button settings
        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smgr.logout();
                finish();
            }
        });

        // queue status message
        textQStatus = (TextView) findViewById(R.id.home_qstatus);
        textQStatus.setText("축구 매치 대기중입니다.");

        // add adapter to listview. Long boring stuff, so factor into separate method.
        homeMenu = (ListView) findViewById(R.id.home_menu);
        setHomeMenu(homeMenu);
        homeMenu.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        smgr.checkLogin();
    }

    private void setHomeMenu(ListView homeMenu){
        ArrayList<HashMap<String, String>> hashMapMenuList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> menu1 = new HashMap<String, String>();
        menu1.put("line1", "전투체육 같이 할 사람 찾기");
        menu1.put("line2", "종목을 고르시면 자동으로 팀원과 상대방을 찾아드립니다.");
        hashMapMenuList.add(menu1);
        HashMap<String, String> menu2 = new HashMap<String, String>();
        menu2.put("line1", "전투체육 활동 등록 및 장소 예약");
        menu2.put("line2", "이미 사람을 다 모으셨나요? 장소를 잡아드립니다.");
        hashMapMenuList.add(menu2);
        HashMap<String, String> menu3 = new HashMap<String, String>();
        menu3.put("line1", "전투체육 일지");
        menu3.put("line2", "전우님의 전투체육 참여 현황을 편리하게 볼 수 있습니다.");
        hashMapMenuList.add(menu3);
        HashMap<String, String> menu4 = new HashMap<String, String>();
        menu4.put("line1", "프로필 수정");
        menu4.put("line2", "개인 프로필 정보를 변경합니다.");
        hashMapMenuList.add(menu4);
        String[] from = {"line1", "line2"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        menuAdapter = new SimpleAdapter(this, hashMapMenuList, android.R.layout.simple_list_item_2, from, to);
        homeMenu.setAdapter(menuAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.home_menu:
                switch(position){
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), ChooseSportActivity.class);
                        startActivity(intent);
                        break;
                    //TODO: 구현
                    default:
                        Toast.makeText(getApplicationContext(), "아직 미구현", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }
}
