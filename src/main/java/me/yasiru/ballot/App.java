package me.yasiru.ballot;

import me.yasiru.ballot.BallotProcessor;
import me.yasiru.ballot.VoteCounter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by wik2kassa on 11/24/2016.
 */
public class App {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        final File folder = new File("balotpapers/");
        ArrayList<String> balotPapers = getFilesInFolder(folder);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("voteresults.txt", "UTF-8");
            writer.println("image_file, valid_vote, party_vote");
            for (String imagename :
                    balotPapers) {
                Mat newImage = Imgcodecs.imread("balotpapers/" + imagename);
                Mat bwImage = new Mat();
                Imgproc.cvtColor(newImage, bwImage, Imgproc.COLOR_RGB2GRAY);
                if (newImage.dataAddr() == 0) {
                    System.out.println("Couldn't open file " + imagename);
                } else {
                    System.out.print("Processing " + imagename + " [ ");
                    Mat threshholdedImage = BallotProcessor.threshhold(bwImage, 200);
                    Imgcodecs.imwrite("processed_images/thresh_" + imagename, threshholdedImage);
                    System.out.print("THRESHHOLD : DONE,\t");
                    BallotProcessor.SegmentationResult segmentationResult = BallotProcessor.segment(threshholdedImage);
                    System.out.print("SEGMENTATION : DONE,\t");
                    BallotProcessor.SegmentedVoteSections segmentedVoteSections = BallotProcessor.getVoteSections(threshholdedImage, segmentationResult);
                    System.out.print("ROTATION : " + (90 + segmentationResult.getPreferenceVoteBB().angle) + " ]\t");
                    Imgcodecs.imwrite("processed_images/partysection_" + imagename, segmentedVoteSections.getPartyVoteArea());
                    Imgcodecs.imwrite("processed_images/prefsection_" + imagename, segmentedVoteSections.getPreferenceVoteArea());
                    VoteCounter vc = new VoteCounter();
                    try {
                        writer.println(imagename + ", " + "yes ," + (vc.getPartyVote(segmentedVoteSections.getPartyVoteArea()) + 1));
                    } catch (VoteCounter.InvalidVote invalidVote) {
                        writer.println(imagename + ", " + "no , -");
                    }
                    System.out.println();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }


        return;
    }

    public static ArrayList<String> getFilesInFolder(final File folder) {
        ArrayList<String> files = new ArrayList<String>();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(fileEntry);
            } else {
                files.add(fileEntry.getName());
            }
        }
        return files;
    }
}
