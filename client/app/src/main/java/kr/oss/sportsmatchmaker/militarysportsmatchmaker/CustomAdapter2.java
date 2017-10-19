package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class CustomAdapter2 extends ArrayAdapter<ListData2>{
    private Context context;
    private int layoutResource;
    private ArrayList<ListData2> listData;
    private SessionManager smgr;
    private Proxy proxy;

    public CustomAdapter2(Context context, int layoutResource, ArrayList<ListData2> listData) {
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
        final Button button = (Button) row.findViewById(R.id.button);

        faceView.setImageDrawable(context.getDrawable(R.drawable.img_defaultface));
        nameView.setText(listData.get(position).getName());
        button.setText(listData.get(position).getButton());
        idView.setText(listData.get(position).getId());
        if (listData.get(position).getId().equals("anon")){
            idView.setText("");
        }

        if (button.getText().equals("대기중")){
            button.setBackgroundColor(context.getColor(android.R.color.holo_orange_dark));
        }
        else if (button.getText().equals("거절함")){
            button.setBackgroundColor(context.getColor(android.R.color.holo_red_dark));
        }
        else button.setBackgroundColor(context.getColor(android.R.color.holo_green_dark));

        //TODO: show picture
        if (listData.get(position).getExistPic()){
            proxy.getProfPic(listData.get(position).getId(), new FileAsyncHttpResponseHandler(context) {
                public void onSuccess(int i, Header[] headers, File file){
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

        return row;
    }


}
