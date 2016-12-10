package me.yasiru.ballot;

import junit.framework.TestCase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BallotPaperProcessorTest extends TestCase {
    String filename = "balotpapers/003.jpg";

    private static ArrayList<String> getFilesInFolder(final File folder) {
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

    public void setUp() throws Exception {
        super.setUp();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        (new File("processed_images/preprocesstest.jpg")).delete();
        (new File("processed_images/segmenttest.jpg")).delete();
        List<Party> parties = new ArrayList<Party>();
        List<String> files = getFilesInFolder(new File("config_images/parties/"));
        Mat temp;
        for (String file :
                files) {
            temp = Imgcodecs.imread("config_images/parties/" + file);
            parties.add(new Party(temp, file));
        }
        BallotPaperProcessor.initialize(Imgcodecs.imread("config_images/empty_pref_votes.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE), parties);
    }

    public void testProcess() throws Exception {
        String[] arr = {"001.jpg",
                "003.jpg",
                "004.jpg",
                "005.jpg",
                "006.jpg",
                "007.jpg",
                "008.jpg",
                "009.jpg",
                "010.jpg",
                "011.jpg",
                "012.jpg",
                "013.jpg"
        };
        boolean[] expectedErrorValue = {
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        };
        String[] expectedPartyVote = {
                "elephant.jpg",
                null,
                "elephant.jpg",
                "elephant.jpg",
                "bell.jpg",
                "bell.jpg",
                "star.jpg",
                "beetle.jpg",
                "swan.jpg",
                "shell.jpg",
                "bicycle.jpg",
        };
        BallotPaper ballotPaper;
        for (int i = 0; i < arr.length; i++) {
            ballotPaper = BallotPaper.loadFromFile("balotpapers/" + arr[i]);
            BallotPaperProcessor.process(ballotPaper, arr[i]);
//            BallotPaperProcessor.getPrefVotes2(ballotPaper);
            System.out.println("Vote Result for " + arr[i] + " : " + ballotPaper);
            System.out.println("Expected " + expectedPartyVote[i]);
            BallotPaper.writeToFile("processed_images/segpart_" + arr[i], ballotPaper.getPartyVoteSegment());
            BallotPaper.writeToFile("processed_images/segpref_" + arr[i], ballotPaper.getPrefVoteSegment());

            assert (ballotPaper.isError() == expectedErrorValue[i]);
            assert ((ballotPaper.getVotedParty() == null && (ballotPaper.getVotedParty() == expectedPartyVote[i])) || ballotPaper.getVotedParty().equals(expectedPartyVote[i])); //handle null values
        }
    }

}