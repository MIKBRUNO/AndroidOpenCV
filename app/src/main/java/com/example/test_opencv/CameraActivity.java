package com.example.test_opencv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_opencv.env.ImageUtils;

public abstract class CameraActivity extends AppCompatActivity implements Camera.PreviewCallback {

    private Camera cameraDevice;
    private HandlerThread handlerThread;
    private Handler handler;
    protected int previewHeight;
    protected int previewWidth;
    protected int[] rgbBytes;
    protected Runnable imgConverter;

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        previewHeight = camera.getParameters().getPreviewSize().height;
        previewWidth = camera.getParameters().getPreviewSize().width;
        rgbBytes = new int[previewHeight*previewWidth];

        imgConverter = new Runnable() {
            @Override
            public void run() {
                ImageUtils.convertYUV420SPToARGB8888(data, previewWidth, previewHeight, rgbBytes);
            }
        };

        processImage();
    }

    private void onCameraPermissionsGranted() {
        if (CameraLib.checkCameraHardware(this)) cameraDevice = CameraLib.getCameraInst();
        else {
            Toast.makeText(this, "There's not camera on your device.", Toast.LENGTH_LONG).show();
            finish();
        }

        CameraPreview cameraPreview = new CameraPreview(this, cameraDevice);

        setContentView(R.layout.activity_main);

        FrameLayout frameLayoutCameraPreview = findViewById(R.id.camera_preview);
        frameLayoutCameraPreview.addView(cameraPreview);

        cameraDevice.setPreviewCallback(this);
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasCameraPermission()) requestCameraPermission(); // requesting camera permission
        else onCameraPermissionsGranted();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlerThread.quit();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cameraDevice.stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        cameraDevice.startPreview();

        handlerThread = new HandlerThread("imageBgProcess");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    protected void runInBackground(Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraDevice.release();
    }

    protected boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // there will be exception on devices
            // with SDK level lower then 23 (Marshmallow)
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            // checks if permission of camera using is granted
        else
            return true;
        // with SDK lower than 23 there is permission of camera
    }

    protected void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) // for 'don't task again' variant
                Toast.makeText(this, "Camera is necessary for the app work properly!", Toast.LENGTH_LONG).show(); // message
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 1); // requesting permission (Activity's func)
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // when we request permission and user answered system dialog calling this event
        int PERMISSIONS_REQUEST = 1;
        if (requestCode == PERMISSIONS_REQUEST) {
            for (int result : grantResults) { // for each permission
                if (result != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission(); // if any permission is not granted app closes
                    return;
                }
            }
        }
        onCameraPermissionsGranted();
    }

    protected abstract void processImage();

}
