package me.yasiru.ballot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wik2kassa on 12/4/2016.
 */
public class BallotResultWriter {
    private static BallotResultWriter insttance;
    private String location;
    private PrintWriter writer;


    public BallotResultWriter(String location) throws FileNotFoundException, UnsupportedEncodingException {
        this.location = location;
        writer = new PrintWriter(location, "UTF-8");
        writer.println("image_file, valid_vote, party_vote, pref_votes");
    }

    public synchronized void writeResult(BallotPaper ballotPaper, String paperid) {
        System.out.println(ballotPaper);
        writer.print(paperid + ", " + (!ballotPaper.isError()) + ", " + ballotPaper.getVotedParty() + ",");
        List<Integer> prefVotes = ballotPaper.getPreferenceVotes();
        if (prefVotes != null) {
            for (Integer prefVote :
                    prefVotes) {
                writer.print(" " + prefVote);
            }
        }
        writer.println();
        writer.flush();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (writer != null)
            writer.close();
    }
}
