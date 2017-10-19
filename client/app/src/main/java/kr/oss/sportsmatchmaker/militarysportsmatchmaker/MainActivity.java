package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager smgr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        smgr = new SessionManager(getApplicationContext());

        if (smgr.isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
        // goto sign in screen.
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
