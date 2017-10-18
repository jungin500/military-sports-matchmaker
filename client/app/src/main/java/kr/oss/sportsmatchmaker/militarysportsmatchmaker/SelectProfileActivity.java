package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectProfileActivity extends AppCompatActivity implements OnClickListener{

    final int GET_PICTURE_URI = 1;
    ImageView selectprofile;
    Button selectphoto;
    TextView viewview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profile);

        selectphoto = (Button) findViewById(R.id.SelectPhoto);
        viewview = (TextView) findViewById(R.id.textView3);
        selectphoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_PICTURE_URI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        if (requestCode == GET_PICTURE_URI) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    selectprofile = (ImageView) findViewById(R.id.SelectProfile);
                    selectprofile.setImageBitmap(bitmap);
                    viewview.setText(String.valueOf(bitmap));
                } catch (Exception e) {
                    Log.e("test", e.getMessage());
                }
            }
        }
    }
}
