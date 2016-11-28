package me.yasiru.ballot;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BallotPaperProcessor {
    private static int config_gaussianblur_radius = 3;
    private static int config_threshhold_level = 200;

    public static void process(BallotPaper ballotPaper) {

    }

    public static void preprocess(BallotPaper ballotPaper) {
        Mat ballotPaperImage = ballotPaper.getImage();
        Imgproc.cvtColor(ballotPaperImage, ballotPaperImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(ballotPaperImage, ballotPaperImage, new Size(config_gaussianblur_radius, config_gaussianblur_radius)
                , 0);
        Imgproc.threshold(ballotPaperImage, ballotPaperImage, config_threshhold_level, 255, Imgproc.THRESH_BINARY_INV);
    }

    public static void segment(BallotPaper ballotPaper) {
        Mat contouredImage = ballotPaper.getImage().clone();
        List<MatOfPoint> contours = ballotPaper.getExternalContours();
        Mat externalCountourIndex = ballotPaper.getExternalContoursList();
        double currentArea;
        double arealimit = ballotPaper.getImage().width() * ballotPaper.getImage().height() * 0.015;
        Imgproc.findContours(contouredImage, contours, externalCountourIndex, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint currentContour;
        MatOfPoint2f currentContour2f;
        RotatedRect rotatedRect = new RotatedRect();
        double rotationAngle = 0;
        double rotatedRectRatio = 0;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
//            currentArea = Imgproc.contourArea(currentContour);
            currentContour2f = new MatOfPoint2f(currentContour.toArray());
            rotatedRect = Imgproc.minAreaRect(currentContour2f);
            currentArea = rotatedRect.size.area();
            rotatedRectRatio = rotatedRect.size.height/rotatedRect.size.width;
            if(rotatedRectRatio < 1.0)
                rotatedRectRatio = 1 / rotatedRectRatio;
            if (currentArea >= arealimit && (Math.abs(rotatedRectRatio - 1.3) < 0.1 || Math.abs(rotatedRectRatio - 3.9) < 0.1)) {
                rotationAngle += rotatedRect.angle;
                System.out.println("Rotation Angle: " + rotationAngle + " Ratio " + rotatedRectRatio);
            }
        }
    }
}
