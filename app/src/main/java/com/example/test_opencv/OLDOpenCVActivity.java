package com.example.test_opencv;
//
//import android.os.Bundle;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.snackbar.Snackbar;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import android.view.View;
//
//import android.view.Menu;
//import android.view.MenuItem;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Offline I hope", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class OLDOpenCVActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback LoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_eye);

                        File CascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        CascadeFile = new File(CascadeDir, "haarcascade_frontalface_alt.xml");

                        FileOutputStream fos = new FileOutputStream(CascadeFile);

                        byte[] bytesBuffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(bytesBuffer)) != -1) {
                            fos.write(bytesBuffer, 0, bytesRead);
                        }
                        is.close();
                        fos.close();

                        faceClassifier = new CascadeClassifier(CascadeFile.getAbsolutePath());
                        CascadeDir.delete();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (! hasCameraPermission())
            requestCameraPermission();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    protected boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        else
            return true;
    }

    protected void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                Toast.makeText(this, "Camera is necessary for the app work properly!", Toast.LENGTH_LONG).show();
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.disableFpsMeter();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, LoaderCallBack);
        } else {
            LoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    private File CascadeFile;

    private CascadeClassifier faceClassifier;

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mRGBA = inputFrame.rgba();
        MatOfRect mFoundRects = new MatOfRect();
        faceClassifier.detectMultiScale(mRGBA, mFoundRects, 1.1, 2);
        Rect[] rects = mFoundRects.toArray();
        for (Rect rect : rects) {
            Imgproc.rectangle(mRGBA, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255));
        }
        return mRGBA;
//        double width = inputFrame.rgba().width();
//        double height = inputFrame.rgba().height();
//        Mat dst = new Mat();
//        Mat affineTransform = Imgproc.getAffineTransform(
//                new MatOfPoint2f(
//                        new Point(0, 0),
//                        new Point(1, 0),
//                        new Point(0, 1)),
//                new MatOfPoint2f(
//                        new Point(0, 0),
//                        new Point(1, 0.5),
//                        new Point(0, 1)
//                ));
//        Imgproc.warpAffine(inputFrame.rgba(), dst, affineTransform, new Size(width, height));
    }
}
