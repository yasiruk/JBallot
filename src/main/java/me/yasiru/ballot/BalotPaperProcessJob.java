package me.yasiru.ballot;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by wik2kassa on 12/4/2016.
 */
public class BalotPaperProcessJob implements Runnable {
    private String paperLocation;
    private BallotResultWriter ballotResultWriter;
    private BallotPaper ballotPaper;

    public BalotPaperProcessJob(String paperLocation, BallotResultWriter ballotResultWriter) {
        this.paperLocation = paperLocation;
        this.ballotResultWriter = ballotResultWriter;
    }

    public void run() {
        System.out.println("Process started for " + paperLocation);
        ballotPaper = BallotPaper.loadFromFile(paperLocation);
        BallotPaperProcessor.process(ballotPaper, paperLocation);
        ballotResultWriter.writeResult(ballotPaper, paperLocation);
    }
}
