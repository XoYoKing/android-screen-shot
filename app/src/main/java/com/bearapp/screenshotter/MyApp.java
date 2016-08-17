package com.bearapp.screenshotter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Henry.Ren on 16/8/17.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApp", "onCreate");
        startService(new Intent(this, FloatService.class));
    }
}
