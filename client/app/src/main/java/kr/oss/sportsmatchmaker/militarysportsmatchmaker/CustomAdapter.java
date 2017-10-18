package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

    String Information[][] = {{"00", "01", "02"}, {"소령 이무기", "대장 강정호", "소위 김찬양"}};

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResource, parent, false);
        }

        final ImageView face = (ImageView) row.findViewById(R.id.Profile);
        final TextView name = (TextView) row.findViewById(R.id.Name);
        final TextView armnum = (TextView) row.findViewById(R.id.Arm_Num);
        Button button = (Button) row.findViewById(R.id.button);

        try{
            InputStream is = context.getAssets().open(listData.get(position).getFace());
            Drawable d = Drawable.createFromStream(is, null);
            face.setImageDrawable(d);
        }   catch(IOException e){
            Log.e("ERROR", "ERROR: ", e);
        }
        name.setText(listData.get(position).getName());
        button.setText(listData.get(position).getButton());
        armnum.setText(listData.get(position).getArm_Num());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("선수 검색");
                alertDialogBuilder.setMessage("군번을 입력하세요.");
                final EditText search = new EditText(context);
                alertDialogBuilder.setView(search);

                alertDialogBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그 취소
                                        dialog.cancel();
                                    }
                                });
                alertDialogBuilder.setNegativeButton("검색",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog, int id) {

                                            if(SearchPlayer(search.getText().toString()) < 0)
                                                Toast.makeText(context,"해당 선수가 없습니다.",Toast.LENGTH_SHORT).show();
                                            else {
                                                armnum.setText(search.getText().toString());
                                                name.setText(Information[1][SearchPlayer(armnum.getText().toString())]);
                                                face.setImageResource(R.drawable.img_basketball);
                                            }

                                    }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialogBuilder.show();
            }
        });

        return row;
    }

    public int SearchPlayer(String armnum){
        int num = -1;

        for(int i=0;i<3;i++) {
            if (Information[0][i] == armnum)
                num = i;
        }

        return num;
    }
}
