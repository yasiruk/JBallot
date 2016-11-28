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
                BallotPaper ballotPaper = BallotPaper.loadFromFile("balotpapers/" + imagename);
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
