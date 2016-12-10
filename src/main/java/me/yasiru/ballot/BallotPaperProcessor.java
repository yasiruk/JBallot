package me.yasiru.ballot;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wik2kassa on 11/29/2016.
 */
public class BallotPaperProcessor {
    private static int config_gaussianblur_radius = 3;
    private static int config_threshhold_level = 200;
    private static float config_pref_vote_mark_z_score_min = 0.35f;
    private static Size config_party_symbol_template_size = new Size(233, 111);
    private static Mat prefVoteBoxCleanTemplate;
    private static int[] cleanPrefVoteWhiteCount;
    private static Rect[] cleanPrefVoteBoxes;

    private static List<Party> partySymbols;
    private static boolean initialized = false;

    public static void initialize(Mat prefVoteBoxCleanTemplate, List<Party> partySymbols) {
        System.out.println("Initializing...");
        Imgproc.GaussianBlur(prefVoteBoxCleanTemplate, prefVoteBoxCleanTemplate, new Size(config_gaussianblur_radius, config_gaussianblur_radius)
                , 0);
        Imgproc.threshold(prefVoteBoxCleanTemplate, prefVoteBoxCleanTemplate, config_threshhold_level, 255, Imgproc.THRESH_BINARY_INV);
        BallotPaperProcessor.prefVoteBoxCleanTemplate = prefVoteBoxCleanTemplate;

        for (Party p :
                partySymbols) {
            Imgproc.cvtColor(p.getSymbol(), p.getSymbol(), Imgproc.COLOR_RGB2GRAY);
            p.getSymbol().convertTo(p.getSymbol(), CvType.CV_8U);
            Imgproc.resize(p.getSymbol(), p.getSymbol(), config_party_symbol_template_size);
        }
        BallotPaperProcessor.partySymbols = partySymbols;
        cleanPrefVoteBoxes = segmentPrefVoteBoxes(prefVoteBoxCleanTemplate);
        cleanPrefVoteWhiteCount = getPrefVoteWhiteCounts(prefVoteBoxCleanTemplate, "clean");
        initialized = true;
        System.out.println("Initialization Complete");
    }

    public static void process(BallotPaper ballotPaper, String suffix) {
        preprocess(ballotPaper);
        segment(ballotPaper);
        processVotes(ballotPaper, suffix);
        getPrefVotes(ballotPaper, suffix);
    }

    public static void preprocess(BallotPaper ballotPaper) {
        Mat ballotPaperImage = ballotPaper.getImage();
        preprocess(ballotPaperImage);
    }

    public static Mat preprocess(Mat m) {
        Imgproc.GaussianBlur(m, m, new Size(config_gaussianblur_radius, config_gaussianblur_radius)
                , 0);
        Imgproc.threshold(m, m, config_threshhold_level, 255, Imgproc.THRESH_BINARY_INV);
        return m;
    }

