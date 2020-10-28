package com.example.test_opencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DetectorActivity extends CameraActivity{

    @Override
    protected void processImage() {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                Log.e("MIKBRUNO", "processImage running");
                imgConverter.run();
                Log.e("MIKBRUNO", String.valueOf(rgbBytes[0]));

                
            }
        });
    }
}