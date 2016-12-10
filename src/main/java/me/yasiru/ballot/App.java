package me.yasiru.ballot;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        BallotResultWriter ballotResultWriter;
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            List<Party> parties = new ArrayList<Party>();
            List<String> files = getFilesInFolder(new File("config_images/parties/"));
            Mat temp;
            for (String file :
                    files) {
                temp = Imgcodecs.imread("config_images/parties/" + file);
                parties.add(new Party(temp, file));
            }
            BallotPaperProcessor.initialize(Imgcodecs.imread("config_images/empty_pref_votes.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE), parties);
            ballotResultWriter = new BallotResultWriter("results.csv");
            for (String imagename :
                    balotPapers) {
//                executorService.submit(new BalotPaperProcessJob("balotpapers/" + imagename, ballotResultWriter));
                new BalotPaperProcessJob("balotpapers/" + imagename, ballotResultWriter).run();
            }
            executorService.shutdown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
