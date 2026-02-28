package com.jump;

import android.app.Service;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;
import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageWriter;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Core;
import org.opencv.core.Size;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.OutputStream;
import java.util.Vector;

public class ClickService extends Service {

    private static final String TAG = "ClickService";
    public  Boolean Thread_run = false;
    public  Boolean finish_flag = false;
    public  Boolean Thread_break = false;
    public  Boolean Thread_pause = false;
    public  Boolean Thread_circle = true;
    public  Boolean Thread_restart = false;
    private int circle_cnt = 0;
    private MediaProjectionManager mMediaProjectionManager = null;
    private MediaProjection  mMediaProjection = null;
    private String atfolder;
    private String subfolder;
    private Vector<String> cmd_flow = new Vector<String>();
    private int delayms = 1000;
    private int click_time = 100;
    private int slide_time = 500;
    private int click_th = 80;
    private int adjustx = 0;
    private int adjusty = 0;
    public  int flow_while_times=0;
    public  int flow_while_cnt=0;
    public  int flow_while_index=0;
    public  Boolean flow_while_break=false;
    public  int flow_index=0;
    public  Boolean flow_break = false;
    public  Boolean flow_skip = false;
    private Boolean auto_resize = false;
    private double resize_value = 1;
    public  int flow_err = 0;
    public  String flow_err_cmd = "";
    public  int flow_err_last = 0;
    private ImageReader imageReader= null;
    private Intent screenCap_intent = null;
    Point point_check = new Point();
    private Intent AS_intent = new Intent("com.jump.myAccessibilityService");
    private Intent FT_intent = new Intent("com.jump.FloatWindowService");
    private Intent MA_intent = new Intent("com.jump.OpenCVActivity");
    public int widthPixels;
    public int heightPixels;
    public int hour_set = 0;
    public String current_flow="";
    public String heart_beat="home";
    public int heart_minute =30;
    private Vector<String> cmd_back_list = new Vector<String>();
    private Vector<String> cmd_home_list = new Vector<String>();
    private Vector<String> cmd_task_list = new Vector<String>();
    private Vector<String> cmd_my_list = new Vector<String>();
    private Vector<String> cmd_exit_list = new Vector<String>();
    private Vector<String> cmd_comb1_list = new Vector<String>();
    private Vector<String> cmd_comb2_list = new Vector<String>();
    private Vector<String> cmd_comb3_list = new Vector<String>();
    private Vector<String> cmd_comb4_list = new Vector<String>();
    private Vector<String> cmd_comb5_list = new Vector<String>();
    private Vector<String> flow_finish_list = new Vector<String>();
    private Vector<String> flow_err_list = new Vector<String>();
//     private final String screencap="screencap -p /sdcard/a01.png";
//     private final String key_home="input keyevent 3"; //home键
//     private final String tap_alipay="input tap 400 300";
//     private final String tap_ant="input tap 130 500";
//     private final String swipe="input swipe 500 2000 500 200 50";
//     private final String tap_friend="input tap 500 1550";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    public interface InterfaceMyBinder {
        public ClickService  getService();
    }
    public class MyBinder extends Binder implements InterfaceMyBinder{
        @Override
        public ClickService getService() {
            return ClickService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return  new MyBinder();//(IBinder)mBinder
    }
    public void start(Intent intent) {
        screenCap_intent = intent;
        Thread_run = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean at_flag= false;
                send_FloatW_msg(String.format("启动中"));
                at_flag = loadat("/sdcard/autotest/load.txt");
                flist_load("/sdcard/autotest/finish_list.txt");
                update_finish_list();
                Log.i(TAG, String.format("get_hour=%d,hour_set=%d",get_hour(),hour_set));
                boolean exeflag = false;
                while (Thread_run) {
                    current_flow="";
                    flow_break = false;
                    Thread_break = false;
                    if(at_flag)
                    {
                        if(finish_flag){
                            if(get_minute()%heart_minute==0){
                                execmd_Global_Action(heart_beat,"1");
                            }
                            send_FloatW_msg(String.format("已完成"));
                        }
                        else{
                            exeflag = exeflow(cmd_flow);
                            circle_cnt++;
                            if(!Thread_circle || exeflag){
                                finish_flag = true;
                                circle_cnt = 0;
                            }
                            else{
                                finish_flag = false;
                                if(circle_cnt==3) {
                                    finish_flag = true;
                                    circle_cnt = 0;
                                }
                            }
                        }
                        if(Thread_restart) {
                            finish_flag = false;
                            Thread_restart = false;
                            circle_cnt = 0;
                            send_FloatW_msg(String.format("自动重启！"));
                            continue;}
                    }
                    else
                        testcmd();
                    if(Thread_break) break;
                    try {
                        Thread.sleep(delayms);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                current_flow="";
                send_FloatW_msg(String.format("已停止"));
                Thread_run = false;
                Thread_break = false;
        }
        }).start();
    }
    public boolean get_Thread_run() {
        return  Thread_run;
    }
    public void set_Thread_break(boolean flag) {
        Thread_break = flag;
        Thread_pause = false;
    }
    private boolean loadat(String fileload) {
        try {
            subfolder = AES_Utils.read_firstline(fileload);
            atfolder = fileload.substring(0,fileload.lastIndexOf("/")+1)+subfolder;//
            Log.d(TAG, atfolder);
            File file= new File(atfolder+"autotest.at");
            if(!file.exists())
                return false;
            cmd_flow.removeAllElements();
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                cmd_flow.add(line);
                if(line.equals("subflow")){
                    String subflow;
                    if((subflow= br.readLine())!= null);{
                        cmd_flow.add(subflow);
                        subflow_load(atfolder+subflow);
                    }
                }
            }
            br.close();
            isr.close();
            fis.close();
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private boolean subflow_load(String fileload) {
        try {
            File file= new File(fileload);
            if(!file.exists())
                return false;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                cmd_flow.add(line);
                if(line.equals("subflow")){
                    String subflow;
                    if((subflow= br.readLine())!= null);{
                        cmd_flow.add(subflow);
                        subflow_load(atfolder+subflow);
                    }
                }
            }
            br.close();
            isr.close();
            fis.close();
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private boolean flist_load(String fileload) {
        try {
            File file= new File(fileload);
            if(!file.exists())
                return false;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            flow_finish_list.removeAllElements();
            String line;
            while ((line = br.readLine()) != null) {
                flow_finish_list.add(line);
            }
            br.close();
            isr.close();
            fis.close();
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private boolean flist_save() {
        String filesave = "/sdcard/autotest/finish_list.txt";
        try {
            File file= new File(filesave);

            FileOutputStream fis = new FileOutputStream(file);
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter br = new BufferedWriter(isr);
            int cnt = flow_finish_list.size();
            String line;
            for(int i=0;i<cnt;i++) {
                br.write(flow_finish_list.get(i)+"\n");
            }
            br.close();
            isr.close();
            fis.close();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private boolean elist_save() {
        String filesave = "/sdcard/autotest/error_list.txt";
        try {
            File file= new File(filesave);

            FileOutputStream fis = new FileOutputStream(file);
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter br = new BufferedWriter(isr);
            int cnt = flow_err_list.size();

            String line;
            for(int i=0;i<cnt;i++) {
                br.write(flow_err_list.get(i)+"\n");
            }
            br.close();
            isr.close();
            fis.close();
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private boolean log_save(String msg) {
        String filesave = "/sdcard/autotest/log.txt";
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        try {
            File file= new File(filesave);

            FileOutputStream fis = new FileOutputStream(file,true);
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter br = new BufferedWriter(isr);
            br.write(String.format("%4d-%2d-%2d %2d:%2d:%2d: %s\n",year,month,date,hour,minute,second,msg));
            br.close();
            isr.close();
            fis.close();
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    private int get_hour() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        //int year = c.get(Calendar.YEAR);
        //int month = c.get(Calendar.MONTH);
        //int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        //int minute = c.get(Calendar.MINUTE);
        //int second = c.get(Calendar.SECOND);
        return  hour;
    }
    private int get_minute() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        //int year = c.get(Calendar.YEAR);
        //int month = c.get(Calendar.MONTH);
        //int date = c.get(Calendar.DATE);
        //int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        //int second = c.get(Calendar.SECOND);
        return  minute;
    }

    public void set_finish(boolean flag) {
        if(flag)
            finish_flag = true;
        else
        {
            flow_finish_list.removeAllElements();
            flow_err_list.removeAllElements();
            flist_save();
            elist_save();
            finish_flag = false;
        }
    }
    private boolean exeflow(Vector<String> flow) {
            int size = flow.size();
            String cmd ;
            flow_err_list.removeAllElements();
            try {
            for(flow_index=0;flow_index<size;flow_index++)
            {
                cmd  = flow.get(flow_index);
                Log.i(TAG, String.format("exeflow num=%d %s",flow_index,cmd));
                flow_err_last = execmd(cmd);
                if(flow_err_last > 0) {
                    flow_break= true;
                    flow_err = flow_err_last;
                    flow_err_cmd = cmd;
                    Log.i(TAG, String.format("err=%d",flow_err_last));
                    continue;}
                if(Thread_break) return false;
                while(Thread_pause) SystemClock.sleep(1000);
            }
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        if(flow_err_list.size()>0) return false;
        return true;
    }
    private int execmd(String cmd) {
        int err = 0;
        String cmd_type="",cmd_para1="",cmd_para2="",cmd_tmp="";
        if(cmd.indexOf("+")>0) {
            cmd_type = cmd.substring(0,cmd.indexOf("+"));
            cmd_tmp = cmd.substring(cmd.indexOf("+")+1,cmd.length());
            if(cmd_tmp.indexOf("+")>0) {
                cmd_para1 = cmd_tmp.substring(0,cmd_tmp.indexOf("+"));
                cmd_para2 = cmd_tmp.substring(cmd_tmp.indexOf("+")+1,cmd_tmp.length());
            }
            else
                cmd_para1 = cmd_tmp;
        }
        else
            cmd_type = cmd;
        int flow_on;
        switch (cmd_type) {
            case "n":
                current_flow = cmd_para1;
                flow_on= Integer.parseInt(cmd_para2);
                if(check_flow(current_flow) || flow_on==0) {flow_skip = true;Log.d(TAG, String.format("%s skip!!",current_flow));}
                break;
            case "wh":
                flow_while_times= Integer.parseInt(cmd_para1);
                flow_while_index = flow_index;
                break;
            case "wb":
                if(flow_err_last==0)
                    flow_while_break = true;
                else{
                    flow_break = false;flow_err=0;
                }
                break;
            case "wn":
                if(flow_err_last>0){
                    flow_while_break = true;
                    flow_break = false;flow_err=0;
                }
                break;
            case "we":
                flow_while_cnt++;
                if(flow_while_break || flow_while_cnt>= flow_while_times)
                    flow_while_cnt = 0;
                else
                    flow_index = flow_while_index;
                flow_while_break = false;
                Log.d(TAG, String.format("we: %d %d",flow_while_times,flow_while_cnt));
                break;
            case "e":
                if(flow_err==0) {
                    if(!check_flow(current_flow)) {
                        flow_finish_list.add(current_flow);
                        flist_save();
                    }
                    send_finish_list(current_flow+"\n");
                    //update_finish_list();
                    }
                else{
                    flow_err_list.add(current_flow);
                    elist_save();
                }
                log_save( String.format("flow:%s cmd:%s error:%d",current_flow,flow_err_cmd,flow_err));
                flow_skip= false; flow_break = false;flow_err=0;break;
            default: break;
        }

        if(flow_break || flow_while_break || flow_skip) return 0;

        Log.d(TAG, String.format("flow:%s execmd:%s %s %s",current_flow,cmd_type,cmd_para1,cmd_para2));
        send_FloatW_msg(cmd_type);
        switch (cmd_type)
        {
            case "ga":
                err =execmd_Global_Action(cmd_para1,cmd_para2);break;
            case "b":
                err =execmd_save(cmd_para1);break;
            case "f":
                err =execmd_find(cmd_para1,cmd_para2);break;
            case "c":
                err =execmd_click(cmd_para1,cmd_para2);break;
            case "fx":
                err =execmd_find_adjust(cmd_para1,cmd_para2);break;
            case "fm":
                err =execmd_comb_click(cmd_para1,cmd_para2);break;
            case "x":
                err =execmd_click_adjust(cmd_para1,cmd_para2);break;
            case "cp":
                err =execmd_click_position(cmd_para1,cmd_para2);break;
            case "s":
                err =execmd_slide(cmd_para1,cmd_para2);break;
            case "sx":
                err =execmd_slidex(cmd_para1,cmd_para2);break;
            case "sy":
                err =execmd_slidey(cmd_para1,cmd_para2);break;
            case "ck":
                err =execmd_check(cmd_para1,cmd_para2);break;
            case "cka":
                err =execmd_check_adjust(cmd_para1,cmd_para2);break;
            case "w":
                err =execmd_wait(cmd_para1,cmd_para2);break;
            case "d":
                err =set_delayms(cmd_para1);break;
            case "t":
                err =set_th(cmd_para1);break;
            case "m":
                err =set_ctime(cmd_para1);break;
            case "ms":
                err =set_stime(cmd_para1);break;
            case "sr":
                err =set_resize(cmd_para1,cmd_para2);break;
            case "sc":
                err =set_comb(cmd_para1,cmd_para2);break;
            case "sf":
                err =set_finish(cmd_para1);break;
            case "cc":
                err =clear_comb(cmd_para1);break;
            case "a":
                err =set_adjust(cmd_para1,cmd_para2);break;
            case "ht":
                err =set_ht(cmd_para1,cmd_para2);break;
        default: break;
    }
        return err;
}
    private boolean check_flow(String flow) {
        for(int i=0;i<flow_finish_list.size();i++)
        {
            if(flow.equals(flow_finish_list.get(i))) return true;
        }
        return false;
    }
    private int execmd_Global_Action(String para1,String para2) {
        int err = 0;
        int cnt= Integer.parseInt(para2);
        send_FloatW_msg(String.format("ga %s %s ",para1,para2));
        for(int i=0;i<cnt;i++)
        {
            send_action(para1);
            SystemClock.sleep(delayms);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        return 0;
    }
    private int execmd_find(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        dstMat = Imgcodecs.imread(atfolder+para1);
        screenMat = startscreenCap(screenCap_intent,false,"");
        send_FloatW_msg(String.format("f"));
        if(screenMat.empty() || dstMat.empty())
            return 0;
        point = getposition_smart(screenMat,dstMat);
        if(point.x==0 && point.y==0)
            return 0;
        for(int i=0;i<cnt;i++)
        {
            Log.i(TAG, String.format("execmd_find %d",i));
            send_FloatW_msg(String.format("f %d",i));
            send_click(point.x,point.y,1,click_time);
            int randint;
            double randfloat = Math.random()*100;
            randint = (int) randfloat;
            SystemClock.sleep(delayms+randint);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private int execmd_click(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        dstMat = Imgcodecs.imread(atfolder+para1);
        screenMat = startscreenCap(screenCap_intent,false,"");
        send_FloatW_msg(String.format("c"));
        if(screenMat.empty())
            return 1;
        if(dstMat.empty())
            return 2;
        point = getposition_smart(screenMat,dstMat);
        if(point.x==0 && point.y==0)
            return 3;
        for(int i=0;i<cnt;i++)
        {
            Log.i(TAG, String.format("execmd_click %d",i));
            send_FloatW_msg(String.format("c %d",i));
            send_click(point.x,point.y,1,click_time);
            int randint;
            double randfloat = Math.random()*100;
            randint = (int) randfloat;
            SystemClock.sleep(delayms+randint);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private Point find_comb_list(Mat src,Vector<String> cmd_comb_list) {
        Mat dstMat = new Mat();
        Point point = new Point();
        for(int i=0;i<cmd_comb_list.size();i++) {
            dstMat = Imgcodecs.imread(atfolder+cmd_comb_list.get(i));
            if(dstMat.empty()) continue;
            point = getposition_smart(src, dstMat);
            if(point.x!=0 || point.y!=0) break;
        }
        dstMat.release();
        return point;
    }
    private int execmd_comb_click(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        screenMat = startscreenCap(screenCap_intent,false,"");
        if(screenMat.empty())
            return 0;

        switch (para1) {
            case "back":
                point = find_comb_list(screenMat,cmd_back_list);
                break;
            case "home":
                point = find_comb_list(screenMat,cmd_home_list);
                break;
            case "task":
                point = find_comb_list(screenMat,cmd_task_list);
                break;
            case "my":
                point = find_comb_list(screenMat,cmd_my_list);
                break;
            case "exit":
                point = find_comb_list(screenMat,cmd_exit_list);
                break;
            case "comb1":
                point = find_comb_list(screenMat,cmd_comb1_list);
                break;
            case "comb2":
                point = find_comb_list(screenMat,cmd_comb2_list);
                break;
            case "comb3":
                point = find_comb_list(screenMat,cmd_comb3_list);
                break;
            case "comb4":
                point = find_comb_list(screenMat,cmd_comb4_list);
                break;
            case "comb5":
                point = find_comb_list(screenMat,cmd_comb5_list);
                break;
            default:break;
        }
        if(point.x==0 && point.y==0)  return 0;
        for(int i=0;i<cnt;i++)
        {
            Log.i(TAG, String.format("execmd_comb %s %d",para1,i));
            send_FloatW_msg(String.format("fm %s %d",para1,i));
            send_click(point.x,point.y,1,click_time);
            int randint;
            double randfloat = Math.random()*100;
            randint = (int) randfloat;
            SystemClock.sleep(delayms+randint);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private int execmd_click_position(String para1,String para2) {
        int err = 0;
        int x= Integer.parseInt(para1);
        int y= Integer.parseInt(para2);

        send_FloatW_msg(String.format("cp"));
        Log.i(TAG, String.format("execmd_click_position x=%d y=%d",x,y));
        send_click(x,y,1,click_time);
        SystemClock.sleep(delayms);
        return 0;
    }
    private int execmd_find_adjust(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        dstMat = Imgcodecs.imread(atfolder+para1);
        screenMat = startscreenCap(screenCap_intent,false,"");
        send_FloatW_msg(String.format("c"));
        if(screenMat.empty())
            return 0;
        if(dstMat.empty())
            return 0;
        point = getposition_smart(screenMat,dstMat);
        if(point.x==0 && point.y==0)
            return 0;
        for(int i=0;i<cnt;i++) {
            Log.i(TAG, String.format("execmd_find_adjust %d", i));
            send_FloatW_msg(String.format("fx %d", i));
            send_click(point.x + adjustx, point.y + adjusty, 1, click_time);
            int randint;
            double randfloat = Math.random() * 100;
            randint = (int) randfloat;
            SystemClock.sleep(delayms + randint);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private int execmd_click_adjust(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        dstMat = Imgcodecs.imread(atfolder+para1);
        screenMat = startscreenCap(screenCap_intent,false,"");
        send_FloatW_msg(String.format("c"));
        if(screenMat.empty())
            return 1;
        if(dstMat.empty())
            return 2;
        point = getposition_smart(screenMat,dstMat);
        if(point.x==0 && point.y==0)
            return 3;
        for(int i=0;i<cnt;i++) {
            Log.i(TAG, String.format("execmd_click_adjust %d", i));
            send_FloatW_msg(String.format("x %d", i));
            send_click(point.x + adjustx, point.y + adjusty, 1, click_time);
            int randint;
            double randfloat = Math.random() * 100;
            randint = (int) randfloat;
            SystemClock.sleep(delayms + randint);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private int execmd_check(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        int cnt= Integer.parseInt(para2);
        dstMat = Imgcodecs.imread(atfolder+para1);
        for(int i=0;i<cnt;i++) {
            screenMat = startscreenCap(screenCap_intent, false, "");
            send_FloatW_msg(String.format("ck"));
            if (screenMat.empty())
                return 1;
            if (dstMat.empty())
                return 2;
            point = getposition_smart(screenMat, dstMat);
            point_check = point;
            if (point.x == 0 && point.y == 0)
                return 3;
            SystemClock.sleep(delayms);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        screenMat.release();
        dstMat.release();
        return 0;
    }
    private int execmd_check_adjust(String para1,String para2) {
        int pvalue = Integer.parseInt(para2);
        switch (para1) {
            case "l":
                if(point_check.x > pvalue)
                    send_slide(widthPixels * 3 / 4, heightPixels / 2, widthPixels / 2, heightPixels / 2, slide_time);
                break;
            case "r":
                if(point_check.x < pvalue)
                    send_slide(widthPixels / 4, heightPixels / 2, widthPixels / 2, heightPixels / 2, slide_time);
                break;
            case "u":
                if(point_check.y > pvalue)
                    send_slide(widthPixels / 2, heightPixels * 3 / 4, widthPixels / 2, heightPixels / 2, slide_time);
                break;
            case "d":
                if(point_check.y < pvalue)
                    send_slide(widthPixels / 2, heightPixels / 4, widthPixels / 2, heightPixels / 2, slide_time);
                break;
                default:break;
        }
        return 0;
    }
    private int execmd_slide(String para1,String para2) {
        int err = 0;
        int cnt= Integer.parseInt(para2);
        send_FloatW_msg(String.format("s"));
        try {
            for(int i=0;i<cnt;i++)
            {
                Log.i(TAG, String.format("execmd_slide %s %d",para1,i));
                send_FloatW_msg(String.format("s %s %d",para1,i));
                switch (para1)
                {
                    case "l":
                        send_slide(widthPixels*3/4,heightPixels/2,widthPixels/4,heightPixels/2,slide_time);
                        break;
                    case "r":
                        send_slide(widthPixels/4,heightPixels/2,widthPixels*3/4,heightPixels/2,slide_time);
                        break;
                    case "u":
                        send_slide(widthPixels/2,heightPixels*3/4,widthPixels/2,heightPixels/8,slide_time);
                        break;
                    case "d":
                        send_slide(widthPixels/2,heightPixels/4,widthPixels/2,heightPixels*7/8,slide_time);
                        break;
                    case "bu":
                        send_slide(widthPixels/2,heightPixels-1,widthPixels/2,heightPixels/2,slide_time);
                        break;
                    case "td":
                        send_slide(widthPixels/2,0,widthPixels/2,heightPixels/2,slide_time);
                        break;
                    case "lr":
                        send_slide(0,heightPixels/2,widthPixels*3/4,heightPixels/2,slide_time);
                        break;
                    case "rl":
                        send_slide(widthPixels-1,heightPixels/2,widthPixels/4,heightPixels/2,slide_time);
                        break;
                    case "hl":
                        send_slide(widthPixels*3/4,heightPixels/4,widthPixels/4,heightPixels/4,slide_time);
                        SystemClock.sleep(500);
                        send_slide(widthPixels*3/4,heightPixels*3/4,widthPixels/4,heightPixels*3/4,slide_time);
                        break;
                    case "hr":
                        send_slide(widthPixels/4,heightPixels/4,widthPixels*3/4,heightPixels/4,slide_time);
                        SystemClock.sleep(500);
                        send_slide(widthPixels/4,heightPixels*3/4,widthPixels*3/4,heightPixels*3/4,slide_time);
                        break;
                    case "hu":
                        send_slide(widthPixels/2,heightPixels*3/8,widthPixels/2,heightPixels/8,slide_time);
                        SystemClock.sleep(500);
                        send_slide(widthPixels/2,heightPixels*7/8,widthPixels/2,heightPixels*5/8,slide_time);
                        break;
                    case "hd":
                        send_slide(widthPixels/2,heightPixels/8,widthPixels/2,heightPixels*3/8,slide_time);
                        SystemClock.sleep(500);
                        send_slide(widthPixels/2,heightPixels*5/8,widthPixels/2,heightPixels*7/8,slide_time);
                        break;
                    default: break;
                }

                int randint;
                double randfloat = Math.random()*300;
                randint = (int) randfloat;
                SystemClock.sleep(delayms+randint);
                while(Thread_pause) SystemClock.sleep(1000);
                if(Thread_break) break;

            }
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return err;
    }
    private int execmd_slidex(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        dstMat = Imgcodecs.imread(atfolder+para1);
        send_FloatW_msg(String.format("sx"));
        screenMat = startscreenCap(screenCap_intent, false, "");
        if (screenMat.empty())
            return 1;
        if (dstMat.empty())
            return 2;
        point = getposition_smart(screenMat, dstMat);
        if (point.x == 0 && point.y == 0)
            return 0;
        switch (para2)
        {
            case "l":send_slide(widthPixels*3/4,point.y,widthPixels/4,point.y,slide_time);break;
            case "r":
                send_slide(widthPixels/4,point.y,widthPixels*3/4,point.y,slide_time);
                break;
            case "pl":send_slide(point.x,point.y,0,point.y,slide_time);break;
            case "pr":
                send_slide(point.x,point.y,widthPixels,point.y,slide_time);
                break;
            default: break;
        }
        SystemClock.sleep(delayms);
        screenMat.release();
        dstMat.release();
        return err;
    }
    private int execmd_slidey(String para1,String para2) {
        int err = 0;
        Mat screenMat = new Mat();
        Mat dstMat = new Mat();
        Point point = new Point();
        dstMat = Imgcodecs.imread(atfolder+para1);
        send_FloatW_msg(String.format("sx"));
        screenMat = startscreenCap(screenCap_intent, false, "");
        if (screenMat.empty())
            return 1;
        if (dstMat.empty())
            return 2;
        point = getposition_smart(screenMat, dstMat);
        if (point.x == 0 && point.y == 0)
            return 0;
        switch (para2)
        {
            case "u":
                send_slide(point.x,heightPixels*3/4,point.x,heightPixels/8,slide_time);
                break;
            case "d":
                send_slide(point.x,heightPixels/4,point.x,heightPixels*7/8,slide_time);
                break;
            case "pu":
                send_slide(point.x,point.y,point.x,0,slide_time);
                break;
            case "pd":
                send_slide(point.x,point.y,point.x,heightPixels,slide_time);
                break;
            default: break;
        }
        while(Thread_pause) SystemClock.sleep(1000);
        SystemClock.sleep(delayms);
        screenMat.release();
        dstMat.release();
        return err;
    }
    private int execmd_save(String para1) {
        int err = 0;
        Mat screenMat = new Mat();
        screenMat = startscreenCap(screenCap_intent, true,atfolder + para1);
        send_FloatW_msg(String.format("b"));
        screenMat.release();
        return err;
    }
    private int execmd_wait(String para1,String para2) {
        int err = 0;
        int ms= Integer.parseInt(para1);
        int cnt=1;
        if(para2.equals("")) cnt=1;
        else cnt= Integer.parseInt(para2);
        if(cnt<1) cnt=1;
        for(int i=0;i<cnt;i++)
        {
            send_FloatW_msg(String.format("w %s %d",para1,i));
            SystemClock.sleep(ms);
            while(Thread_pause) SystemClock.sleep(1000);
            if(Thread_break) break;
        }
        return err;
    }
    private int set_delayms(String para1) {
        int err = 0;
        int ms= Integer.parseInt(para1);
        delayms = ms;
        return err;
    }
    private int set_th(String para1) {
        int err = 0;
        int th= Integer.parseInt(para1);
        click_th = th;
        return err;
    }
    private int set_resize(String para1,String para2) {
        int err = 0;
        int th= Integer.parseInt(para1);
        resize_value = Float.parseFloat(para2);
        if(resize_value<0.5) resize_value = 0.5;
        if(resize_value>2) resize_value = 2;
        if(th==0)
            auto_resize = false;
        else
            auto_resize = true;
        return err;
    }
    private int clear_comb(String para1) {
        int err = 0;
        switch (para1) {
            case "back":
                cmd_back_list.removeAllElements();
                break;
            case "home":
                cmd_home_list.removeAllElements();
                break;
            case "task":
                cmd_task_list.removeAllElements();
                break;
            case "my":
                cmd_my_list.removeAllElements();
                break;
            case "exit":
                cmd_exit_list.removeAllElements();
                break;
            case "comb1":
                cmd_comb1_list.removeAllElements();
                break;
            case "comb2":
                cmd_comb2_list.removeAllElements();
                break;
            case "comb3":
                cmd_comb3_list.removeAllElements();
                break;
            case "comb4":
                cmd_comb4_list.removeAllElements();
                break;
            case "comb5":
                cmd_comb5_list.removeAllElements();
                break;
            default:break;
        }
        return err;
    }
    private int set_comb(String para1,String para2) {
        int err = 0;
        switch (para1) {
            case "back":
                cmd_back_list.add(para2);
                break;
            case "home":
                cmd_home_list.add(para2);
                break;
            case "task":
                cmd_task_list.add(para2);
                break;
            case "my":
                cmd_my_list.add(para2);
                break;
            case "exit":
                cmd_exit_list.add(para2);
                break;
            case "comb1":
                cmd_comb1_list.add(para2);
                break;
            case "comb2":
                cmd_comb2_list.add(para2);
                break;
            case "comb3":
                cmd_comb3_list.add(para2);
                break;
            case "comb4":
                cmd_comb4_list.add(para2);
                break;
            case "comb5":
                cmd_comb5_list.add(para2);
                break;
            default:break;
        }
        return err;
    }
    private int set_ht(String para1,String para2) {
        int err = 0;
        heart_beat = para1;
        heart_minute= Integer.parseInt(para2);
        return err;
    }
    private int set_ctime(String para1) {
        int err = 0;
        int time= Integer.parseInt(para1);
        click_time = time;
        return err;
    }
    private int set_stime(String para1) {
        int err = 0;
        int time= Integer.parseInt(para1);
        slide_time = time;
        return err;
    }
    private int set_finish(String para1) {
        flow_finish_list.add(para1);
        return 0;
    }
    private int set_adjust(String para1,String para2) {
        int err = 0;
        if(para1!="") adjustx= Integer.parseInt(para1);
        if(para2!="") adjusty= Integer.parseInt(para2);
        return err;
    }
    private void testcmd() {
        atfolder = "/sdcard/autotest/default/";
        execmd("c+default.png+1");
        execmd("w+5000");
    }
    private int getDis() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat edgeMat = new Mat();

        BitmapFactory.Options opts = new BitmapFactory.Options();//保证图片为原尺寸
        opts.inScaled = false;

        String path = Environment.getExternalStorageDirectory() + "/01.png";
        //String path = "/01.png";
        Bitmap srcBitmap = BitmapFactory.decodeFile(path, opts);
        Utils.bitmapToMat(srcBitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(grayMat, edgeMat, 20, 100);

        //起始点（x1，y1）
        int x1 = 0;
        int y1 = 100000;
        for (int i = 400; i < grayMat.rows() - 200; i++) {
            for (int j = 50; j < grayMat.cols() - 50; j++) {
                if (grayMat.get(i, j)[0] < 60 && grayMat.get(i + 150, j)[0] < 60 && y1 > i) {
                    y1 = i;
                    x1 = j;
                }
            }
        }

        //终点（x2，y2）
        int x2 = 0;
        int y2 = 100000;
        for (int i = 400; i < edgeMat.rows() - 200; i++) {
            for (int j = 50; j < edgeMat.cols() - 50; j++) {
                if (edgeMat.get(i, j)[0] == 255 && y2 > i) {
                    y2 = i;
                    x2 = j;
                }
            }
        }
        //修正位置
        y1 = y1 + 200;

        int dis = (int) (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * 1.35);
        dis=dis>1500?1500:dis;
        Log.e(TAG, "onClick: " + x1 + "aaa" + y1 + "aaa" + x2 + "aaa" + y2 + "aaa" + dis);
        return dis;
    }
    public static void exec_root(int dis) {

        //破解反外挂机制
        int aa = (int) (500 + 300 * Math.random());
        int bb = (int) (1360 + 140 * Math.random());
//        String cmd = "input tap 125 340 \n";
        String cmd = "input swipe" + " " + aa + " " + bb + " " + aa + " " + bb + " " + dis + " " + "\n";
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void screenCap_root() {
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            String cmd = "screencap -p /sdcard/01.png";
            //String cmd = "screencap -p /01.png";
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Point getposition(Mat srcMat,Mat dstMat) {
        Point point = new Point();
        Mat srcMat_gray= new Mat(),dstMat_gray= new Mat();
        if(srcMat.channels()>1)
            Imgproc.cvtColor(srcMat,srcMat_gray,Imgproc.COLOR_RGB2GRAY);
        else
            srcMat_gray = srcMat;
        if(dstMat.channels()>1)
            Imgproc.cvtColor(dstMat,dstMat_gray,Imgproc.COLOR_RGB2GRAY);
        else
            dstMat_gray = dstMat;
        Mat outputImage = new Mat(srcMat_gray.rows(), srcMat_gray.cols(), srcMat_gray.type());
        Imgproc.matchTemplate(srcMat_gray, dstMat_gray, outputImage, Imgproc.TM_CCOEFF_NORMED);

        // 获取匹配结果,查找最大匹配值
        Core.MinMaxLocResult result = Core.minMaxLoc(outputImage);
        org.opencv.core.Point matchLoc = result.maxLoc;
        double similarity = result.maxVal; //匹配度
        if(similarity*100 > click_th)
        {
            point.x = (int) matchLoc.x + dstMat_gray.cols()/2; //小图大大图中的x坐标
            point.y = (int) matchLoc.y + dstMat_gray.rows()/2; //小图大大图中的y坐标
        }
       else
        {
            point.x = 0; //
            point.y = 0; //
        }
        Log.d(TAG, String.format("point:%d %d;similarity:%f,click_th:%d",point.x,point.y,similarity,click_th));
        outputImage.release();
        srcMat_gray.release();
        dstMat_gray.release();
        return point;
    }
    private Point getposition_smart(Mat srcMat,Mat dstMat) {
        Point point = new Point();
        if(auto_resize){
            point = getposition(srcMat,dstMat);
            if(point.x==0 && point.y==0) {
                Mat resize_mat= new Mat();
                double resize_r;
                for(int i=0;i<4;i++){
                    resize_r = resize_value+i*0.02;
                    Imgproc.resize(dstMat,resize_mat,new Size(0,0),resize_r,resize_r,3);
                    point = getposition(srcMat,resize_mat);
                    if(point.x!=0 || point.y!=0) break;
                }
                resize_mat.release();
            }
        }
        else
            point = getposition(srcMat,dstMat);
        return point;
    }
    public Mat startscreenCap(Intent intent,boolean save_flag,String path) {

        Mat screenMat = new Mat();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);//
        //获取令牌
        mMediaProjection = mMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent);
        //这里延迟一会再取
        SystemClock.sleep(100);
        //配置ImageReader
        screenMat = configImageReader(save_flag, path);

        return screenMat;
    }
    private Mat configImageReader(boolean save_flag,String path) {
        Mat rgbMat = new Mat();
        /*WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        int width = size.x;
        int height = size.y;*/
        DisplayMetrics dm = new DisplayMetrics();
        dm =  getResources().getDisplayMetrics();
        dm.widthPixels = widthPixels;
        dm.heightPixels = heightPixels;
        Log.d(TAG, String.format("widthPixels =%d,heightPixels =%d,densityDpi =%d",widthPixels,heightPixels,dm.densityDpi));
        imageReader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 1);
       //把内容投射到ImageReader 的surface
        if (null != mMediaProjection) {
           mMediaProjection.createVirtualDisplay(
                    TAG, dm.widthPixels, dm.heightPixels, dm.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, imageReader.getSurface(), null, null
            );
            SystemClock.sleep(500);

            rgbMat = ImageReadertoMat(imageReader,save_flag,path);
            //if(save_flag)
            //    save_imageReader(imageReader,path);
            mMediaProjection.stop();
        }
        return rgbMat;
    }
    public  Mat ImageReadertoMat(ImageReader reader,boolean save_flag,String path) {
        Mat rgbMat = new Mat();
        Mat roiMat = new Mat();
        Mat rMat = new Mat();
        Image image = reader.acquireLatestImage();
        org.opencv.core.Rect roi = new org.opencv.core.Rect();

        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            Utils.bitmapToMat(bitmap, rgbMat);
            roi.x= rowPadding / pixelStride/2;
            roi.y= 0;
            roi.width= width;
            roi.height= height;
            roiMat = rgbMat.submat(roi);
            image.close();
            Imgproc.cvtColor(roiMat,rMat,Imgproc.COLOR_BGR2RGB);
            if(save_flag && path!="") //保存图片
            {
                Log.d(TAG, String.format("path:%s",path));
                Imgcodecs.imwrite(path, rMat);
                Log.d(TAG, String.format("image save!widthPixels =%d,heightPixels =%d,rowPadding =%d,pixelStride =%d",width,height,rowPadding,pixelStride));

            }
        }
        rgbMat.release();
        roiMat.release();
        return rMat;
    }

    /*
     * 保存图片
     */
    public void save_imageReader(ImageReader reader,String path) {

        Image image = reader.acquireLatestImage();
        //String path = Environment.getExternalStorageDirectory() + "/snap.jpg";
        //String path =  "/sdcard/snap.jpg";
        Log.d(TAG, String.format("path:%s",path));
        if(path=="") return;
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            //保存图片到本地

            File filePic = new File(path);
            Log.d(TAG, String.format("image save!  widthPixels =%d,heightPixels =%d,rowPadding =%d,pixelStride =%d",width,height,rowPadding,pixelStride));
            try {
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs();
                    filePic.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filePic);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.d(TAG, "New image available from virtual display.");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //记得关闭 image
            try {
                image.close();
            } catch (Exception e) {
            }
        }
    }
    public void send_action(String action) {
        int Point_Array[] ={0,0,0,0,1,100};
        AS_intent.putExtra("type", action);
        AS_intent.putExtra("Point_Array", Point_Array);
        sendBroadcast(AS_intent);
        SystemClock.sleep(100);
    }
    public void send_click(int x,int y,int cnt,int time) {
        int Point_Array[] ={x,y,x,y,1,time};
        AS_intent.putExtra("type", "click");
        AS_intent.putExtra("Point_Array", Point_Array);
        sendBroadcast(AS_intent);
        SystemClock.sleep(100);
    }
    public void send_slide(int x1,int y1,int x2,int y2,int time) {
        int Point_Array[] ={x1,y1,x2,y2,1,time};
        AS_intent.putExtra("type", "swape");
        AS_intent.putExtra("Point_Array", Point_Array);
        sendBroadcast(AS_intent);
        SystemClock.sleep(100);
    }
    public void send_FloatW_msg(String msg) {
        FT_intent.putExtra("msg", String.format("%s %s",current_flow,msg));
        sendBroadcast(FT_intent);
        SystemClock.sleep(100);
    }
    public void send_finish_list(String msg) {
        MA_intent.putExtra("click", false);
        MA_intent.putExtra("msg", msg);
        sendBroadcast(MA_intent);
        SystemClock.sleep(100);
    }
    public void update_finish_list() {
        send_finish_list("clear");
        for(int i=0;i<flow_finish_list.size();i++)
        {
            send_finish_list(flow_finish_list.get(i)+"\n");
        }
    }
    public void delete_allfile(String path) {

    }
}
