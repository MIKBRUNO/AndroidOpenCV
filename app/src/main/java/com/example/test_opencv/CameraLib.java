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

    private static CameraManager cameraManager;

    public static CameraManager getCameraManager(Activity activity) throws CameraAccessException {
        if (cameraManager == null)
            cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        return cameraManager;
    }

}
