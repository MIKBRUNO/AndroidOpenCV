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
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements Camera.PreviewCallback {

    private Camera cameraDevice;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        
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

        hideSystemUI();

        if (!hasCameraPermission()) requestCameraPermission(); // requesting camera permission
        else onCameraPermissionsGranted();

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraDevice.stopPreview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSystemUI();
        cameraDevice.startPreview();
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

}
