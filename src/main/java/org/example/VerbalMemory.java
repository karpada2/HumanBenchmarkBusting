package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VerbalMemory extends RunnableSolver {
    public static void main(String[] args) {
        new VerbalMemory().play();
    }

    static Color textColor = new Color(255, 255, 255);
    static int textAreaPadding = 10;


    static boolean debug = false;
    Point debugStartPoint = new Point(1340, 597);
    Rectangle debugWorkingArea = new Rectangle(122, 178, 2407, 524);

    Point idlePoint;

    Point markSeenButton = null;
    Point markNewButton = null;

    Set<ArrayList<Integer>> seenWords = new HashSet<>(1000);

    public void updateButtonPositions(BufferedImage image) {
        boolean[][] mask = Utils.erode(Utils.dilate(Utils.filterForColor(image, Utils.startButtonColor), 10), 5);
        Blob[] blobs = Blob.getBlobs(mask, 1);
        if (blobs.length == 2) {
            Point[] centers = new Point[blobs.length];

            for (int i = 0; i < blobs.length; i++) {
                centers[i] = utils.toGlobal(blobs[i].getCenterOfMass());
            }


            if (centers[0].x < centers[1].x) {
                markSeenButton = centers[0];
                markNewButton = centers[1];
            }
            if (centers[0].x > centers[1].x) {
                markSeenButton = centers[1];
                markNewButton = centers[0];
            }
        }
    }

    public void makeChoice(boolean seen) {
        utils.move(seen ? markSeenButton : markNewButton);
        utils.pressM1();
        utils.move(idlePoint);
    }

    public static int getSumOfColumn(boolean[][] mask, int index) {
        int sum = 0;
        for (int x = 0; x < mask.length; x++) {
            if (mask[x][index]) {
                sum++;
            }
        }
        return sum;
    }

    public static ArrayList<Integer> getAllColumnsSums(boolean[][] mask) {
        Integer[] sums = new Integer[mask[0].length];
        for (int i = 0; i < mask[0].length; i++) {
            sums[i] = getSumOfColumn(mask, i);
        }
        return new ArrayList<>(Arrays.asList(sums));
    }


    @Override
    public void run() {
        if (!debug) {
            BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

            utils.setWorkinoArea(workingArea);

            boolean[][] yellowResult = Utils.filterForColor(utils.getScreenshot(), Utils.startButtonColor);
            Point startButtonPosition = utils.toGlobal(Utils.averagePositionOfTrueValues(yellowResult));

            idlePoint = startButtonPosition;

            utils.move(startButtonPosition);
            utils.pressM1();
        }
        else {
            utils.setWorkinoArea(debugWorkingArea);

            idlePoint = debugStartPoint;

            utils.move(debugStartPoint);
            utils.pressM1();
        }


        BufferedImage screenshot = utils.getScreenshot();
        while (markSeenButton == null) {
            updateButtonPositions(screenshot);
            screenshot = utils.getScreenshot();
        }
        updateButtonPositions(screenshot);

        int longestSequenceStart = -1;
        int longestSequenceLength = -1;
        int secondLongestSequenceStart = -1;
        int secondLongestSequenceLength = -1;
        int currSequenceStart = -1;
        int currSequenceLength = 0;
        boolean last = false;

        boolean[][] textMask = Utils.dilate(Utils.filterForColor(screenshot, textColor), 10);
        for (int y = 0; y < textMask.length; y++) {
            boolean rowContainsTrue = false;

            // does row contain true
            for (int x = 0; x < textMask[0].length; x++) {
                if (textMask[y][x]) {
                    rowContainsTrue = true;
                }
            }


            if (rowContainsTrue && !last) {
                currSequenceStart = y;
                currSequenceLength = 1;
            }
            else if (rowContainsTrue) {
                currSequenceLength++;
            }

            if (!rowContainsTrue && last) {
                if (currSequenceLength > longestSequenceLength) {
                    secondLongestSequenceLength = longestSequenceLength;
                    secondLongestSequenceStart = longestSequenceStart;

                    longestSequenceLength = currSequenceLength;
                    longestSequenceStart = currSequenceStart;
                }
            }


            last = rowContainsTrue;
        }

        Rectangle scoreAndLivesArea = new Rectangle(0, secondLongestSequenceStart, utils.workinoArea.width, secondLongestSequenceLength);
        Rectangle incomingWordsArea = new Rectangle(0, longestSequenceStart - textAreaPadding, utils.workinoArea.width, longestSequenceLength + 2 * textAreaPadding);

        BufferedImage lastScoreAndLives = null;

        int count = 0;
        long startTime = System.currentTimeMillis();


        while (isActive()) {
            screenshot = utils.getScreenshot();
            BufferedImage scoreAndLives = screenshot.getSubimage(scoreAndLivesArea.x, scoreAndLivesArea.y, scoreAndLivesArea.width, scoreAndLivesArea.height);
            Utils.saveImage(screenshot, "VerbalMemory");
            if (lastScoreAndLives == null || !Arrays.deepEquals(Utils.filterForColor(lastScoreAndLives, textColor), Utils.filterForColor(scoreAndLives, textColor))) {
                BufferedImage words = screenshot.getSubimage(incomingWordsArea.x, incomingWordsArea.y, incomingWordsArea.width, incomingWordsArea.height);
                ArrayList<Integer> sums = getAllColumnsSums(Utils.filterForColor(words, textColor));
                count++;
                makeChoice(seenWords.contains(sums));
                seenWords.add(sums);
            }
            lastScoreAndLives = scoreAndLives;
        }

        System.out.println(count);
        System.out.println((double)count/((System.currentTimeMillis()-startTime)/1000.0));
    }
}
