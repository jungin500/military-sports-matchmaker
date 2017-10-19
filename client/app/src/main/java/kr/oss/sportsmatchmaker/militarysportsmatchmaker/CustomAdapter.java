package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;

import cz.msebera.android.httpclient.Header;

    public class CustomAdapter extends ArrayAdapter<ListData>{
    private Context context;
    private int layoutResource;
    private ArrayList<ListData> listData;
    private SessionManager smgr;
    private Proxy proxy;

    public CustomAdapter(Context context, int layoutResource, ArrayList<ListData> listData) {
        super(context, layoutResource, listData);
        this.context = context;
        this.layoutResource = layoutResource;
        this.listData = listData;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        smgr = new SessionManager(context);
        proxy = new Proxy(context);

        View row = convertView;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResource, parent, false);
        }

        final ImageView faceView = (ImageView) row.findViewById(R.id.Profile);
        final TextView nameView = (TextView) row.findViewById(R.id.Name);
        final TextView idView = (TextView) row.findViewById(R.id.Id);
        Button button = (Button) row.findViewById(R.id.button);

        // set default settings
        Bitmap bitmap = listData.get(position).getFace();
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        rbd.setCornerRadius(bitmap.getHeight()/8.0f);
        faceView.setImageDrawable(rbd);
        nameView.setText(listData.get(position).getName());
        button.setText(listData.get(position).getButton());
        idView.setText(listData.get(position).getId());
        if (listData.get(position).getId().split("_").equals("anon")){
            idView.setText("");
        }

        // if leader, find profileimage and add.
        if (position == 0){
            proxy.getUserInfo(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        boolean success = response.getBoolean("result");
                        if (success){
                            if (response.getBoolean("profile_image")){
                                proxy.getProfPic(response.getString("id"), new FileAsyncHttpResponseHandler(context) {
                                    public void onSuccess(int i, Header[] headers, File file){
                                        Log.e("TAG", String.valueOf(file.length()));
                                        String filePath = file.getAbsolutePath();
                                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
                                        rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                                        faceView.setImageDrawable(rbd);
                                    }
                                    @Override
                                    public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                        Log.e("TAG", "Error: file open failed");
                                    }
                                });
                            }
                        }
                        else {
                            Toast.makeText(context, "회원정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // button does nothing for position 0
                if (position == 0)
                    return;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("선수 검색");
                alertDialogBuilder.setMessage("군번을 입력하세요.");
                final EditText search = new EditText(context);
                search.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
                search.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));

                alertDialogBuilder.setView(search);
                alertDialogBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    // 다이얼로그 취소
                    dialog.cancel();
                    }
                });
                alertDialogBuilder.setNegativeButton("검색", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        final String queryid = search.getText().toString();
                        // 검색어와 중복된 아이디가 있으면
                        for (ListData i : listData){
                            if (queryid.equals(i.getId())) {
                                Toast.makeText(context, "이미 등록된 사람을 검색할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        //프사 받아와서 보여준다.
                        proxy.getUserDetail(queryid, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    boolean result = response.getBoolean("result");
                                    if (result){
                                        listData.get(position).setId(queryid);
                                        listData.get(position).setName(RankHelper.intToRank(response.getInt("rank")) + " " + response.getString("name"));
                                        // add picture to listData and notifyDataSetChanged();
                                        if (response.getBoolean("profile_image")){
                                            proxy.getProfPic(queryid, new FileAsyncHttpResponseHandler(context) {
                                                public void onSuccess(int i, Header[] headers, File file){
                                                    String filePath = file.getAbsolutePath();
                                                    listData.get(position).setFace(BitmapFactory.decodeFile(filePath));
                                                    notifyDataSetChanged();
                                                }
                                                @Override
                                                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                                                    Log.e("TAG", "Error: file open failed");
                                                }
                                            });

                                        }
                                        else {
                                            notifyDataSetChanged();
                                        }
                                    }
                                    else {
                                        String errorName = response.getString("reason");
                                        if (errorName.equals("NoSuchUserException")){
                                            Toast.makeText(context, "군번이 없습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (errorName.equals("NotLoggedInException")){
                                            Toast.makeText(context, "세션이 만료되었습니다. 다시 로그인해주십시오.", Toast.LENGTH_SHORT).show();
                                            smgr.logout();
                                        }
                                        Log.e("CustomAdapter", errorName);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                alertDialogBuilder.show();
            }
        });

        return row;
    }
}
