package com.jump;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.EditText;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import com.jump.ClickService;
import static java.lang.Math.sqrt;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import android.provider.Settings;

import com.jump.FloatWindowManager;
import com.jump.FloatWindowService;
import com.jump.FloatWindowSmallView.OnClickListener;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class OpenCVActivity extends AppCompatActivity {

    private static final String TAG = "OpenCVActivity";
    private  ClickService.InterfaceMyBinder mClickServiceBinder = null;
    private ClickService mClickService = null;
    private MediaProjectionManager mMediaProjectionManager = null;
    private myAccessibilityService mAccessibilityService = null;
    private MediaProjection mMediaProjection = null;
    private Timer timer;
    private Button start;
    private Button stop;
    private Button clear;
    private TextView status;
    private ImageView img;
    private Switch sbar;
    private Switch nbar;
    private Switch finish;
    private Switch circle;
    private EditText hour;
    private EditText f_list;
    private EditText mac;
    private boolean sbar_flag = false;
    private boolean nbar_flag = true;
    private boolean start_flag = false;
    private FloatWindowManager floatWindowManager;
    private Context context;
    private MsgReceiver msgReceiver;
    private int widthPixels;
    private int heightPixels;
    private String Mac_string;
    private String endcode_key="xikexuan";
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        //img = (ImageView) findViewById(R.id.img);
        start = (Button) findViewById(R.id.btn);
        stop = (Button) findViewById(R.id.stop);
        clear = (Button) findViewById(R.id.clear);
        finish = (Switch) findViewById(R.id.finish);
        nbar = (Switch) findViewById(R.id.nbar);
        sbar = (Switch) findViewById(R.id.sbar);
        circle = (Switch) findViewById(R.id.circle);
        status = (TextView) findViewById(R.id.status);
        hour = (EditText) findViewById(R.id.hour);
        f_list = (EditText) findViewById(R.id.f_list);
        mac = (EditText) findViewById(R.id.mac);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jump.OpenCVActivity");
        registerReceiver(msgReceiver, intentFilter);
        bindService(new Intent(OpenCVActivity.this, ClickService.class), connection, Context.BIND_AUTO_CREATE);
        context = this;
        widthPixels = ScreenUtils.getScreenWidth(context);
        heightPixels = refresh_heightPixels();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_flag = true;
                if(!check_lisence("/sdcard/autotest/lisence.txt")){
                    Toast.makeText(getApplicationContext(), "配置出错！", Toast.LENGTH_SHORT).show();
                    return;
                }
                autotest_start();
                if(mClickService.finish_flag) finish.setChecked(true); else finish.setChecked(false);
                Toast.makeText(getApplicationContext(), "自动测试启动！", Toast.LENGTH_SHORT).show();
                start_timer();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_flag = false;
                autotest_stop();
                Toast.makeText(getApplicationContext(), "自动测试停止！", Toast.LENGTH_SHORT).show();
                stop_timer();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickService.set_finish(false);
                finish.setChecked(false);
                f_list.setText("");
                Toast.makeText(OpenCVActivity.this, "任务记录已清空！", Toast.LENGTH_SHORT).show();
            }
        });
        finish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mClickService.finish_flag = finish.isChecked();
            }
        });
        circle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mClickService.Thread_circle = circle.isChecked();
            }
        });
        sbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(sbar.isChecked()) sbar_flag = true;else sbar_flag = false;
                heightPixels = refresh_heightPixels();
            }
        });
        nbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nbar.isChecked()) nbar_flag = true;else nbar_flag = false;
                heightPixels = refresh_heightPixels();
            }
        });
        checkPermission();
        show_small();
        Mac_string = AES_Utils.getMacAddress();
        mac.setText(Mac_string);
        Log.i(TAG, String.format("Mac=%s",Mac_string));
        Toast.makeText(OpenCVActivity.this, "服务准备完成！", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mClickService!= null)
        {
            Log.d(TAG, "mClickService valid!");
            mClickService.start(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mClickServiceBinder = (ClickService.InterfaceMyBinder) service;
            mClickService = mClickServiceBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };
    private void start_timer() {
        if (timer == null) {
            timer = new Timer();
            // 每5000毫秒就执行一次刷新任务
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 5000);
            Log.i(TAG, "start_timer！");
        }
    }
    private void stop_timer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            Log.i(TAG, "stop_timer！");
        }
    }
    private void autotest_start() {

        if(!mClickService.get_Thread_run()){
            mClickService.widthPixels = widthPixels;
            mClickService.heightPixels = heightPixels;
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), 666);
            status.setText("正在运行！");
        }
        mClickService.Thread_pause = false;
    }
    private void autotest_stop() {
        if(mClickService== null) return;
        if(mClickService.get_Thread_run()){
            mClickService.set_Thread_break(true);
            mClickService.send_FloatW_msg("停止中");
            status.setText("测试停止！");
        }
    }

    private void autotest_pause() {
        if(mClickService== null) return;
        if(mClickService.get_Thread_run()){
            if(!mClickService.Thread_pause){
                mClickService.Thread_pause = true;
                mClickService.send_FloatW_msg("暂停");
                Toast.makeText(getApplicationContext(), "自动测试暂停！", Toast.LENGTH_SHORT).show();
                status.setText("测试暂停！");
            }
            else
            {
                mClickService.Thread_pause = false;
                mClickService.send_FloatW_msg("启动");
                Toast.makeText(getApplicationContext(), "自动测试启动！", Toast.LENGTH_SHORT).show();
                status.setText("测试启动！");
            }
        }
    }
    private void open_as() {
        Intent intent_as = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent_as.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent_as);
    }
    private void open_Overlays() {
        if(!Settings.canDrawOverlays(this)){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "悬浮窗权限已开启！", Toast.LENGTH_SHORT).show();
    }
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        } else {
            Toast.makeText(this, "存储读写授权成功！", Toast.LENGTH_SHORT).show();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.ACCESS_NETWORK_STATE}, 0);

        } else {
            Toast.makeText(this, "WIFI授权成功！", Toast.LENGTH_SHORT).show();
        }
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BIND_ACCESSIBILITY_SERVICE, Manifest.permission.ACCESS_NETWORK_STATE}, 0);
        //open_Overlays();
        //open_as();
    }

    public void open_Overlays_c(View view) {
        open_Overlays();
        //get_lisence();
    }
    public void open_as_c(View view) {
        open_as();
    }
    /**
     * 显示小窗口
     *
     * @param view
     */
    public void show_small_c(View view) {
        show_small();

    }
    public void show_small() {
        // 需要传递小悬浮窗布局，以及根布局的id，启动后台服务
        if(Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(context, FloatWindowService.class);
            intent.putExtra(FloatWindowService.LAYOUT_RES_ID,
                    R.layout.floatwindow_small);
            intent.putExtra(FloatWindowService.ROOT_LAYOUT_ID,
                    R.id.small_window_layout);
            startService(intent);
        }
        SystemClock.sleep(500);
    }

    /**
     * 显示二级悬浮窗
     *
     * @param view
     */
    public void showBig(View view) {

        floatWindowManager.createBigWindow(context);

    }

    /**
     * 移除所有的悬浮窗
     *
     * @param view
     */
    public void remove(View view) {
        floatWindowManager.removeAll();
    }

    public int refresh_heightPixels() {
        int height;
        height = ScreenUtils.getScreenHeight(context);
        if(nbar_flag) height =  height + getNavigationBarHeight();
        if(sbar_flag) height =  height + getStatusBarHeight();
        return height;
    }

    private int getStatusBarHeight() {

        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getNavigationBarHeight() {

        int resourceId=getResources().getIdentifier("navigation_bar_height","dimen","android");

        int height = getResources().getDimensionPixelSize(resourceId);

        Log.v("navigation bar>>>", "height:" + height);

        return height;
    }

    public boolean check_lisence(String file) {
        String lisence;
        lisence = AES_Utils.read_firstline(file);
        Log.i(TAG, String.format("lisence=%s",lisence));
        if(lisence.equals("") || Mac_string.equals("")) return false;
        String mac_decrypt  = AES_Utils.decrypt(lisence,endcode_key);
        Log.i(TAG, String.format("MAC_D=%s",mac_decrypt));
        if(Mac_string.equals(mac_decrypt)) return true;
        return false;
    }
    public boolean get_lisence() {
        String mac_get = mac.getText().toString();
        if(mac_get.equals("")) return false;
        String encrypt = AES_Utils.encrypt(mac_get, endcode_key);
        Log.i("ft_e", String.format("%s",encrypt));
        return true;
    }

    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean click = intent.getBooleanExtra("click",false);
        String msg =  intent.getStringExtra("msg");
        if( click ){
                autotest_pause();
        }
        if(msg!="") {
            if(!msg.equals("clear"))
                f_list.append(msg);
            else
                f_list.setText("");
        }
    }
}
    /**
     * 定时执行
     */
    private class RefreshTask extends TimerTask {
        @Override
        public void run() {
            Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
            //int year = c.get(Calendar.YEAR);
            //int month = c.get(Calendar.MONTH);
            //int date = c.get(Calendar.DATE);
            int hour_now = c.get(Calendar.HOUR_OF_DAY);
            int minute_now = c.get(Calendar.MINUTE);
            //int second = c.get(Calendar.SECOND);
            Log.i(TAG, String.format("hour_now=%d,minute_now=%d",hour_now,minute_now));

            if(mClickService!= null) {
                mClickService.hour_set = Integer.parseInt(hour.getText().toString());
                Log.i(TAG, String.format("hour_set=%d",mClickService.hour_set));
                if(hour_now == mClickService.hour_set && minute_now == 5){
                    mClickService.set_finish(false);
                    mClickService.Thread_restart = true;
                    mClickService.Thread_break = true;
                    f_list.setText("");
                    Log.i(TAG, String.format("autotest_restart!"));
                }
            }
        }
    }
}
