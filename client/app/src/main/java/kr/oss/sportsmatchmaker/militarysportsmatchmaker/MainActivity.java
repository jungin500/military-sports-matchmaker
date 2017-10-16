package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


// MainActivity is a splash screen where we decide whether to go or not.
public class MainActivity extends AppCompatActivity {

    //TODO: fix this using SharedPref
    boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: update using SharedPref

        //TODO: go to core activity
        if (isLoggedIn) {

        }
        // goto sign in screen.
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
