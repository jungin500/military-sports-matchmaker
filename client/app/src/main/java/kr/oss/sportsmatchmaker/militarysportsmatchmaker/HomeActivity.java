package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    //Helper
    private SessionManager smgr;
    private HashMap<String, String> prof;

    //Widgets
    private TextView textWelcome;
    private Button logoutButton;
    private TextView textQStatus;
    private ListView homemenu;

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
            }
        });

        // TextView setting


    }
}
