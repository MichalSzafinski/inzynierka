package com.example.BridgeDetecting;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CameraBlobDetectorUnitTests {

    @Test(expected = IllegalArgumentException.class)
    public void GetMaxAreaContour_EmptyListTest() {
        OpenCVLoader.initDebug(false);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint maxAreaContour = ColorBlobDetector.GetMaxAreaContour(contours);
    }

    @Test
    public void GetMaxAreaContour_3ContoursTest() {
        OpenCVLoader.initDebug(false);
        final MatOfPoint m1 = new MatOfPoint();
        final MatOfPoint m2 = new MatOfPoint();
        final MatOfPoint m3 = new MatOfPoint();
        List<Point> list1 = new ArrayList<>();
        list1.add(new Point(1,1));
        list1.add(new Point(1,3));
        list1.add(new Point(3,1));
        list1.add(new Point(3,3));
        List<Point> list2 = new ArrayList<>();
        list2.add(new Point(1,1));
        list2.add(new Point(1,10));
        list2.add(new Point(5,1));
        list2.add(new Point(3,32));
        List<Point> list3 = new ArrayList<>();
        list3.add(new Point(1,1));
        list3.add(new Point(1,2));
        list3.add(new Point(2,1));
        list3.add(new Point(2,4));
        m1.fromList(list1);
        m2.fromList(list2);
        m3.fromList(list3);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>() {{
            add(m1);
            add(m2);
            add(m3);
        }};
        MatOfPoint maxAreaContour = ColorBlobDetector.GetMaxAreaContour(contours);
        assertEquals(m2, maxAreaContour);
    }

    @Test
    public void GetMaxAreaContour_6ContoursTest() {
        OpenCVLoader.initDebug(false);
        final MatOfPoint m1 = new MatOfPoint();
        final MatOfPoint m2 = new MatOfPoint();
        final MatOfPoint m3 = new MatOfPoint();
        final MatOfPoint m4 = new MatOfPoint();
        final MatOfPoint m5 = new MatOfPoint();
        List<Point> list1 = new ArrayList<>();
        list1.add(new Point(1,1));
        list1.add(new Point(1,3));
        list1.add(new Point(3,1));
        list1.add(new Point(3,3));
        List<Point> list2 = new ArrayList<>();
        list2.add(new Point(1,1));
        list2.add(new Point(1,10));
        list2.add(new Point(5,1));
        list2.add(new Point(3,32));
        List<Point> list3 = new ArrayList<>();
        list3.add(new Point(1,1));
        list3.add(new Point(1,2));
        list3.add(new Point(2,1));
        list3.add(new Point(2,4));
        List<Point> list4 = new ArrayList<>();
        list4.add(new Point(1,1));
        list4.add(new Point(1,2));
        list4.add(new Point(2,1));
        List<Point> list5 = new ArrayList<>();
        list5.add(new Point(1,1));
        list5.add(new Point(5,5));
        list5.add(new Point(6,6));
        list5.add(new Point(7,7));
        m1.fromList(list1);
        m2.fromList(list2);
        m3.fromList(list3);
        m4.fromList(list4);
        m5.fromList(list5);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>() {{
            add(m1);
            add(m2);
            add(m3);
            add(m4);
            add(m5);
        }};
        MatOfPoint maxAreaContour = ColorBlobDetector.GetMaxAreaContour(contours);
        assertEquals(m2, maxAreaContour);
    }
}
