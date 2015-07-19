package com.example.vsatish.measure;

import org.opencv.core.*;

import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import android.graphics.Color;
import android.util.Log;
import android.widget.*;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.hardware.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;

public class ImageProcessor {

	Mat oldMat = null;
	Mat newMat = null;
	Mat oldMatGray = null;
    Mat original = null;
	Mat newMatGray = null;
    MatOfByte status = new MatOfByte();
    MatOfFloat err = new MatOfFloat();
	MatOfPoint previousPoints = new MatOfPoint();
	MatOfPoint2f nextPoints = new MatOfPoint2f();
	Boolean firstTime = false;
    Double averagePixelDisplacement = 0.0;
    Double tempAveragePixelDisplacement = 0.0;
    Double overallPixelDisplacement = 0.0;
    int numDisplacements = 0;
	private static final String  TAG = "Main Activity";

    public Mat runOpticalFlow(Mat frame) {
        original = frame.clone();
		//first time, oldMat == null
        Point[] temp = previousPoints.toArray();
		if (temp.length == 0) {

			//set firstTime to true so there will be no optical flow for this frame- next frame will be newMat
			firstTime = true;

			//put first frame in oldMat
			oldMat = frame;
			oldMatGray = frame;
			if (oldMat != null) {
				Log.i(TAG, "MAT IS NOT NULL!!!!!!!!!!!!!!!!!!!!!!!");
			}
			//convert oldMat to grayscale, and store in oldMatGray
			Imgproc.cvtColor(oldMat, oldMatGray, Imgproc.COLOR_BGR2GRAY);
			//Imgproc.cvtColor(oldMatGray, oldMatGray, Imgproc.COLOR_GRAY2RGBA, 4);
			Imgproc.goodFeaturesToTrack(oldMatGray, previousPoints, 600, 0.01, 0.01);
		}


		//when it is not the first time
		else  {

            //put current frame in newMat                         
            newMat = frame;
            newMatGray = frame;
//            if (oldMat == null) {
//                Log.i(TAG, "oldMat IS NULL!!!!!!!!!!!!!!!!!!!!!!!");
//            }
            //convert newMat to grayscale, and store in newMatGray
            Imgproc.cvtColor(newMat, newMatGray, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.cvtColor(newMatGray, newMatGray, Imgproc.COLOR_GRAY2RGBA, 4);

            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();
            //Video.calcOpticalFlowPyrLK(oldMatGray, newMatGray, previousPoints, nextPoints, status, err, 50, 2, 0);

            //calculate optical flow
            MatOfPoint2f tempMat2f = new MatOfPoint2f(previousPoints.toArray());

//            Log.i(TAG, "Checking MatOfPoint2F!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + tempMat2f.checkVector(2, CvType.CV_32F, true));
            if (tempMat2f.checkVector(2, CvType.CV_32F, true) > 0)
                Video.calcOpticalFlowPyrLK(oldMatGray, newMatGray, tempMat2f, nextPoints, status, err);


            //draw circles and lines to show optical flow
            Point[] pointp = previousPoints.toArray();
            Point[] pointn = nextPoints.toArray();
            for (int i = 0; i < pointp.length; i++) {
                //Core.circle(original, pointp[i], 15, new Scalar(0, 0, 255));
                tempAveragePixelDisplacement = Math.sqrt( Math.pow((pointn[i].x - pointp[i].x), 2) + Math.pow((pointn[i].y - pointp[i].y), 2));
                if (tempAveragePixelDisplacement < 100 && tempAveragePixelDisplacement > 20 && Math.abs(pointn[i].y - pointp[i].y) < 5) {
                    Core.line(original, pointp[i], pointn[i], new Scalar(0, 255, 0), 10);
                    averagePixelDisplacement += tempAveragePixelDisplacement;
                    numDisplacements++;
                }

            }
            averagePixelDisplacement = averagePixelDisplacement / numDisplacements;
            numDisplacements = 0;
            if (averagePixelDisplacement > 0) {
                overallPixelDisplacement += averagePixelDisplacement;
            }
//            Core.putText(original, "" + (overallPixelDisplacement / 830) * 12.5, new Point(50, 50), 2, 2.5, new Scalar(0, 255, 0), 3);

//            Log.i(TAG, "AverageDisplacement" + averagePixelDisplacement);
//            Log.i(TAG, "OverallDisplacement" + overallPixelDisplacement);
            averagePixelDisplacement = 0.0;
            //since the images are always changes, features have to be found again, using the current frame, which is now stored in oldMat/oldMatGray
            Imgproc.goodFeaturesToTrack(oldMatGray, previousPoints, 200, 0.01, 0.01);

            //the new frame becomes the old frame for the next round
            oldMat = newMat;
            oldMatGray = newMatGray;
        }


		//return the current frame with optical flow drawn on it
		return original;
	}

    public double getLength() {
        return Math.floor((double)(overallPixelDisplacement / 830) * 12.5 * 100) / 100;
    }
	
//public Mat runOpticalFlow(Mat frame) {
//
////            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
////			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_GRAY2RGBA, 4);
//			original = frame.clone();
//			//put first frame in oldMat
//			oldMat = frame;
//			oldMatGray = frame;
//			//convert oldMat to grayscale, and store in oldMatGray
//			Imgproc.cvtColor(oldMat, oldMatGray, Imgproc.COLOR_BGR2GRAY);
//			//Imgproc.cvtColor(oldMatGray, oldMatGray, Imgproc.COLOR_GRAY2RGBA, 4);
//			Imgproc.goodFeaturesToTrack(oldMatGray, previousPoints, 600, 0.01, 0.01);
//
//            newMatGray = frame;
//            //Point[] points = previousPoints.toArray();
//            //MatOfPoint2f test = new MatOfPoint2f(points);
//            //Point[] points1 = test.toArray();
////            int counter = 0;
////    for (Point p : points1) {
////        Log.i(TAG, "" + counter + " ): " + p.x + ", " + p.y);
////        counter++;
////    }
//    //counter = 0;
//            //test.fromArray(previousPoints.toArray());
//        //previousPoints.convertTo(test, CvType.CV_32FC2);
////            for(int i = 0; i < test.rows(); i++) {
////                for(int j = 0; j < test.cols(); j++) {
////                    Log.i(TAG, "" + test.get(i, j));
////                }
////    }
//            //Log.i(TAG, "Checking MatOfPoint2F!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + test.checkVector(2, CvType.CV_32F, true));
//
//			Point[] pointp = previousPoints.toArray();
//            //Point[] nextp = nextPoints.toArray();
//			for (int i = 0; i < pointp.length; i++) {
//                //Log.i(TAG, "" + px.x + ", " + px.y);
//				Core.circle(original, pointp[i], 15, new Scalar(0, 0, 255));
//                //Core.line(original, pointp[i], nextp[i], new Scalar(0, 255, 0));
//			}
//
//		return original;
//	}
}
