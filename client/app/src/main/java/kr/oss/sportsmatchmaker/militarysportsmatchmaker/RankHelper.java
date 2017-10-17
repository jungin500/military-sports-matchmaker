package kr.oss.sportsmatchmaker.militarysportsmatchmaker;


/**
 * Created by Administrator on 2017-10-17.
 */

public class RankHelper {
    public static final String[] ranks =
            {"이병", "일병", "상병", "병장", "하사", "중사", "상사", "원사", "준위", "소위",
             "중위", "대위", "소령", "중령", "대령", "준장", "소장", "중장", "대장"};

    public static int rankToInt(String rank){
        for(int i=0;i<ranks.length;i++) {
            if (rank.equals(ranks[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String intToRank(int i){
        return ranks[i];
    }

    public static int numRanks(){
        return ranks.length;
    }

}
