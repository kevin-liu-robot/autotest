package com.jump;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.jump.FloatWindowManager;
import com.jump.FloatWindowService;
import com.jump.FloatWindowSmallView.OnClickListener;

public class FloatWindowService extends Service {
    public FloatWindowService() {
    }
    private static final String TAG = "FloatWindowService";
    public static final String LAYOUT_RES_ID = "layoutResId";
    public static final String ROOT_LAYOUT_ID = "rootLayoutId";
    private FloatWindowManager floatWindowManager;
    // 用于在线程中创建/移除/更新悬浮窗
    private Handler handler = new Handler();
    private Context context;
    private Timer timer;
    // 小窗口布局资源id
    private int layoutResId;
    // 布局根布局id
    private int rootLayoutId;
    private MsgReceiver msgReceiver;
    private Intent MA_intent = new Intent("com.jump.OpenCVActivity");
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        floatWindowManager = FloatWindowManager.getInstance(context);
        layoutResId = intent.getIntExtra(LAYOUT_RES_ID, 0);
        rootLayoutId = intent.getIntExtra(ROOT_LAYOUT_ID, 0);
        if (layoutResId == 0 || rootLayoutId == 0) {
            throw new IllegalArgumentException(
                    "layoutResId or rootLayoutId is illegal");
        }
        if (timer == null) {
            timer = new Timer();
            // 每500毫秒就执行一次刷新任务
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
        }
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jump.FloatWindowService");
        registerReceiver(msgReceiver, intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
// Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
    }
    private class RefreshTask extends TimerTask {
        @Override
        public void run() {
        // 当前界面没有悬浮窗显示，则创建悬浮
            if (!floatWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        floatWindowManager
                                .createSmallWindow(context, layoutResId,
                                        rootLayoutId);

                    }
                });
            }
            // 设置小悬浮窗的单击事件
            floatWindowManager.setOnClickListener(new OnClickListener() {
                @Override
                public void click()  {
                    Log.d(TAG, String.format("floatwindow Click!"));
                    send_floatw_click();
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void send_floatw_click() {
        MA_intent.putExtra("click", true);
        MA_intent.putExtra("msg", "");
        sendBroadcast(MA_intent);
        SystemClock.sleep(100);
    }
    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg =  intent.getStringExtra("msg");
            floatWindowManager
                    .setSmallWindow_msg(msg, layoutResId,
                            rootLayoutId);
        }
    }
}
