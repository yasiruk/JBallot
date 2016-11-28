package me.yasiru.ballot;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BalotPaperProcessor {
    private static int config_gaussianblur_radius = 3;
    private static int config_threshhold_level = 200;

    public void process(BallotPaper ballotPaper) {

    }

    private void preprocess(BallotPaper ballotPaper) {
        Imgproc.GaussianBlur(ballotPaper, ballotPaper, new Size(config_gaussianblur_radius, config_gaussianblur_radius)
                , 0);
        Imgproc.threshold(ballotPaper, ballotPaper, config_threshhold_level, 255, Imgproc.THRESH_BINARY_INV);
    }

    private void segment(BallotPaper ballotPaper) {
        Mat contouredImage = ballotPaper.clone();
        List<MatOfPoint> contours = ballotPaper.getExternalContours();
        Mat externalCountourIndex = ballotPaper.getExternalContoursList();
        double currentArea;
        double arealimit = ballotPaper.width() * ballotPaper.height() * 0.01;
        Imgproc.findContours(contouredImage, contours, externalCountourIndex, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint currentContour;
        MatOfPoint2f currentContour2f;
        RotatedRect rotatedRect = new RotatedRect();
        double rotationAngle = 0;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            currentArea = Imgproc.contourArea(currentContour);
            if (currentArea >= arealimit) {
                currentContour2f = new MatOfPoint2f(currentContour.toArray());
                rotatedRect = Imgproc.minAreaRect(currentContour2f);
                rotationAngle += rotatedRect.angle;
                System.out.println("Rotation Angle: " + rotationAngle);
            }
        }
    }
}
