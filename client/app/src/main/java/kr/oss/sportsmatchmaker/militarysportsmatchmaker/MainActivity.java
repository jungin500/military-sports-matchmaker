package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


// MainActivity is a splash screen where we decide whether to go or not.
public class MainActivity extends AppCompatActivity {

    private SessionManager smgr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
