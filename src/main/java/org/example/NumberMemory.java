package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;


public class NumberMemory extends RunnableSolver {
    enum State {
        LISTEN, // when the number needed to remember is shown
        INPUT_TRANSITION, // transition between LISTEN to INPUT, should wait
        INPUT, // when need to input the number
        RESULT_TRANSITION, // transition between INPUT to RESULT, should wait
        RESULT, // transition screen from INPUT -> LISTEN
    }
    public static void main(String[] args) {
        boolean isTest = false;
        if (isTest) {
            BufferedImage[] images = new BufferedImage[10];
            Blob[] options = new Blob[10];
            for (int i = 0; i < 10; i++) {
                images[i] = Utils.openImage("NumberMemory", "reference_" + i);
                options[i] = getNumberBlobs(images[i])[0];
                Utils.drawBlob(images[i], options[i]);
            }

            BufferedImage test = Utils.openImage("NumberMemory", "query_771109567121785404");
            Blob[] numbers = getNumberBlobs(test, false);

            StringBuilder builder = new StringBuilder(numbers.length + 1);
            for (int i = 0; i < numbers.length; i++) {
                builder.append(Utils.getClosestMatch(options, numbers[i]));
                System.out.println(numbers[i].getCornerY());
            }

            System.out.println("771109567217854041");
            System.out.println(builder);

            while (true);
        }
        else{
            new NumberMemory().play();
        }
    }

    public static Blob[] removeVerticalOutliers(Blob[] blobs, int count) {
        double[] yValues = new double[blobs.length];
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = blobs[i].getCentroid()[1] + blobs[i].getBoundingBox().y;
        }
        double[] squaredDistanceToOthers = new double[yValues.length];
        for (int i = 0; i < yValues.length; i++) {
            for (int j = 0; j < yValues.length; j++) {
                if (i != j) {
                    squaredDistanceToOthers[i] += (yValues[i] - yValues[j])*(yValues[i] - yValues[j]);
                }
            }
        }

        for (int i = 0; i < count; i++) {
            int maxIndex = 0;
            for (int j = 0; j < squaredDistanceToOthers.length; j++) {
                if (squaredDistanceToOthers[j] > squaredDistanceToOthers[maxIndex]) {
                    maxIndex = j;
                }
            }
            squaredDistanceToOthers[maxIndex] = -1;
        }

        Blob[] result = new Blob[blobs.length - count];
        int index = 0;
        for (int i = 0; i < squaredDistanceToOthers.length; i++) {
            if (squaredDistanceToOthers[i] != -1) {
                result[index] = blobs[i];
                index++;
            }
        }
        return result;
    }

    public static Blob[] getNumberBlobs(BufferedImage image) {
        return getNumberBlobs(image, true);
    }

    public static Blob[] getNumberBlobs(BufferedImage image, boolean removeOutliers) {
        boolean[][] mask = Utils.filterBrightness(image);
        Blob[] result =
                Utils.filterBlobsByFillPercentage(
                        Blob.getBlobs(
                                mask
                                , minBlobSize, false)
                        , -0.9)
                ;

        if (removeOutliers) {
            result = removeVerticalOutliers(result, 1);
        }

        Utils.sortByRows(result, 20);

        return result;
    }

    static int minBlobSize = 100;

    int expectedDigitCount = 1;


    Blob[] references = new Blob[10];
    int referenceCount = 0;

    BufferedImage shownNumberImage;
    BufferedImage image;

    RobotTyper typer = null;

    State currentState = State.LISTEN;

    Point nextButton = null;
    Point submitButton = null;

    Point debugStartPoint = new Point(1340, 584);
    Rectangle debugWorkingArea = new Rectangle(122, 178, 2407, 524);
    @Override
    public void run() {
        if (!isDebug) {
            BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

            utils.setWorkinoArea(workingArea);

            boolean[][] yellowResult = Utils.filterForColor(utils.getScreenshot(), Utils.startButtonColor);
            Point startButtonPosition = utils.toGlobal(Utils.averagePositionOfTrueValues(yellowResult));
            utils.move(startButtonPosition);
            utils.pressM1();
        }
        else {
            utils.setWorkinoArea(debugWorkingArea);
            utils.move(debugStartPoint);
            utils.pressM1();
        }

        while (isActive()) {
            System.out.println(currentState);
            image = utils.getScreenshot();
            if (currentState == State.LISTEN) {
                if (Blob.getBlobs(Utils.filterBrightness(image), 1, false).length == expectedDigitCount + 1 && shownNumberImage == null) {
                    shownNumberImage = image;
                    expectedDigitCount++;
                    currentState = State.INPUT_TRANSITION;
                }
            }
            else if (currentState == State.INPUT_TRANSITION && Utils.any(Utils.filterForColor(image, Utils.startButtonColor))) {
                submitButton = utils.toGlobal(Utils.averagePositionOfTrueValues(Utils.filterForColor(image, Utils.startButtonColor)));
                currentState = State.INPUT;
            }
            else if (currentState == State.INPUT) {
                if (referenceCount < 10) { // handle filling out references
                    if (references[referenceCount] == null) {
                        if (typer == null) {
                            typer = new RobotTyper(robot, "\b" + referenceCount);
                            typer.perform();
                        }
                        else {
                            Blob[] blobs = getNumberBlobs(image);
                            if (blobs.length == 1) {
                                if (referenceCount == 0 || blobs[0].getBlobsSimilarity(references[referenceCount - 1]) < 0.9995) {
                                    Utils.saveImage(image, "NumberMemory", "reference_" + referenceCount);
                                    references[referenceCount] = blobs[0];
                                    referenceCount++;
                                    if (referenceCount >= 10) {
                                        typer.sequence[0].perform();
                                    }
                                    typer = null;
                                }
                            }
                        }
                    }
                }
                else { // actually input
                    Blob[] numbers = getNumberBlobs(shownNumberImage, false);
                    StringBuilder builder = new StringBuilder(numbers.length + 1);
                    for (int i = 0; i < numbers.length; i++) {
                        builder.append(Utils.getClosestMatch(references, numbers[i]));
                    }
                    Utils.saveImage(shownNumberImage, "NumberMemory", "query_" + builder);
                    builder.append('\n');
                    RobotTyper typer = new RobotTyper(robot, builder.toString());
                    typer.perform();
                    currentState = State.RESULT_TRANSITION;
                    shownNumberImage = null;
                    utils.move(utils.workinoArea.getLocation());
                }
            }
            else if (currentState == State.RESULT_TRANSITION && utils.getPixelColorAt(submitButton) != Utils.startButtonColor && utils.getPixelColorAt(submitButton) != Utils.textColor) {
                currentState = State.RESULT;
            }
            else if (currentState == State.RESULT) {
                if (nextButton == null) {
                    if (Utils.any(Utils.filterForColor(image, Utils.startButtonColor))) {
                        nextButton = utils.toGlobal(Utils.averagePositionOfTrueValues(Utils.filterForColor(image, Utils.startButtonColor)));
                    }
                }
                else {
                    utils.move(nextButton);
                    utils.pressM1();
                    currentState = State.LISTEN;
                }
            }
        }
    }
}
