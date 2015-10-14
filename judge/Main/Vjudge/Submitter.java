package Main.Vjudge;

import Main.Main;
import Main.OJ.OJ;
import Main.OJ.OTHOJ;
import Main.status.RES;
import Main.status.Result;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/21.
 */
/*
* 提交器
* */
public class Submitter implements Runnable{
    private SubmitInfo info;//正在处理的info
    private int status;//忙碌状态与否
    private String username;
    private String password;
    private int ojid;
    private String ojsrid;
    int submitterID;
    String showstatus="";

    private VJudge vj;

    public Submitter(int ojid,String us,String pas,int id,VJudge vj){
        this.ojid=ojid;
        username=us;
        password=pas;
        status=IDLE;
        submitterID=id;
        this.vj=vj;
    }
    public SubmitInfo getSubmitInfo(){return info;}
    public int getOjid() {
        return ojid;
    }
    public boolean isBusy(){
        return status==BUSY;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getOjsrid(){return ojsrid;}

    public void run(){//开始执行线程
        try {
            while(true){
                //读取队列执行
                //System.out.println(submitterID+"IDLE");
                this.info=vj.queue.get(ojid).take();
                this.status=BUSY;
                Main.status.setStatusResult(info.rid,Result.JUDGING,"-","-","");
                go();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void go() {
        try{
            //System.out.println(submitterID+":submitter go");
            showstatus="go";
            OTHOJ oj;
            oj = Main.ojs[ojid];
            String prid;
            int z=0;
            do{
                z++;
                if(z>=10){
                    //System.out.println(submitterID+":getPrid Error");
                    showstatus="get prid error";
                    Main.status.setStatusResult(info.rid, Result.ERROR, "-", "-", "");
                    this.status=IDLE;
                    return;
                }
                prid = oj.getRid(username);//获得原来的rid
                System.out.println(submitterID+":prid="+prid);
                showstatus="get"+z+" prid="+prid;
            }while(prid.equals("error"));
            String nrid ;
            RES r ;
            int num = 0;
            int k=2;
            do{
                while (oj.submit(this).equals(submitterID+":error")) {
                    num++;
                    if (num >= 10) {
                        //System.out.println(submitterID+":doSubmit out time");
                        showstatus="doSubmit error";
                        Main.status.setStatusResult(info.rid, Result.ERROR, "-", "-", "");
                        this.status=IDLE;
                        return;
                    }
                    Main.sleep(1000);
                    //System.out.println(submitterID+":doSubmit error");
                    showstatus="doSubmit"+num;
                }
                num = 0;
                do {
                    Main.sleep(1000);
                    nrid = oj.getRid(username);
                    //System.out.println(submitterID+":get rid "+num+"=" + nrid);
                    showstatus="get rid"+num+"="+nrid;
                    num++;
                    if (num == 10) break;//提交失败重新提交
                } while (nrid.equals(prid));
                k--;
            }while(num==10&&k!=0);
            if(k==0){
                Main.status.setStatusResult(info.rid, Result.ERROR, "-", "-", "");
            }else{
                ojsrid = nrid;
                do{
                    Main.sleep(1000);
                    r=oj.getResult(this);
                    //System.out.println(submitterID+":get res="+r.getR());
                    showstatus="get res="+r.getR();
                }while(!r.canReturn());
                Main.status.setStatusResult(info.rid, r.getR(),r.getTime(),r.getMemory(),r.getCEInfo());
            }
            this.status=IDLE;
        }catch(NullPointerException e){
            e.printStackTrace();
            Main.status.setStatusResult(info.rid, Result.ERROR, "-", "-", "");
            this.status=IDLE;
        }
    }

    public List<String> row(){
        //submitterID,ojid,status,username,info.rid,info.pid,ojsrid,show
        List<String> row=new ArrayList<String>();
        row.add(submitterID+"");
        row.add(ojid+"");
        row.add(status==1?"正在评测":"空闲");
        row.add(username);
        row.add(info==null?"":info.rid+"");
        row.add(info==null?"":info.pid+"");
        row.add(ojsrid+"");
        row.add(showstatus);
        return row;
    }

    public static final int BUSY=1;
    public static final int IDLE=0;
}