    public static boolean segment(BallotPaper ballotPaper) {
        RotatedRect partyVoteBB = null;
        RotatedRect prefVoteBB = null;
        Mat contouredImage = ballotPaper.getImage().clone();
        List<MatOfPoint> contours = ballotPaper.getExternalContours();
        Mat externalCountourIndex = ballotPaper.getExternalContoursList();
        double currentArea;
        double arealimit = ballotPaper.getImage().width() * ballotPaper.getImage().height() * 0.015;
        Imgproc.findContours(contouredImage, contours, externalCountourIndex, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ballotPaper.setAllCountours(contours);

        MatOfPoint currentContour;
        MatOfPoint2f currentContour2f;
        RotatedRect rotatedRect;
        double rotatedRectRatio = 0;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            currentContour2f = new MatOfPoint2f(currentContour.toArray());
            rotatedRect = Imgproc.minAreaRect(currentContour2f);
            currentArea = rotatedRect.size.area();
            rotatedRectRatio = rotatedRect.size.height / rotatedRect.size.width;
            if (rotatedRectRatio < 1.0)
                rotatedRectRatio = 1 / rotatedRectRatio;
            if (currentArea >= arealimit) {
                if (Math.abs(rotatedRectRatio - 1.3) < 0.4) { // Party Vote Area
                    if (partyVoteBB != null) {
                        ballotPaper.setError(true);
                        System.out.printf("error");
                        return false;
                    } else
                        partyVoteBB = rotatedRect;
                }
                if (Math.abs(rotatedRectRatio - 3.9) < 0.1) { // Party Vote Area
                    if (prefVoteBB != null) {
                        ballotPaper.setError(true);
                        System.out.printf("error");
                        return false;
                    } else
                        prefVoteBB = rotatedRect;
                }
//                System.out.println(rotatedRectRatio + ": " + rotatedRect.angle);
                if (partyVoteBB != null && prefVoteBB != null) {
//                    System.out.println("Breaking at : " + (i * 100.0 / contours.size()) + "%");
                    break;
                }
            }
        }
        //rotation correction

        Size partySize = new Size();
        Size prefSize = new Size();

        double rotangle = partyVoteBB.angle;
        if (rotangle < -45) {
            rotangle = -(90 + rotangle);
            partySize.width = (int) Math.round(partyVoteBB.size.height);
            partySize.height = (int) Math.round(partyVoteBB.size.width);
        } else {
            partySize.width = (int) Math.round(partyVoteBB.size.width);
            partySize.height = (int) Math.round(partyVoteBB.size.height);
        }

        Imgproc.warpAffine(ballotPaper.getImage(), ballotPaper.getPartyVoteSegment(),
                Imgproc.getRotationMatrix2D(partyVoteBB.center, rotangle, 1),
                ballotPaper.getImage().size(), Imgproc.INTER_CUBIC);

        rotangle = prefVoteBB.angle;
        if (rotangle < -45) {
            rotangle = -(90 + rotangle);
            prefSize.width = (int) Math.round(prefVoteBB.size.height);
            prefSize.height = (int) Math.round(prefVoteBB.size.width);
        } else {
            prefSize.width = (int) Math.round(prefVoteBB.size.width);
            prefSize.height = (int) Math.round(prefVoteBB.size.height);
        }
        Imgproc.warpAffine(ballotPaper.getImage(), ballotPaper.getPrefVoteSegment(),
                Imgproc.getRotationMatrix2D(prefVoteBB.center, rotangle, 1),
                ballotPaper.getImage().size(), Imgproc.INTER_CUBIC);


        Imgproc.getRectSubPix(ballotPaper.getPartyVoteSegment(), partySize, partyVoteBB.center, ballotPaper.getPartyVoteSegment());
        Imgproc.getRectSubPix(ballotPaper.getPrefVoteSegment(), prefSize, prefVoteBB.center, ballotPaper.getPrefVoteSegment());
//        Imgcodecs.imwrite("processed_images/prefArea.jpg", ballotPaper.getPrefVoteSegment());
        return true;
    }

    public static void processVotes(BallotPaper ballotPaper, String suffix) {
        Mat contouredPartyVotes = ballotPaper.getPartyVoteSegment().clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat externalCountourIndex = new Mat();

        MatOfPoint currentContour;
        Rect rr, original_rr;

        int voteYPos = -500, partyCount = 0;
        boolean voteFound = false;
        List<Rect> parySignBoxes = new ArrayList<Rect>();


        Imgproc.findContours(contouredPartyVotes, contours, externalCountourIndex, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_NONE);
        double voteBoxAreaLimit = ballotPaper.getPartyVoteSegment().width() * ballotPaper.getPartyVoteSegment().width() / 85;
        double partySignAreaLimit = 1.6 * ballotPaper.getPartyVoteSegment().width() * ballotPaper.getPartyVoteSegment().width() / 85;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            original_rr = Imgproc.boundingRect(currentContour);
            rr = original_rr.clone();
            if (rr.area() > voteBoxAreaLimit) {
                if (rr.x > (ballotPaper.getPartyVoteSegment().width() * 7.5) / 9) {
                    rr.x += rr.width / 20;
                    rr.y += rr.height / 20;
                    rr.width = (18 * rr.width) / 20;
                    rr.height = (18 * rr.height) / 20;
                    partyCount++;
                    if (Core.countNonZero(new Mat(ballotPaper.getPartyVoteSegment(), rr)) > (rr.width * rr.height / 25)) {
                        if (voteFound) {
                            ballotPaper.setError(true);
                            return;
                        } else {
                            voteFound = true;
                            voteYPos = rr.y;
                        }
                    }
                } else if (original_rr.x > (ballotPaper.getPartyVoteSegment().width() * 4.0) / 9 && (rr.area() > partySignAreaLimit)) {
                    parySignBoxes.add(original_rr);
//                    Imgcodecs.imwrite("processed_images/party_" + parySignBoxes.size() + ".jpg", new Mat(ballotPaper.getPartyVoteSegment(), original_rr));
                }
            }
        }


        if (parySignBoxes.size() != partyCount) {
            System.out.printf("Party count mismatch");
            ballotPaper.setError(true);
            return;
        }

