package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */
import android.widget.Button;

public class ListData {
    private String face;
    private String name;
    private String textview;

    public ListData(String face, String name, String textview) {
        this.face = face;
        this.name = name;
        this.textview = textview;
    }

    public String getFace() {
        return face;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return textview;
    }
}



