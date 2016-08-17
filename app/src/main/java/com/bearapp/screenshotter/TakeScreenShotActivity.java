package com.bearapp.screenshotter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bearapp.libscreenshotter.ScreenshotCallback;
import com.bearapp.libscreenshotter.Screenshotter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TakeScreenShotActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String TAG = "TakeScreenShotActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_screen_shot);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(FloatService.ACTION_HIDE_BUTTON));
        if (isStoragePermissionGranted()) {
            takeScreenshot();
        } else {
            requestStoragePermissions();
        }
    }

    public void takeScreenshot() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Screenshotter.getInstance()
                            .setSize(720, 1280)
                            .takeScreenshot(TakeScreenShotActivity.this, resultCode, data, new ScreenshotCallback() {
                                @Override
                                public void onScreenshot(Bitmap bitmap) {
                                    Log.d(TAG, "onScreenshot called");
                                    Toast.makeText(TakeScreenShotActivity.this, "Screenshot Captured!", Toast.LENGTH_SHORT).show();

                                    Date now = new Date();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.ENGLISH);
                                    String fileName = simpleDateFormat.format(now) + ".jpg";
                                    String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
                                    FileOutputStream out = null;
                                    try {
                                        out = new FileOutputStream(filePath);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (out != null) {
                                                out.flush();
                                                out.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                    LocalBroadcastManager.getInstance(TakeScreenShotActivity.this).sendBroadcast(new Intent(FloatService.ACTION_SHOW_BUTTON));
                    finish();
                }
            }, 1000);
        } else {
            Toast.makeText(this, "You denied the permission.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeScreenshot();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void requestStoragePermissions() {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

}
