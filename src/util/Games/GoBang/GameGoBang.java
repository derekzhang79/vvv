package util.Games.GoBang;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.Games.BaseGame;
import util.Games.GameReadException;
import util.Games.IGamePlayer;
import util.Tool;

import java.util.ArrayList;

/**
 * 1表示黑子，2表示白子
 * 黑子先下
 * Created by syimlzhu on 2016/9/30.
 */
public class GameGoBang extends BaseGame{
    private int row;
    private int col;
    private int chessBoard[][];
    /**
     * 当前落子方  用1、2表示
     */
    private int nowPlayer;
    private JSONObject record;
    private int stepNum;

    public GameGoBang(int row,int col,IGamePlayer p1,IGamePlayer p2){
        this.row = row;
        this.col = col;
        chessBoard = new int[row][];
        for(int i=0;i<row;i++){
            chessBoard[i]=new int[col];
            for(int j=0;j<col;j++){
                chessBoard[i][j] = 0;
            }
        }
        record = new JSONObject();
        record.put("type","GoBang");
        record.put("row",row);
        record.put("col",col);
        record.put("step",new JSONArray());
        stepNum = 0;
        nowPlayer = 1;
        players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
    }

    @Override
    public int gameEnd(int status){
        Tool.debug("GameEnd("+status+")");
        return status;
    }

    @Override
    public void run() {
        try {
            players.get(0).putInt(1);
            players.get(1).putInt(2);
        }catch (GameReadException e) {
            gameEnd(-1);
            return ;
        }
        while(true){
            int x,y,otherPlayer;
            try {
                x = players.get(nowPlayer - 1).getInt();
                y = players.get(nowPlayer - 1).getInt();

                Tool.debug(nowPlayer+" ("+x+","+y+")");
                otherPlayer = 3 - nowPlayer;
            }catch (GameReadException e){
                gameEnd(nowPlayer + 2);
                return ;
            }
            int ret = set(x, y);
            if(ret == 3 || ret == 4) {
                //落子非法
                gameEnd(ret);
                return ;
            }

            try {
                players.get(otherPlayer -1).putInt(x);
                players.get(otherPlayer -1).putInt(y);
            } catch (GameReadException e) {
                gameEnd(otherPlayer + 2);
                return ;
            }
            if(ret != 0){
                gameEnd(ret);
                return ;
            }
            nowPlayer = 3 - nowPlayer;
        }
    }

    /**
     * 在位置i,j下一枚子
     * 返回胜利方
     * 如果当前方下在非法位置，直接判负
     * @param i 位置坐标i
     * @param j 位置坐标j
     * @return 0未分胜负，可以继续
     *         1黑方五连珠胜
     *         2白方五连珠胜
     *         3白方下子非法，黑方胜
     *         4黑方下子非法，白方胜
     *         -1平局
     */
    private int set(int i,int j){
        JSONObject aStep = new JSONObject();
        aStep.put("x",i);
        aStep.put("y",j);
        record.getJSONArray("step").add(aStep);
        stepNum++;
        if(i<0||i>=row||j<0||j>col||chessBoard[i][j] != 0){
            if(nowPlayer == 1){
                return 4;
            }else{
                return 3;
            }
        }
        chessBoard[i][j] = nowPlayer;
        return getVec(i,j);
    }

    /**
     * 判断刚刚落的子是否连成五颗
     * @param x 刚刚落完的子所在的x坐标
     * @param y 刚刚落完的子所在的y坐标
     * @return 1 连成5个
     *         0 未连成5个
     *         -1 平局(整个棋盘都已经下满)
     */
    private int getVec(int x,int y){
        int player = chessBoard[x][y];
        int next[][] = {
                {0,1},
                {1,1},
                {1,0},
                {1,-1}
        };
        for (int[] aNext : next) {
            int nowLength = 1;
            int nx = x, ny = y;
            while (true) {
                nx += aNext[0];
                ny += aNext[1];
                if (nx < 0 || nx >= row || ny < 0 || ny <= col || chessBoard[nx][ny] != player) {
                    break;
                } else {
                    nowLength++;
                }
            }
            while (true) {
                nx -= aNext[0];
                ny -= aNext[1];
                if (nx < 0 || nx >= row || ny < 0 || ny <= col || chessBoard[nx][ny] != player) {
                    break;
                } else {
                    nowLength++;
                }
            }
            if(nowLength>=5) return player;
        }
        if(stepNum == row*col){
            return -1;
        }
        return 0;
    }

    public JSONObject getRecord(){
        return record;
    }
}
