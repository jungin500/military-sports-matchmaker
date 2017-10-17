package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<ListData>{
    private Context context;
    private int layoutResource;
    private ArrayList<ListData> listData;

    public CustomAdapter(Context context, int layoutResource, ArrayList<ListData> listData) {
        super(context, layoutResource, listData);
        this.context = context;
        this.layoutResource = layoutResource;
        this.listData = listData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResource, parent, false);
        }

        ImageView face = (ImageView) row.findViewById(R.id.Profile);
        TextView name = (TextView) row.findViewById(R.id.Name);
        TextView insert = (TextView) row.findViewById(R.id.textView);
        try{
            InputStream is = context.getAssets().open(listData.get(position).getFace());
            Drawable d = Drawable.createFromStream(is, null);
        }   catch(IOException e){
            Log.e("ERROR", "ERROR: ", e);
        }

        return row;
    }
}
