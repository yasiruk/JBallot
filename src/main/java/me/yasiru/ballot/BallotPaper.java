package me.yasiru.ballot;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BallotPaper {
    private Mat image;
    private Mat partyVoteSegment;
    private Mat prefVoteSegment;
    private Rect prefVoteRR;
    private Rect partyVoteRR;
    private List<MatOfPoint> externalContours;
    private List<MatOfPoint> allCountours;
    private Mat externalContoursList;
    private List<Rect> voteBoxes;
    private Rect votedPartyBox;
    private boolean isValidVote;
    private boolean error;
    private String filename;
    private String votedParty;
    private List<Integer> preferenceVotes;


    public BallotPaper() {
        super();
        partyVoteSegment = new Mat();
        prefVoteSegment = new Mat();

        image = new Mat();
        externalContoursList = new Mat();
        externalContours = new ArrayList<MatOfPoint>();

        isValidVote = false;
        error = false;
    }

    public static BallotPaper loadFromFile(String file) {
        Mat m = Imgcodecs.imread(file, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        BallotPaper ballotPaper = new BallotPaper();
        ballotPaper.image = m;
        ballotPaper.filename = file;
        return ballotPaper;
    }

    public static void writeToFile(String file, BallotPaper ballotPaper) {
        Imgcodecs.imwrite(file, ballotPaper.image);
    }

    public static void writeToFile(String file, Mat mat) {
        Imgcodecs.imwrite(file, mat);
    }

    public Mat getPartyVoteSegment() {
        return partyVoteSegment;
    }

    public void setPartyVoteSegment(Mat partyVoteSegment) {
        this.partyVoteSegment = partyVoteSegment;
    }

    public Mat getPrefVoteSegment() {
        return prefVoteSegment;
    }

    public void setPrefVoteSegment(Mat prefVoteSegment) {
        this.prefVoteSegment = prefVoteSegment;
    }

    public List<MatOfPoint> getExternalContours() {
        return externalContours;
    }

    public void setExternalContours(List<MatOfPoint> externalContours) {
        this.externalContours = externalContours;
    }

    public List<Rect> getVoteBoxes() {
        return voteBoxes;
    }

    public void setVoteBoxes(List<Rect> voteBoxes) {
        this.voteBoxes = voteBoxes;
    }

    public Rect getVotedPartyBox() {
        return votedPartyBox;
    }

    public void setVotedPartyBox(Rect votedPartyBox) {
        this.votedPartyBox = votedPartyBox;
    }

    public boolean isValidVote() {
        return isValidVote;
    }

    public void setValidVote(boolean validVote) {
        isValidVote = validVote;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Mat getExternalContoursList() {
        return externalContoursList;
    }

    public void setExternalContoursList(Mat externalContoursList) {
        this.externalContoursList = externalContoursList;
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getVotedParty() {
        return votedParty;
    }

    public void setVotedParty(String votedParty) {
        this.votedParty = votedParty;
    }

    public List<Integer> getPreferenceVotes() {
        return preferenceVotes;
    }

    public void setPreferenceVotes(List<Integer> preferenceVotes) {
        this.preferenceVotes = preferenceVotes;
    }

    public List<MatOfPoint> getAllCountours() {
        return allCountours;
    }

    public void setAllCountours(List<MatOfPoint> allCountours) {
        this.allCountours = allCountours;
    }

    public Rect getPrefVoteRR() {
        return prefVoteRR;
    }

    public void setPrefVoteRR(Rect prefVoteRR) {
        this.prefVoteRR = prefVoteRR;
    }

    public Rect getPartyVoteRR() {
        return partyVoteRR;
    }

    public void setPartyVoteRR(Rect partyVoteRR) {
        this.partyVoteRR = partyVoteRR;
    }

    @Override
    public String toString() {
        if (error) {
            return "Invalid Vote";
        } else {
            String output = "Valid Vote ";
            output += "Voted party : " + votedParty + " Preference Votes : < ";
            if (preferenceVotes != null) {
                for (Integer i :
                        preferenceVotes) {
                    output += i + " ";
                }
            }
            return output + ">";
        }
    }
}
