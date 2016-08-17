package com.bearapp.screenshotter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatService extends Service {
    private WindowManager windowManager;
    private ImageView imageView;

    private WindowManager.LayoutParams params;

    private static final String TAG = "FloatService";

    public static final String ACTION_SHOW_BUTTON = "ACTION_SHOW_BUTTON";
    public static final String ACTION_HIDE_BUTTON = "ACTION_HIDE_BUTTON";


    public FloatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FloatService onCreate");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_upload);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.x = 200;
        params.y = 400;

        imageView.setOnTouchListener(new View.OnTouchListener() {
            private int initX;
            private int initY;
            private float initTouchX;
            private float initTouchY;

            long time_start = 0, time_end = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();
                        initX = params.x;
                        initY = params.y;
                        initTouchX = motionEvent.getRawX();
                        initTouchY = motionEvent.getRawY();
                        Log.d(TAG, "initX, initY=" + initX +"," + initY);
                        Log.d(TAG, "initTouchX, initTouchY=" + initTouchX +"," + initTouchY);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "motionEvent.getRawX()=" + motionEvent.getRawX());
                        Log.d(TAG, "motionEvent.getRawY()=" + motionEvent.getRawY());
                        params.x = (int) (initX - (motionEvent.getRawX() - initTouchX));
                        params.y = (int) (initY - (motionEvent.getRawY() - initTouchY));
                        windowManager.updateViewLayout(imageView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        int x_diff = (int) (motionEvent.getRawX() - initTouchX);
                        int y_diff = (int) (motionEvent.getRawY() - initTouchY);
                        if (Math.abs(x_diff) < 50 && Math.abs(y_diff) < 50) {
                            time_end = System.currentTimeMillis();
                            if (time_end - time_start < 300) {
                                imageClick();
                            }
                        }
                        return true;

                }
                return false;
            }
        });

        windowManager.addView(imageView, params);

        registerReceiver();

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SHOW_BUTTON.equals(action)) {
                windowManager.addView(imageView, params);
            } else if (ACTION_HIDE_BUTTON.equals(action)) {
                windowManager.removeView(imageView);
            }
        }
    };

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHOW_BUTTON);
        intentFilter.addAction(ACTION_HIDE_BUTTON);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void imageClick() {
        Intent it = new Intent(this, TakeScreenShotActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(it);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        Log.d(TAG, "FloatService onDestroy");
        if (imageView != null) {
            windowManager.removeView(imageView);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FloatService onStartCommand");
        return START_NOT_STICKY;
    }
}
