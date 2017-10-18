package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */
import android.widget.Button;

public class ListData {
    private String face;
    private String name;
    private String armnum;
    private String button;

    public ListData(String face, String name, String armnum, String button) {
        this.face = face;
        this.name = name;
        this.armnum = armnum;
        this.button = button;
    }

    public String getFace() {
        return face;
    }

    public String getArm_Num() {
        return armnum;
    }

    public String getName() {
        return name;
    }

    public String getButton() {
        return button;
    }
}



