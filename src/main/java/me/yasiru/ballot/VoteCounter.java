package me.yasiru.ballot;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class VoteCounter {
    public void addParty(String name, Mat image) {

    }

    public int getPartyVote(Mat partyVote) throws InvalidVote {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        Mat contouredImage = partyVote.clone();
        Imgproc.findContours(contouredImage, contours, hierachy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE);

        int areaLowerBound = (partyVote.width() * partyVote.height()) / (11 * 10);
        int heightUpperBound = (int) ((partyVote.height() / 11) * 1.5);
        int widthUpperBound = ((partyVote.width() / 3));
        int voteMarkBoundary = (int) (7.0 * partyVote.width()) / 9;
        int partyMarkBoundary = (int) (6.5 * partyVote.width()) / 9;
        int voteThreshhond = areaLowerBound / 10;

        MatOfPoint currentContour;
        Rect bRect;
        int votePartyIndex = 0;
        boolean votefound = false;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            double currentArea = Imgproc.contourArea(currentContour);
            if (currentArea >= areaLowerBound) {
                bRect = Imgproc.boundingRect(currentContour);
                if (bRect.height < heightUpperBound && bRect.width < widthUpperBound) {
                    if (bRect.x > voteMarkBoundary)
                        if (Core.countNonZero(new Mat(partyVote, bRect)) > voteThreshhond) {
                            if (votefound)
                                throw new InvalidVote();
                            votePartyIndex = (int) Math.ceil((11 * bRect.y) / partyVote.height());
                            votefound = true;
//                            System.out.println("vote goes to " + (int)Math.ceil( (11 * bRect.y) / partyVote.height()) + " " + bRect.y);
//                            Imgcodecs.imwrite("vote_pos" + i + ".jpg", new Mat(partyVote, bRect));
                        }
                }
            }
        }
        return votePartyIndex;
    }

    public static class InvalidVote extends Exception {

    }


}
