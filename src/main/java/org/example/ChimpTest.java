package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ChimpTest extends RunnableSolver {
    public static void main(String[] args) {
//        BufferedImage image = Utils.openImage("ChimpTest", "java_img_4_numbers");
//        if (image != null) {
//            boolean[][] whiteMask = Utils.filterBrightness(image);
//            Utils.showImageNative(Utils.visualizeBooleanArray(whiteMask));
//        }
//
//        while (true) {
//
//        }

        new ChimpTest().play();
    }

    // expects an array consisting of the first four blobs (corresponding to 1, 2, 3 and 4)
    public static void kickStartBlobOrder(Blob[] startingBlobs) {
        Arrays.sort(startingBlobs, Comparator.comparing(Blob::getMass));

        blobOrder.add(startingBlobs[0]);
        blobOrder.add(startingBlobs[3]);
        blobOrder.add(startingBlobs[2]);
    }

    public static Blob[] getClickingOrderFromBlobs(Blob[] blobs) {
        if (blobs.length != blobOrder.size() + 1) {
            return null;
        }
        boolean[] orderUsed = new boolean[blobOrder.size()];
        boolean[] blobsUsed = new boolean[blobs.length];
        Blob[] result = new Blob[blobs.length];
        for (int clickOrder = 0; clickOrder < blobOrder.size(); clickOrder++) {
            for (int blobIndex = 0; blobIndex < blobs.length; blobIndex++) {
                if (!blobsUsed[blobIndex] && !orderUsed[clickOrder] && blobOrder.get(clickOrder).equals(blobs[blobIndex])) {
                    result[clickOrder] = blobs[blobIndex];
                    blobsUsed[blobIndex] = true;
                    orderUsed[clickOrder] = true;
                }
            }
        }
        for (int i = 0; i < blobs.length; i++) {
            if (!blobsUsed[i]) {
                result[result.length - 1] = blobs[i];
            }
        }
        return result;
    }

    // boxes should be no bigger than numbers
    public static Blob[] combineUnnecessaryBlobs(Blob[] numbers, Blob[] boxes) {
        Blob[] newNumbers = new Blob[boxes.length];
        boolean[] used = new boolean[numbers.length];

        for (int i = 0; i < boxes.length; i++) {
            Blob blob = null;
            for (int j = 0; j < numbers.length; j++) {
                if (!used[j] && boxes[i].getBoundingBox().contains(numbers[j].getBoundingBox())) {
                    blob = numbers[j].merge(blob);
                    used[j] = true;
                }
            }
            newNumbers[i] = blob;
        }

        return newNumbers;
    }


    static ArrayList<Blob> blobOrder = new ArrayList<>(3);
    Point[] clickInstructions;

    boolean isListening = true;
    boolean isSolving = false;
    boolean pressedContinue = false;

    static Color boxColor = new Color(65, 147, 214);

    static Rectangle debugWorkingArea = new Rectangle(122, 178, 2407, 524);
    static Point debugStartPoint = new Point(1340, 594);
    Point continueButton;

    @Override
    public void run() {
        if (!isDebug) {
            BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

            utils.setWorkinoArea(workingArea);

            boolean[][] yellowResult = Utils.filterForColor(utils.getScreenshot(), Utils.startButtonColor);
            Point startButtonPosition = utils.toGlobal(Utils.averagePositionOfTrueValues(yellowResult));

            continueButton = startButtonPosition;

            utils.move(startButtonPosition);
            utils.pressM1();
        }
        else {
            utils.setWorkinoArea(debugWorkingArea);

            continueButton = debugStartPoint;

            utils.move(debugStartPoint);
            utils.pressM1();
        }


        BufferedImage screenshot = utils.getScreenshot();
        boolean[][] mask = Utils.filterBrightness(screenshot);
        Blob[] numberBlobs = Blob.getBlobs(mask, 1);
        Blob[] boxBlobs;

        while (numberBlobs.length != 4) {
            screenshot = utils.getScreenshot();
            mask = Utils.filterBrightness(screenshot);
            numberBlobs = Blob.getBlobs(mask, 1);
        }

        kickStartBlobOrder(numberBlobs);


        while (isActive()) {
            if (blobOrder.size() >= 40) {
                halt();
            }
            screenshot = utils.getScreenshot();
            mask = Utils.filterBrightness(screenshot);
            numberBlobs = Blob.getBlobs(mask, 1);
            boxBlobs = Blob.getBlobs(Utils.filterForColor(screenshot, boxColor), 10);
            numberBlobs = combineUnnecessaryBlobs(numberBlobs, boxBlobs);
            if (isListening && !isSolving) {
                if (numberBlobs.length == blobOrder.size() + 1) {
                    Blob[] clickOrderLocal = getClickingOrderFromBlobs(numberBlobs);
                    blobOrder.add(clickOrderLocal[clickOrderLocal.length - 1]);
                    clickInstructions = new Point[clickOrderLocal.length];
                    for (int i = 0; i < clickOrderLocal.length; i++) {
                        clickInstructions[i] = utils.toGlobal(clickOrderLocal[i].getCenterOfMass());
                    }
                    isListening = false;
                    isSolving = true;
                }
            }
            else if (!isListening && isSolving) {
                for (int i = 0; i < clickInstructions.length; i++) {
                    utils.move(clickInstructions[i]);
                    utils.pressM1();
                }

                isSolving = false;
                isListening = false;
            }
            else if (!isListening && !isSolving) {
                utils.move(continueButton);
                utils.pressM1();
                isListening = true;
            }
        }
    }
}
