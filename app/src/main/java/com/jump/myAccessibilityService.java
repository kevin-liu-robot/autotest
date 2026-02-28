package com.jump;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Path;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityService.GestureResultCallback;
import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.GestureDescription.StrokeDescription;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Point;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;

public class myAccessibilityService extends AccessibilityService  {

    private final String TAG = getClass().getName();

    private MsgReceiver msgReceiver;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        //动态注册广播接收器
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jump.myAccessibilityService");
        registerReceiver(msgReceiver, intentFilter);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}).start();*/
    }
    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            int Point_Array[] =  intent.getIntArrayExtra("Point_Array");
            Point p1 = new Point();
            Point p2 = new Point();
            int cnt= 0;
            cnt = Point_Array[4];
            int time = Point_Array[5];
            p1.x = Point_Array[0];p1.y = Point_Array[1];
            p2.x = Point_Array[2];p2.y = Point_Array[3];
            switch (type) {
                case "click":
                    for(int i=0;i<cnt;i++)
                        startGesture(p1,p2,time);
                    break;
                case "swape":
                    for(int i=0;i<cnt;i++)
                        startGesture(p1,p2,time);
                    break;
                 default:
                     Global_Action(type,cnt);
                     break;
            }
        }
    }
    public void Global_Action(String type,int cnt) {
        for(int i=0;i<cnt;i++)
            switch (type) {
                case "home":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    break;
                case "back":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    break;
                case "task":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    break;
                case "power":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                    break;
                case "notif":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                    break;
                case "split":
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                    break;
                case "swape_r":
                    performGlobalAction(AccessibilityService.GESTURE_SWIPE_RIGHT);
                    break;
                case "swape_l":
                    performGlobalAction(AccessibilityService.GESTURE_SWIPE_LEFT);
                    break;
                case "swape_d":
                    performGlobalAction(AccessibilityService.GESTURE_SWIPE_DOWN);
                    break;
                case "swape_u":
                    performGlobalAction(AccessibilityService.GESTURE_SWIPE_UP);
                    break;
                default:break;
            }
    }

    /**
     * 模拟点击
     */
    public void startGesture(Point p1,Point p2,int time) {
        Log.d(TAG, String.format("x1 =%d,y1 =%d",p1.x,p1.y));
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, (int)(time+ Math.random() * 100) ));
        GestureDescription gesture = builder.build();
        //myAccessibilityService accessibilityService = new myAccessibilityService();
        boolean isDispatched =  dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
        if(isDispatched == true)
            Log.d(TAG, "click finished.");

    }
}
