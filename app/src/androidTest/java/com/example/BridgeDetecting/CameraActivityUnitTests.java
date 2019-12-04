package com.example.BridgeDetecting;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CameraActivityUnitTests {

    @Test(expected = IllegalArgumentException.class)
    public void GetAverageWidth_EmptyListTest() {
        OpenCVLoader.initDebug(false);
        ArrayList<Point> points = new ArrayList<Point>();
        int width = CameraActivity.GetAverageWidth(points);
        assertEquals(width, 5);
    }

    @Test
    public void GetAverageWidth_2Points() {
        OpenCVLoader.initDebug(false);
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(0,0));
        points.add(new Point(10,2));
        int width = CameraActivity.GetAverageWidth(points);
        assertEquals(width, 5);
    }

    @Test
    public void GetAverageWidth_6Points() {
        OpenCVLoader.initDebug(false);
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(0,0));
        points.add(new Point(10,2));
        points.add(new Point(2,32));
        points.add(new Point(12,12));
        points.add(new Point(122,11));
        points.add(new Point(4,24));
        int width = CameraActivity.GetAverageWidth(points);
        assertEquals(width, 61);
    }

}
