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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;

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

    String Information[][] = {{"00", "01", "02"}, {"소령 이무기", "대장 강정호", "소위 김찬양"}};

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
                alertDialogBuilder.setNegativeButton("검색",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String queryid = search.getText().toString();
                                if(SearchPlayer(queryid) < 0) {
                                    Toast.makeText(context, "해당 선수가 없습니다.", Toast.LENGTH_SHORT).show();
                                    idView.setText("");
                                    idView.setText("선수를 추가해주세요.");
                                }
                                else {
                                    listData.get(position).setId(queryid);
                                    listData.get(position).setName(Information[1][SearchPlayer(queryid)]);
                                    nameView.setText(Information[1][SearchPlayer(queryid)]);
                                    idView.setText(queryid);
                                    int temp3 = context.getResources().getIdentifier("img_" + idView.getText().toString(), "drawable", "kr.oss.sportsmatchmaker.militarysportsmatchmaker");
                                    listData.get(position).setFace(BitmapFactory.decodeResource(context.getResources(), temp3));
                                    notifyDataSetChanged();
                                }
                            }
                        });

                alertDialogBuilder.show();
            }
        });

        return row;
    }

    public int SearchPlayer(CharSequence armnum){
        int num = -1;

        for(int i=0;i<3;i++) {
            if (Information[0][i].equals(armnum)) {
                num = i;
                break;
            }
        }

        return num;
    }
}
