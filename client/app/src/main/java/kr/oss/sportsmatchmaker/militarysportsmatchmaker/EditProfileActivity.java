package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class EditProfileActivity extends AppCompatActivity {

    private final int GET_PICTURE_URI = 1;
    private ArrayList<String> ranks;
    private ArrayList<String> sexes;
    private ArrayList<String> units;
    /*
     위젯 선언부
     */
    private TextView idView;
    private EditText pwView;
    private EditText pwView2;
    private EditText nameView;
    private EditText favView;
    private EditText descView;
    private Spinner rankView;
    private Spinner sexView;
    private Spinner unitView;
    private Button submitButton;
    private ImageButton profPic;
    private byte[] byteImage;

    // id uniqueness check flag
    private Boolean idFlag;

    private SessionManager smgr;
    private Proxy proxy;

    /*
     갤러리 사용을 위한 Permissions 선언
    */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         상태바 없애는 코드
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_profile);

        proxy = new Proxy(getApplicationContext());
        smgr = new SessionManager(getApplicationContext());

        verifyStoragePermissions(this);

        initializeSpinner();
        idFlag = true;

        // 위젯들 사용위한 선언부
        idView = (EditText) findViewById(R.id.editProfile_id);
        pwView = (EditText) findViewById(R.id.editProfile_pw);
        pwView2 = (EditText) findViewById(R.id.editProfile_pw2);
        nameView = (EditText) findViewById(R.id.editProfile_name);
        favView = (EditText) findViewById(R.id.editProfile_favorite);
        descView = (EditText) findViewById(R.id.editProfile_desc);
        profPic = (ImageButton) findViewById(R.id.editProfile_profPic);

        /*
        계급고르는 스피너
        */
        rankView = (Spinner) findViewById(R.id.editProfile_rank);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ranks);
        rankView.setAdapter(adapter);

        /*
         성별고르는 스피너
         */
        sexView = (Spinner) findViewById(R.id.editProfile_sex);
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexes);
        sexView.setAdapter(adapterSex);

        /*
         소속부대고르는 스피너
         */
        unitView = (Spinner) findViewById(R.id.editProfile_unit);
        ArrayAdapter<String> adapterUnits = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, units);
        unitView.setAdapter(adapterUnits);

        final String id = smgr.getProfile().get(SessionManager.ID);
        idView.setText(id);
        idView.setEnabled(false);

        /*
        프로필사진 추가시키는 버튼
        */
        byteImage = null;
        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_PICTURE_URI);
            }
        });

        /*
         지금 로그인 되어있는 회원 정보 불로오기
         */
        proxy.getUserInfo(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean success = response.getBoolean("result");
                    if (success){
                        nameView.setText(response.getString("name"));
                        int tempunit = (int) (response.getString("unit").charAt(0))-'1';
                        if (tempunit < 0 || tempunit > 3){
                            tempunit = 0;
                        }
                        unitView.setSelection(tempunit);
                        favView.setText(response.getString("favoriteEvent"));
                        descView.setText(response.getString("description"));
                        rankView.setSelection(RankHelper.numRanks() - 1 -  response.getInt("rank"));
                        sexView.setSelection(response.getString("gender").equals("여성") ? 0 : 1);
                        if (response.getBoolean("profile_image")){
                            proxy.getProfPic(id, new FileAsyncHttpResponseHandler(getApplicationContext()) {
                                public void onSuccess(int i, Header[] headers, File file){
                                    Log.e("TAG", String.valueOf(file.length()));
                                    String filePath = file.getAbsolutePath();
                                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                                    profPic.setImageDrawable(rbd);
                                }
                                @Override
                                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                    Log.e("TAG", "Error: file open failed");
                                }
                            });
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "회원정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /*
         프로필 수정란에 적었던것들 받아오기
         */
        submitButton = (Button) findViewById(R.id.editProfile_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String pw = pwView.getText().toString();
                String pw2 = pwView2.getText().toString();
                final String name = nameView.getText().toString();
                String unit = unitView.getSelectedItem().toString();
                String fav = favView.getText().toString();
                String desc = descView.getText().toString();
                final String rank = rankView.getSelectedItem().toString();
                String sex = sexView.getSelectedItem().toString();
                boolean pwChanged = !pw.equals("");
                // 제대로 다 입력했는지 확인.
                if (pwChanged){
                    if (!pw.equals(pw2)){
                        pwView2.setError("비밀번호가 일치하지 않습니다.");
                        pwView2.requestFocus();
                        return;
                    }
                    if (pw.length() < 6){
                        pwView.setError("비밀번호가 너무 짧습니다.");
                        pwView.requestFocus();
                        return;
                    }
                }
                if (name.equals("")){
                    nameView.setError("이름을 입력해주십시오.");
                    nameView.requestFocus();
                    return;
                }
                if (fav.equals("")){
                    favView.setError("좋아하는 운동을 입력해주십시오.");
                    favView.requestFocus();
                    return;
                }
                if (desc.equals("")){
                    descView.setError("자기소개를 입력해주십시오.");
                    descView.requestFocus();
                    return;
                }

                int rankid = RankHelper.rankToInt(rank);
                int sexid = (sex.equals("여성") ? 0 : 1);
                proxy.updateUserInfo(id, pw, name, rankid, unit, sexid, fav, desc, byteImage, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            boolean result = response.getBoolean("result");
                            if (result){
                                smgr.createSession(id, name, rank);
                                finish();
                            }
                            else {
                                String error = response.getString("reason");
                                if (error.equals("MissingValuesException")) {
                                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    /*
     스피너 내용물들 초기화
     */
    private void initializeSpinner(){
        // spinner for rank
        ranks = new ArrayList<String>();
        // add all ranks to arraylist
        for(int i = 0; i < RankHelper.numRanks(); i++){
            ranks.add(RankHelper.ranks[RankHelper.numRanks() - 1 - i]);
        }
        // spinner for sex
        sexes = new ArrayList<String>();
        sexes.add("여성");
        sexes.add("남성");

        // spinner for unit
        units = new ArrayList<String>();
        units.add("1대대");
        units.add("2대대");
        units.add("3대대");
        units.add("4대대");
    }


    /*
     수정하거나 등록할 사진 선택
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        if (requestCode == GET_PICTURE_URI) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri profPicUri = data.getData();
                    Bitmap bitmap = formatBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), profPicUri));
                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                    profPic.setImageDrawable(rbd);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteImage = stream.toByteArray();
                } catch (Exception e) {
                    Log.e("test", e.getMessage());
                }
            }
        }
    }

    public static Bitmap formatBitmap(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        return Bitmap.createScaledBitmap(Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight), 240, 240, false);
    }

}
