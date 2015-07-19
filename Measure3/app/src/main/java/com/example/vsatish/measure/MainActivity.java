package com.example.vsatish.measure;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Intent;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends Activity implements CvCameraViewListener, View.OnTouchListener {

    private static final String  TAG = "Main Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageProcessor    mImageProcessor;
    private boolean processImage;
    private Button measure;
    TextView length;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
//                    mOpenCvCameraView.setMaxFrameSize(8000, 1500);
                    mOpenCvCameraView.setMinimumHeight(8000);
                    mOpenCvCameraView.setMinimumWidth(2000);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "Creating and seting view");
        mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(this, -1);
        setContentView(R.layout.activity_main);

        RelativeLayout myLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

        mOpenCvCameraView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT));
        myLayout.addView(mOpenCvCameraView);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mImageProcessor = new ImageProcessor();

        measure = (Button)findViewById(R.id.measure);
        measure.bringToFront();

        Button menu = (Button)findViewById(R.id.menu);
        menu.bringToFront();

        Button save = (Button)findViewById(R.id.save);
        save.bringToFront();

        ImageView logo = (ImageView)findViewById(R.id.imageView);
        logo.bringToFront();

        length = (TextView)findViewById(R.id.length);
        length.bringToFront();

        updateLength();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public boolean onTouch(View view, MotionEvent event) {

        return false;
    }

    public Mat onCameraFrame(Mat inputFrame) {
    	if (measure.isPressed())
            return mImageProcessor.runOpticalFlow(inputFrame);
        else
            return inputFrame;
    }

    public void save (View v) {
        Intent intent = new Intent(this, NameActivity.class);
        startActivity(intent);
    }

    private void updateLength() {
        Thread timer = new Thread() { //new thread
            public void run() {
                Boolean b = true;
                try {
                    do {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (!measure.isPressed() && mImageProcessor.getLength() > 0)
                                    length.setText("" + mImageProcessor.getLength() + "in.");
                            }
                        });


                    }
                    while (b == true);
                } finally {
                }
            };
        };
        timer.start();
    }
//    public void measure(View v) {
//
//    }


}

