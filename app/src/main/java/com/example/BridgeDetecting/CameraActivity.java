package com.example.BridgeDetecting;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    //view holder
    CameraBridgeViewBase cameraBridgeViewBase;

    //camera listener callback
    BaseLoaderCallback baseLoaderCallback;

    //image holder
    Mat img;

    ColorBlobDetector colorDetector;
    TextView offsetTextView;
    Button calibrateButton;
    double currentOffset = 0;

    int sensitivity = 15;
    Scalar lowColor = new Scalar(70-sensitivity, 100, 60);
    Scalar highColor = new Scalar(70+sensitivity,255, 255);

    private Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);

    private int defaultWidth = -2;
    private int defaultToLeftWidth = 0;
    private int defaultToRightWidth = 0;
    private CountDownTimer timer;
    private TcpClient mTcpClient;
    private boolean tcpConnected = false;

    private static final String MOBILE_APP_KEY = "WALK-ON-THE-BRIDGE-MOBILE";
    private static final String VR_MODULE_KEY = "WALK-ON-THE-BRIDGE-VR";
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int PORT = 9005;
    private static final int TIMEOUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findVRModule();
        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        offsetTextView = findViewById(R.id.offsetText);
        calibrateButton = findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer!=null)
                    timer.cancel();
                defaultWidth = -1;
                defaultToLeftWidth = 0;
                defaultToRightWidth = 0;
                timer = new CountDownTimer(200000,100) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        offsetTextView.setText("Current offset: " + String.valueOf(currentOffset));
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

//        if(tcpConnected) {
//            mTcpClient.sendMessage("X");
//        }

        if(contours.size()>0)
        {
            int currentAvgWidth = GetAverageWidth(contours.get(0).toList());
            if(defaultWidth==-1)
                defaultWidth = currentAvgWidth;

            if(currentAvgWidth > defaultWidth + defaultToRightWidth)
                defaultToRightWidth = currentAvgWidth - defaultWidth;
            if(currentAvgWidth < defaultWidth - defaultToLeftWidth)
                defaultToLeftWidth = defaultWidth - currentAvgWidth;

            if(currentAvgWidth>defaultWidth)
            {
                currentOffset = (double)(currentAvgWidth - defaultWidth) / defaultToRightWidth;
            }
            else
            {
                currentOffset = - ((double)(defaultWidth -  currentAvgWidth) / defaultToLeftWidth);
            }

            if(tcpConnected) {
                mTcpClient.sendDouble(currentOffset);
            }
        }
        else if(tcpConnected) {
            mTcpClient.sendDouble(0.0);
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

    public static int GetAverageWidth(List<Point> points)
    {
        if(points.size()==0)
            throw new IllegalArgumentException();

        Point maxWidth = points.get(0), minWidth = points.get(0);
        for (Point p : points) {
            if(p.x>maxWidth.x)
                maxWidth = p;
            if(p.x<minWidth.x)
                minWidth = p;
        }

        return (int)((maxWidth.x-minWidth.x)/2 + minWidth.x);
    }

    private void findVRModule() {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            boolean vrModuleFound = false;
            String vrModuleIP;

            @Override
            public void run() {
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();

                    InetAddress serverAddr = InetAddress.getByName(BROADCAST_ADDRESS);
                    DatagramPacket dp;
                    dp = new DatagramPacket(MOBILE_APP_KEY.getBytes(), MOBILE_APP_KEY.length(), serverAddr, PORT);
                    ds.send(dp);

                    String receiveString;
                    byte[] lMsg = new byte[1024];
                    dp = new DatagramPacket(lMsg, lMsg.length);
                    long startTime = System.currentTimeMillis();
                    do {
                        ds.setSoTimeout(TIMEOUT);
                        ds.receive(dp);
                        receiveString = new String(lMsg, 0, dp.getLength());
                    } while(!receiveString.equals(VR_MODULE_KEY) && (System.currentTimeMillis() - startTime) < TIMEOUT);
                    if((System.currentTimeMillis() - startTime) >= TIMEOUT) {
//                        throw new SocketTimeoutException();
                    }
                    vrModuleFound = true;
                    vrModuleIP = ((InetAddress)dp.getAddress()).getHostAddress();
                }
                catch (SocketTimeoutException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CameraActivity.this, "Couldn't find VR Module", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (ds != null) {
                        ds.close();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(vrModuleFound) {
                            new ConnectTask(vrModuleIP, PORT).execute();
                            Toast.makeText(CameraActivity.this, "Connected to VR module", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        thread.start();
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {
        private String serverIp;
        private int serverPort;

        public ConnectTask(String ip, int port) {
            serverIp = ip;
            serverPort = port;
        }

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(serverIp, serverPort, new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                }
            });
            tcpConnected = mTcpClient.run();
            return null;
        }

//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            //response received from server
//            Log.d("test", "response " + values[0]);
//            //process server response here....
//
//        }
    }
}