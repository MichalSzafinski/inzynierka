package com.example.cvtest1;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    //view holder
    CameraBridgeViewBase cameraBridgeViewBase;

    //camera listener callback
    BaseLoaderCallback baseLoaderCallback;

    //image holder
    Mat img;

    ColorBlobDetector colorDetector;
    Button offsetTextView;
    Button calibrateButton;
    int currentOffset = 0;

    int sensitivity = 15;
    Scalar lowColor = new Scalar(70-sensitivity, 100, 60);
    Scalar highColor = new Scalar(70+sensitivity,255, 255);

    private Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);

    private int defaultWidth = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
/*
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);*/

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        //create camera listener callback
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.v("aashari-log", "Loader interface success");
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        //offsetTextView = findViewById(R.id.offsetTextView);
        calibrateButton = findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultWidth = -1;
                new CountDownTimer(200000,100) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        calibrateButton.setText(String.valueOf(currentOffset));
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        colorDetector = new ColorBlobDetector();
        colorDetector.setBounds(lowColor, highColor);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat src = inputFrame.rgba();

        colorDetector.process(src);
        List<MatOfPoint> contours = colorDetector.getContours();
        Log.e("CAMERA_ACTIVITY", "Contours count: " + contours.size());
        Imgproc.drawContours(src, contours, -1, CONTOUR_COLOR);

        if(contours.size()>0)
        {
            List<Point> points = contours.get(0).toList();
            Point maxWidth = points.get(0), minWidth =points.get(0);
            for (Point p : points) {
                if(p.x>maxWidth.x)
                    maxWidth = p;
                if(p.x<minWidth.x)
                    minWidth = p;
            }



            if(defaultWidth==-1)
                defaultWidth = (int)((maxWidth.x-minWidth.x)/2 + minWidth.x);

            int currentAvgWidth = (int)((maxWidth.x-minWidth.x)/2 + minWidth.x);
            currentOffset = currentAvgWidth - defaultWidth;
        }

        return src;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }
}