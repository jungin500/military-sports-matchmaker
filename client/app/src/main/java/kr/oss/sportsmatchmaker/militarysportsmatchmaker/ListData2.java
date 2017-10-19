package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

/**
 * Created by Administrator on 2017-10-17.
 */
import android.graphics.Bitmap;
import android.widget.Button;

import java.util.Comparator;

public class ListData2 {
    private boolean existPic;
    private String name;
    private String id;
    private String button;

    public ListData2(boolean existPic, String name, String id, String button) {
        this.existPic = existPic;
        this.name = name;
        this.id = id;
        this.button = button;
    }
    public void setExistPic(boolean existPic) {this.existPic = existPic;}
    public void setName(String name){ this.name = name; }
    public void setId(String id){ this.id = id; }
    public void setButton(String button){ this.button = button; }


    public boolean getExistPic() { return existPic; }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getButton() {
        return button;
    }

    static class data2Comparator implements Comparator<ListData2> {
        @Override
        public int compare(ListData2 o1, ListData2 o2) {
            if (getStatus(o2.getButton()) != getStatus(o1.getButton())) return getStatus(o2.getButton()) - getStatus(o1.getButton());
            return getRank(o2.getId(), o2.getName()) - getRank(o1.getId(),o1.getName());
        }

        private int getStatus(String str){
            if (str.equals("방장")) return 3;
            if (str.equals("수락함")) return 2;
            if (str.equals("대기중")) return 1;
            if (str.equals("거절함")) return 0;
            return 4;
        }

        private int getRank(String id, String rankname){
            if (id.split("_")[0].equals("anon")) return -1;
            return RankHelper.rankToInt(rankname.split("")[0]);
        };
    }

}



