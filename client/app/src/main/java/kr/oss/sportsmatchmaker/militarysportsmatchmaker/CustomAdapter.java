package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

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

    public CustomAdapter(Context context, int layoutResource, ArrayList<ListData> listData) {
        super(context, layoutResource, listData);
        this.context = context;
        this.layoutResource = layoutResource;
        this.listData = listData;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View row = convertView;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResource, parent, false);
        }

        final ImageView faceView = (ImageView) row.findViewById(R.id.Profile);
        final TextView nameView = (TextView) row.findViewById(R.id.Name);
        final TextView idView = (TextView) row.findViewById(R.id.Id);
        Button button = (Button) row.findViewById(R.id.button);

        faceView.setImageBitmap(listData.get(position).getFace());
        nameView.setText(listData.get(position).getName());
        button.setText(listData.get(position).getButton());
        idView.setText(listData.get(position).getId());
        if (listData.get(position).getId().equals("anon")){
            idView.setText("");
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
                        smgr = new SessionManager(context);

                        final String queryid = search.getText().toString();
                        if (queryid.equals(smgr.getProfile().get(smgr.ID))) {
                            Toast.makeText(context, "자기 자신을 검색할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();
                        params.put("id", queryid);
                        client.setCookieStore(smgr.myCookies);
                        String queueURL = Proxy.SERVER_URL + ":" + Proxy.SERVER_PORT + "/process/searchUserDetails";
                        client.post(queueURL, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    boolean result = response.getBoolean("result");
                                    if (result){
                                        listData.get(position).setId(queryid);
                                        listData.get(position).setName(RankHelper.intToRank(response.getInt("rank")) + " " + response.getString("name"));
                                        //TODO: 프로필 사진 기능 만들면 프사 받아와서 구현
                                        String temp = "img_0" + String.valueOf(position % 3);
                                        int temp3 = context.getResources().getIdentifier(temp, "drawable", "kr.oss.sportsmatchmaker.militarysportsmatchmaker");
                                        listData.get(position).setFace(BitmapFactory.decodeResource(context.getResources(), temp3));
                                        notifyDataSetChanged();
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
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
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
