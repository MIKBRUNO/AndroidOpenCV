package com.example.test_opencv;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public final class CameraLib {

    private CameraLib() {}

    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getCameraInstance(int specificCamera) {
        Camera cameraInst = null;
        try {
            cameraInst = Camera.open(specificCamera);
        }
        catch (Exception e) {

        }
        return cameraInst;
    }

}
