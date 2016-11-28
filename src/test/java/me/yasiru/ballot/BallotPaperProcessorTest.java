package me.yasiru.ballot;

import junit.framework.TestCase;
import org.opencv.core.Core;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BallotPaperProcessorTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        (new File("processed_images/preprocesstest.jpg")).delete();
        (new File("processed_images/segmenttest.jpg")).delete();
    }
    public void testProcess() throws Exception {

    }

    public void testPreprocess() throws Exception {
        BallotPaper ballotPaper = BallotPaper.loadFromFile("balotpapers/001.jpg");
        BallotPaperProcessor.preprocess(ballotPaper);
//        BallotPaper.writeToFile("processed_images/preprocesstest.jpg", ballotPaper);
    }

    public void testSegment() throws Exception {
        BallotPaper ballotPaper = BallotPaper.loadFromFile("balotpapers/001.jpg");
        BallotPaperProcessor.preprocess(ballotPaper);
        BallotPaperProcessor.segment(ballotPaper);
//        BallotPaper.writeToFile("processed_images/segmenttest.jpg", ballotPaper);
    }
}