package com.example.test_opencv;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.util.Log;
import android.hardware.camera2.CameraManager;

import androidx.annotation.RequiresApi;

public final class CameraLib {

    private CameraLib() {}

    public static boolean checkCameraHardware(Context context) { return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA); }

    public static Camera getCameraInst() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    public static Camera getCameraInst(int specCameraId) {
        Camera c = null;
        try {
            c = Camera.open(specCameraId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

}
