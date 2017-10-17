package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */
public class ListData {
    private String face;
    private String name;
    private String text;

    public ListData(String face, String name, String text) {
        this.face = face;
        this.name = name;
        this.text = text;
    }

    public String getFace() {
        return face;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}


