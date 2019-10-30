package com.example.cvtest1;

import android.graphics.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Method;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    //view holder
    CameraBridgeViewBase cameraBridgeViewBase;

    //camera listener callback
    BaseLoaderCallback baseLoaderCallback;

    //image holder
    Mat img;

    int sensitivity = 15;
    Scalar lowColor = new Scalar(70-sensitivity, 100, 60);
    Scalar highColor = new Scalar(70+sensitivity,255, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);

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

        //Text
        final EditText highPinkText =findViewById(R.id.highPinkEditText);
        final EditText lowPinkText = findViewById(R.id.lowPinkEditText);

//        highPinkText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                try
//                {
//                    String[] split = highPinkText.getText().toString().split(",");
//
//                    highColor = new Scalar(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
//                }
//                catch(Exception e)
//                {
//                }
//            }
//        });
//        lowPinkText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                try
//                {
//                    String[] split = lowPinkText.getText().toString().split(",");
//
//                    lowColor = new Scalar(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
//                }
//                catch(Exception e)
//                {
//
//                }
//            }
//        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat src = inputFrame.rgba();

        Mat hsvFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src, hsvFrame, Imgproc.COLOR_RGB2HSV, 3);

        Mat pinkMat = new Mat(hsvFrame.rows(), hsvFrame.cols(), CvType.CV_8UC1, new Scalar(3));
        Core.inRange(hsvFrame, lowColor, highColor, pinkMat);
        //apply a series of erosions and dilations to the mask
        // using an elliptical kernel
        // blur the mask to help remove noise, then apply the
        // mask to the frame
//        final Size ksize = new Size(3, 3);
//
//        Mat skin = new Mat(pinkMat.rows(), pinkMat.cols(), CvType.CV_8U, new Scalar(3));
//        Imgproc.GaussianBlur(pinkMat, pinkMat, ksize, 0);
//        Core.bitwise_and(src, src, skin, pinkMat);

        double buff[] = new double[(int)pinkMat.total() * pinkMat.channels()];
        pinkMat.get(0,0, buff);
        int sumHeight=0, sumWidth=0, quantity=0;
        for (int i=0;i<buff.length; i++)
        {
            if(buff[i]>0)
            {
                quantity++;
                int height = i%pinkMat.rows();
                int width = i - height;
                sumHeight+=height;
                sumWidth+=width;
            }
        }
//        for (int i=0;i<pinkMat.rows(); i++)
//        {
//            for (int j=0;j<pinkMat.cols(); j++)
//            {
//                if(pinkMat.get(i,j)[0]>0)
//                {
//                    quantity++;
//                    sumHeight+=i;
//                    sumWidth+=j;
//                }
//            }
//
//        }
        if(quantity>20)
        {
            int avgHeight = sumHeight/quantity, avgWidth = sumHeight/quantity;

            Imgproc.circle (
                    src,                 //Matrix obj of the image
                    new Point(avgHeight, avgWidth),    //Center of the circle
                    10,                    //Radius
                    new Scalar(0, 0, 255),  //Scalar object for color
                    10                      //Thickness of the circle
            );
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
    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
}