        //Identify the party
        for (Rect r :
                parySignBoxes) {
            if (Math.abs(r.y - voteYPos) < (r.height / 10)) {
                ballotPaper.setVotedParty(identifyParty(new Mat(ballotPaper.getPartyVoteSegment(), r)));
                break;
            }
        }
        ballotPaper.setPreferenceVotes(getPrefVotes(ballotPaper, suffix));
    }

    public static String identifyParty(Mat partySignBox) {
        Mat templateMatchResult = new Mat(partySignBox.rows(), partySignBox.cols(), CvType.CV_32F);

        Party identifiedParty = null;
        int minVal = partySignBox.width() * partySignBox.height(), whitecount;

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3), new Point(-1, -1)); // kernel performing drode
        Mat xorResult = new Mat();
        //resize the party sign box to the def template size
        Imgproc.resize(partySignBox, partySignBox, config_party_symbol_template_size);

        for (Party p :
                partySymbols) {
            Core.bitwise_xor(partySignBox, p.getSymbol(), xorResult);
            Imgproc.erode(xorResult, xorResult, element);
            Imgproc.threshold(xorResult, xorResult, 60, 255, Imgproc.THRESH_BINARY);
//            Imgcodecs.imwrite("processed_images/xor_" + p.getName(), xorResult);
            whitecount = Core.countNonZero(xorResult);
            if (whitecount < minVal) {
                minVal = whitecount;
                identifiedParty = p;
            }
        }
        return identifiedParty == null ? null : identifiedParty.getName();
    }

    private static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static List<Integer> getPrefVotes(BallotPaper ballotPaper, String suffix) {

        List<Integer> prefVotes = new ArrayList<Integer>();
        float[] wc = new float[40];

        int voteIterator = 0, stepx = 0, stepy = 0, startx, starty, meanval = 0;
        Imgproc.resize(ballotPaper.getPrefVoteSegment(), ballotPaper.getPrefVoteSegment(), prefVoteBoxCleanTemplate.size());
        Core.bitwise_xor(ballotPaper.getPrefVoteSegment(), prefVoteBoxCleanTemplate, ballotPaper.getPrefVoteSegment());
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3), new Point(0, 0)); // kernel performing drode
        Imgproc.erode(ballotPaper.getPrefVoteSegment(), ballotPaper.getPrefVoteSegment(), element);
        Imgproc.threshold(ballotPaper.getPrefVoteSegment(), ballotPaper.getPrefVoteSegment(), 60, 255, Imgproc.THRESH_BINARY);

        Rect iteratorRect = new Rect();
        iteratorRect.width = ballotPaper.getPrefVoteSegment().width() / 10;
        iteratorRect.height = ballotPaper.getPrefVoteSegment().height() / 4;
        stepx = iteratorRect.width;
        stepy = iteratorRect.height;
        startx = iteratorRect.width / 50;
        starty = iteratorRect.height / 50;
        iteratorRect.width -= iteratorRect.width / 25;
        iteratorRect.height -= iteratorRect.height / 25;
        while (voteIterator < 40) {
            iteratorRect.x = startx + (stepx * (voteIterator % 10));
            iteratorRect.y = starty + (stepy * (voteIterator / 10));
            wc[voteIterator] = Core.countNonZero(new Mat(ballotPaper.getPrefVoteSegment(), iteratorRect));
            meanval += wc[voteIterator];
            voteIterator++;
//            Imgproc.rectangle(ballotPaper.getPrefVoteSegment(), iteratorRect.tl(), iteratorRect.br(), new Scalar(255));
        }

        meanval /= 40;
        float ss = 0;
        for (int i = 0; i < 40; i++) {
            ss += (wc[i] - meanval) * (wc[i] - meanval);
        }
        ss = (float) Math.sqrt(ss);
        for (int i = 0; i < 40; i++) {
            wc[i] = (((wc[i] - meanval) / ss));
            if (wc[i] > config_pref_vote_mark_z_score_min)
                prefVotes.add(i + 1);
        }
