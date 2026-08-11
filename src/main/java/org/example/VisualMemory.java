package org.example;

import javax.imageio.ImageIO;
import javax.tools.Tool;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class VisualMemory extends RunnableSolver {
    public static void main(String[] args) {
//        try {
//            File png = new File("/home/electrocaruzo/Downloads/java_img_4x4.png");
//            BufferedImage img = ImageIO.read(png);
//
//            boolean[][] temp = Utils.erode(Utils.filterForColor(img, clickSquareColor), 20);
//            showImageNative(Utils.visualizeBooleanArray(temp));
//            if (Utils.any(temp, 30)) {
//                try {
//                    Blob[] blobs = Blob.getBlobs(temp, 100);
//                    Point[] whereToPress = new Point[blobs.length];
//                    for (int i = 0; i < blobs.length; i++) {
//                        whereToPress[i] = blobs[i].getCenterOfMass();
//                    }
//                    System.out.println(Arrays.toString(blobs));
//                }
//                catch (StackOverflowError e) {
//                    e.printStackTrace();
//                }
//            }
//
//            Thread.sleep(1000_000);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }


        new VisualMemory().play();
    }

    public static void saveImage(BufferedImage img) {
        try {
            String filename = "java_img_" + System.currentTimeMillis() + ".png";
            File outFile = new File("/home/electrocaruzo/Pictures/HumanBenchmarkScreenshots/VisualMemory/", filename);

            ImageIO.write(img, "png", outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showImageNative(BufferedImage img) {
        try {
            // Create a temporary file that deletes itself when Java exits
            File tempFile = File.createTempFile("java_img_", ".png");
            tempFile.deleteOnExit();

            // Write the buffered image to the temporary file
            ImageIO.write(img, "png", tempFile);

            // Command the host OS to open the file with its default app
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                System.out.println("Desktop API is not supported on this platform.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Color clickSquareColor = new Color(255, 255, 255);

    static Color startButtonYellow = new Color(255, 209, 84);

    static Color normalSquareColor = new Color(37, 115, 193);
    static Color backgroundColor = new Color(43, 135, 209);

    Point[] whereToPress = null;

    static double requiredFillPercentage = 0.95;

    @Override
    public void run() {
        BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

        utils.setWorkinoArea(workingArea);

        boolean[][] yellowResult = Utils.filterForColor(utils.getScreenshot(), startButtonYellow);
        Point startButtonPosition = utils.toGlobal(Utils.averagePositionOfTrueValues(yellowResult));
        utils.move(startButtonPosition);
        utils.pressM1();


        while (isActive()) {
            BufferedImage screenshot = utils.getScreenshot();
            if (whereToPress == null) {
                boolean[][] temp = Utils.filterForColor(screenshot, clickSquareColor);
                boolean[][] eroded = Utils.erode(temp, 2);
                if (Utils.any(eroded, 50)) {
                    try {
                        Blob[] blobs = Blob.getBlobs(eroded, 1, false);
                        int actualLength = blobs.length;
                        for (int i = 0; i < blobs.length; i++) {
                            if (((double) blobs[i].getMass() / (blobs[i].getBoundingBox().height * blobs[i].getBoundingBox().width) < requiredFillPercentage)) {
                                blobs[i] = null;
                                actualLength--;
                            }
                        }
                        if (actualLength > 0) {
                            whereToPress = new Point[actualLength];
                            int index = 0;
                            for (int i = 0; i < blobs.length; i++) {
                                if (blobs[i] != null) {
                                    Utils.drawRectangle(screenshot, blobs[i].getBoundingBox());
                                    Utils.drawBlob(screenshot, blobs[i]);
                                    whereToPress[index] = utils.toGlobal(blobs[i].getCenterOfMass());
                                    index++;
                                }
                            }
                            utils.move(whereToPress[0]);
                            saveImage(screenshot);
                        }
                    }
                    catch (StackOverflowError e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                while (utils.getPixelColorAtCursor().equals(clickSquareColor)) {

                }

                for (int i = 0; i < whereToPress.length; i++) {
                    utils.move(whereToPress[i]);
                    utils.pressM1();
                }
                whereToPress = null;
            }

        }
    }
}
