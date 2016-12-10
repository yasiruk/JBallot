package me.yasiru.ballot;


import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wik2kassa on 11/24/2016.
 */
public class BallotProcessor {

    final static  double arealimit = 200000;

    public static class SegmentationResult {
        private RotatedRect partyVoteBB;
        private RotatedRect preferenceVoteBB;

        public RotatedRect getPartyVoteBB() {
            return partyVoteBB;
        }

        public RotatedRect getPreferenceVoteBB() {
            return preferenceVoteBB;
        }
    }
    public static class SegmentedVoteSections {
        private Mat partyVotes;
        private Mat preferenceVotes;

        public Mat getPartyVoteArea() {
            return partyVotes;
        }

        public Mat getPreferenceVoteArea() {
            return preferenceVotes;
        }
    }

    public static Mat threshhold(Mat image, int level) {
        Mat outputimage = new Mat();
        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);
        Imgproc.threshold(image, outputimage, level, 255, Imgproc.THRESH_BINARY_INV);
//        Imgproc.medianBlur(outputimage, outputimage,3);
        return outputimage;
    }

    public static SegmentationResult segment(Mat image) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        SegmentationResult segmentationResult = new SegmentationResult();
        Mat contouredImage = image.clone();
        Imgproc.findContours(contouredImage, contours, hierachy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint currentContour;
        MatOfPoint2f currentContour2f;
        RotatedRect rotatedRect = new RotatedRect();
        double rotationAngle = 0;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);

            double currentArea = Imgproc.contourArea(currentContour);

            if(currentArea >= arealimit) {
                currentContour2f = new MatOfPoint2f(currentContour.toArray());
                rotatedRect = Imgproc.minAreaRect(currentContour2f);
                rotationAngle += rotatedRect.angle;

                if(segmentationResult.partyVoteBB == null)
                    segmentationResult.partyVoteBB = rotatedRect;
            }
        }

        if(segmentationResult.partyVoteBB.size.area() < rotatedRect.size.area()) {
            segmentationResult.preferenceVoteBB = segmentationResult.partyVoteBB;
            segmentationResult.partyVoteBB = rotatedRect;
        } else
            segmentationResult.preferenceVoteBB = rotatedRect;



        return  segmentationResult;
    }

    public static SegmentedVoteSections getVoteSections(Mat srcImage, SegmentationResult segmentationResult) {
        SegmentedVoteSections segmentedVoteSections = new SegmentedVoteSections();
        double partyangle, prefangle;
        partyangle = segmentationResult.partyVoteBB.angle < -45 ? segmentationResult.partyVoteBB.angle + 90 : segmentationResult.partyVoteBB.angle;
        prefangle = segmentationResult.preferenceVoteBB.angle < -45 ? segmentationResult.preferenceVoteBB.angle + 90 : segmentationResult.preferenceVoteBB.angle;
        Mat partyVoteRotM = Imgproc.getRotationMatrix2D(segmentationResult.partyVoteBB.center, partyangle, 1);
        Mat prefVoteRotM = Imgproc.getRotationMatrix2D(segmentationResult.preferenceVoteBB.center, prefangle, 1);

        Mat partyVoteR = new Mat();
        Mat prefVoteR = new Mat();

        Imgproc.warpAffine(srcImage, partyVoteR, partyVoteRotM, srcImage.size(), Imgproc.INTER_CUBIC);
        Imgproc.warpAffine(srcImage, prefVoteR, prefVoteRotM, srcImage.size(), Imgproc.INTER_CUBIC);

        Mat croppedPartyVotes = new Mat();
        Mat croppedPrefVotes = new Mat();

        Size partySize = new Size();
        Size prefSize = new Size();

        if(segmentationResult.partyVoteBB.angle < -45) {
            partySize.height = segmentationResult.partyVoteBB.size.width;
            partySize.width = segmentationResult.partyVoteBB.size.height;
        } else {
            partySize = segmentationResult.partyVoteBB.size;
        }

        if(segmentationResult.preferenceVoteBB.angle < -45) {
            prefSize.height = segmentationResult.preferenceVoteBB.size.width;
            prefSize.width = segmentationResult.preferenceVoteBB.size.height;
        } else {
            prefSize = segmentationResult.preferenceVoteBB.size;
        }

        Imgproc.getRectSubPix(partyVoteR, partySize, segmentationResult.partyVoteBB.center, croppedPartyVotes);
        Imgproc.getRectSubPix(prefVoteR, prefSize, segmentationResult.preferenceVoteBB.center, croppedPrefVotes);

        segmentedVoteSections.partyVotes = croppedPartyVotes;
        segmentedVoteSections.preferenceVotes = croppedPrefVotes;

        return segmentedVoteSections;
    }


}