//        Imgcodecs.imwrite("processed_images/clean/" + suffix + "xor.jpg", ballotPaper.getPrefVoteSegment());
        return prefVotes;
    }

    public static List<Integer> getPrefVotes3(BallotPaper ballotPaper) {
        return null;

    }

    public static List<Integer> getPrefVotes4(BallotPaper ballotPaper, String suffix) {
//        Imgproc.resize(ballotPaper.getPrefVoteSegment(), ballotPaper.getPrefVoteSegment(), prefVoteBoxCleanTemplate.size());
        int[] wc = getPrefVoteWhiteCounts(ballotPaper.getPrefVoteSegment(), suffix);
        Rect[] prefBoxes = segmentPrefVoteBoxes(ballotPaper.getPrefVoteSegment());
//        Imgcodecs.imwrite("processed_images/pf/pfarea.jpg", ballotPaper.getPrefVoteSegment());
        ArrayList<Integer> prefVotes = new ArrayList<Integer>();

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3), new Point(-1, -1)); // kernel performing drode
        Mat xorResult = new Mat();
        Mat prefBox = new Mat();
        //resize the party sign box to the def template size


        for (int i = 0; i < wc.length; i++) {
            Imgproc.resize(new Mat(ballotPaper.getPrefVoteSegment(), prefBoxes[i]), prefBox, cleanPrefVoteBoxes[i].size());
            Core.bitwise_xor(prefBox, new Mat(prefVoteBoxCleanTemplate,
                    cleanPrefVoteBoxes[i]), xorResult);
//            Imgcodecs.imwrite("processed_images/clean/" + suffix + "_xor_" + (i + 1) + ".jpg", xorResult);
            Imgproc.erode(xorResult, xorResult, element);
        }
        for (int i = 0; i < wc.length; i++) {
            if (wc[i] != 0 && ((float) Math.abs(wc[i] - cleanPrefVoteWhiteCount[i]) / wc[i]) > 0.2) {
                prefVotes.add(i + 1);
            }
        }
        return prefVotes;
    }

    private static Rect[] segmentPrefVoteBoxes(Mat prefVoteSegment) {
        Mat coutouredPrefVotes = prefVoteSegment.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat externalCountourIndex = new Mat();
        Mat cd = prefVoteSegment.clone();
        MatOfPoint currentContour;
        Rect rr;
        boolean segmentationSuccessfull = true;

        int voteYPos = -500, partyCount = 0;
        boolean voteFound = false;
        List<Rect> parySignBoxes = new ArrayList<Rect>();

        Imgproc.findContours(coutouredPrefVotes, contours, externalCountourIndex, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE);

        final int prefVoteAreaWidth = prefVoteSegment.width();
        final int prefVoteAreaHeight = prefVoteSegment.height();

        Rect[] prefVoteBoxes = new Rect[40];


        int widthLB = prefVoteSegment.width() / 12;
        int widthUB = prefVoteSegment.width() / 8;
        int averagePrefBoxWidth = 0;
        int averagePrefBoxHeight = 0;
        int boxcount = 0;
        int index;
        for (int i = 0; i < contours.size(); i++) {
            currentContour = contours.get(i);
            rr = Imgproc.boundingRect(currentContour);
            if (rr.width >= widthLB && rr.width <= widthUB) {
//                Imgproc.rectangle(cd, rr.tl(), rr.br(), new Scalar(0, 255, 0));

                index = (int) Math.floor(
                        1 + ((float) rr.x * 10 / prefVoteAreaWidth) + ((40.0 * rr.y / prefVoteAreaHeight))
                );
//                System.out.println(index);
                prefVoteBoxes[index - 1] = rr;
                averagePrefBoxHeight += rr.height;
                averagePrefBoxWidth += rr.width;
                boxcount++;
            }
        }
        //detect S
        averagePrefBoxHeight /= boxcount;
        averagePrefBoxWidth /= boxcount;

        for (int i = 0; i < prefVoteBoxes.length; i++) {
            if (prefVoteBoxes[i] == null) {
                prefVoteBoxes[i] = new Rect();
                prefVoteBoxes[i].x = (prefVoteSegment.width() / 10) * (i % 10);
                prefVoteBoxes[i].y = (prefVoteSegment.height() / 4) * (i / 10);
                prefVoteBoxes[i].width = averagePrefBoxWidth;
                prefVoteBoxes[i].height = averagePrefBoxHeight;
                segmentationSuccessfull = false;
//                System.out.println("Manually segmenting " + (i + 1) + " with " + prefVoteBoxes[i]);
            }

        }
//        if (segmentationSuccessfull)
//            System.out.println("Successfully perfomed auto segmentation of  pref area");
        return prefVoteBoxes;
    }


    private static int[] getPrefVoteWhiteCounts(Mat prefVotes, String suffix) {
        int[] whitecount = new int[40];
        Rect borderRect = new Rect();
        Rect[] rects = segmentPrefVoteBoxes(prefVotes);
        for (int i = 0; i < rects.length; i++) {
            Imgproc.rectangle(prefVotes, rects[i].tl(), rects[i].br(), new Scalar(0), rects[i].width / 5);
            whitecount[i] = (int) ((100000 * Core.countNonZero(new Mat(prefVotes, rects[i]))) / rects[i].area());
//            Imgcodecs.imwrite("processed_images/clean/" + suffix + "wc1_" + i + ".jpg", new Mat(prefVotes, rects[i]));
        }
        return whitecount;
    }
}
