package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */
import android.graphics.Bitmap;
import android.widget.Button;

public class ListData {
    private Bitmap face;
    private String name;
    private String id;
    private String button;

    public ListData(Bitmap face, String name, String id, String button) {
        this.face = face;
        this.name = name;
        this.id = id;
        this.button = button;
    }

    public void setFace(Bitmap face){ this.face = face; }
    public void setName(String name){ this.name = name; }
    public void setId(String id){ this.id = id; }
    public void setButton(String button){ this.button = button; }



    public Bitmap getFace() {
        return face;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getButton() {
        return button;
    }
}



