package util;

import entity.Log;
import entity.User;
import util.SQL.SQL;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 杂项功能
 * Created by Administrator on 2015/11/24 0024.
 */
public class Tool {
    /**
     * 让线程等待t毫秒
     * @param t 等待时间 单位毫秒
     * @return 1
     */
    public static int sleep(int t){
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            return 1;
        }
        return 1;
    }
    public static String nowDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }
    public static Timestamp now(){
        return new Timestamp(System.currentTimeMillis());
    }

    public static void log(String s){
        log(s,2);
    }
    public static void log(String s,int stackDepth){
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        User u = Main.loginUser();
        Thread current = Thread.currentThread();
        System.out.println("["+now()+"|"+current.getId()+"]["+(u==null?"null":u.getUsername())+"]"+s+"["+stacks[stackDepth]+"]");
    }

    public static void log(Exception e){
        try{
            ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            e.printStackTrace(new java.io.PrintWriter(buf, true));
            String expMessage = buf.toString();
            User loginUser=Main.loginUser();
            Log log=new Log(now(),expMessage,loginUser==null?null:loginUser.getUsername());
            Main.logs.save(log);
            if(Main.config.isDebug) e.printStackTrace();
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
    public static void debug(String s){
        debug(s,2);
    }
    public static void debug(String s,int stackDepth){
        if(Main.config.isDebug){
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            Thread current = Thread.currentThread();
            String nowTime = now().toString();
            while(nowTime.length() < 23) nowTime+="0";
            System.out.println(ANSI.CYAN+"【"+nowTime+"|"+current.getId()+"】"+ANSI.RESET+s+ANSI.GREEN+"["+stacks[stackDepth]+"]"+ANSI.RESET);
        }
    }
    public static void debug(String s,String className){
        if(Main.config.isDebug){
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            int stackDepth;
            for(stackDepth=1;stackDepth<stacks.length;stackDepth++){
                if(!stacks[stackDepth].getClassName().equals(className)
                        &&!stacks[stackDepth].getMethodName().equals("debug")
                        &&!stacks[stackDepth].getMethodName().equals("SQLDebug")) break;
            }
            Thread current = Thread.currentThread();
            String nowTime = now().toString();
            while(nowTime.length() < 23) nowTime+="0";
            System.out.println(ANSI.CYAN+"【"+nowTime+"|"+current.getId()+"】"+ANSI.RESET+s+ANSI.GREEN+"["+stacks[stackDepth]+"]"+ANSI.RESET);
        }
    }
    public static void SQLDebug(Long time, String sql){
        if(Main.config.isDebug){
            if(time < 10){
                debug("{"+time+"}"+sql,SQL.class.getName());
            }else if(time < 100){
                debug(ANSI.YELLOW+"{"+time+"}"+ANSI.RESET+sql,SQL.class.getName());
            }else{
                debug(ANSI.RED+"{"+time+"}"+ANSI.RESET+sql,SQL.class.getName());
            }
        }
    }
    public static Timestamp getTimestamp(String d,String s,String m){
        //System.out.println(d + " " + s + ":" + m + ":00");
        return Timestamp.valueOf(d + " " + s + ":" + m + ":00");
    }

    /**
     * 产生一个随机整数 范围在[l,r]内，是均匀分布
     * @param l 随机数下限
     * @param r 随机数上限
     * @return 随机整数
     */
    public static int randNum(int l,int r){
        if(l>r) return 0;
        return (int)(Math.random()*(r-l+1)+l);
    }

    private static final Random r = new Random();
    /**
     * 产生一个随机数，符合正态分布
     * @param miu 数学期望
     * @param sigma 方差
     * @return 随机double
     */
    public static double randomGaussian(double miu,double sigma){
        return r.nextGaussian() * sigma + miu;
    }
}
