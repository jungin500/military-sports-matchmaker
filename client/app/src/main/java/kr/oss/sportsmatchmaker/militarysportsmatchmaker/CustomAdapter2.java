package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class CustomAdapter2 extends ArrayAdapter<ListData>{
    private Context context;
    private int layoutResource;
    private ArrayList<ListData> listData;
    private SessionManager smgr;
    private Proxy proxy;

    public CustomAdapter2(Context context, int layoutResource, ArrayList<ListData> listData) {
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

        faceView.setImageBitmap(listData.get(position).getFace());
        nameView.setText(listData.get(position).getName());
        button.setText(listData.get(position).getButton());
        idView.setText(listData.get(position).getId());
        if (listData.get(position).getId().equals("anon")){
            idView.setText("");
        }

        return row;
    }
}
