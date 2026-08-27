package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Typing extends RunnableSolver {
    static boolean isTest = false;
    public static void main(String[] args) {
        if (isTest) {
            BufferedImage img1 = Utils.openImage("Typing", "java_img_1787836858076");
            BufferedImage img2 = Utils.openImage("Typing", "java_img_1787836859956");

            System.out.println("img1");
            System.out.println("green: " + countPixelsOfHue(img1, greenHue));
            System.out.println("red: " + countPixelsOfHue(img1, redHue));
            System.out.println("\n");
            Utils.showImageNative(Utils.visualizeBooleanArray(maskForHue(img1, redHue)));

            System.out.println("img2");
            System.out.println("green: " + countPixelsOfHue(img2, greenHue));
            System.out.println("red: " + countPixelsOfHue(img2, redHue));
            System.out.println("\n");

            System.out.println("pass: " + isLastCharacterCorrect(img2, countPixelsOfHue(img1, greenHue), countPixelsOfHue(img1, redHue)));
            while (true);
        }
        else {
            new Typing().play();
        }
    }

    public static boolean[][] maskForHue(BufferedImage image, double hue) {
        double tolerance = 0.075;
        boolean[][] result = new boolean[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color curr = new Color(image.getRGB(x, y));
                result[y][x] = Math.abs(hue - Utils.getHSV(curr)[0]) < tolerance;
            }
        }
        int level = 0;
        return Utils.dilate(Utils.erode(result, level), level);
    }

    public static int countPixelsOfHue(BufferedImage image, double hue) {
        Blob[] blobs = Blob.getBlobs(maskForHue(image, hue), 5);
        int totalMass = 0;
        for (int i = 0; i < blobs.length; i++) {
            totalMass += blobs[i].getMass();
        }
        return totalMass;
    }

    public static boolean isLastCharacterCorrect(BufferedImage image, int lastAttemptGreenPixels, int lastAttemptRedPixels) {
        int diffGreen = countPixelsOfHue(image, greenHue) - lastAttemptGreenPixels;
        int diffRed = countPixelsOfHue(image, redHue) - lastAttemptRedPixels;
        return diffGreen > 0 && diffRed <= 0;
    }

    static double redHue = Utils.getHSV(new Color(245,121,125))[0];
    static double greenHue = Utils.getHSV(new Color(135,232,143))[0];

    int lastAttemptGreenPixels = 0;
    int lastAttemptRedPixels = 0;

    int currentIndex = 0;

    StringBuilder builder = new StringBuilder(200);
    ArrayList<RobotTyper> normals = new ArrayList<>(70);
    ArrayList<RobotTyper> upperCases = new ArrayList<>(70);
    ArrayList<RobotTyper> whatToUse = upperCases;
    RobotTyper.RobotKeyStroke backspace;

    Set<String> useUpperCaseAfter = new HashSet<>(3);

    Rectangle debugWorkingArea = new Rectangle(68, 178, 2461, 524);
    @Override
    public void run() {
        if (!isDebug) {
            BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

            utils.setWorkinoArea(workingArea);
        }
        else {
            utils.setWorkinoArea(debugWorkingArea);
        }

        backspace = new RobotTyper.RobotKeyStroke(robot, '\b');


        normals.add(new RobotTyper(robot, " "));
        for (char i = 'a'; i <= 'z'; i++) {
            normals.add(new RobotTyper(robot, String.valueOf(i)));
        }
        normals.add(new RobotTyper(robot, ", "));
        normals.add(new RobotTyper(robot, ". "));
        normals.add(new RobotTyper(robot, "- "));
        normals.add(new RobotTyper(robot, "? "));
        normals.add(new RobotTyper(robot, "! "));
        for (char i = '0'; i <= '9'; i++) {
            normals.add(new RobotTyper(robot, String.valueOf(i)));
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            normals.add(new RobotTyper(robot, String.valueOf(i)));
            upperCases.add(new RobotTyper(robot, String.valueOf(i)));
        }
        normals.add(new RobotTyper(robot, "'"));

        useUpperCaseAfter.add(". ");
        useUpperCaseAfter.add("? ");
        useUpperCaseAfter.add("! ");


        for (char i = 'a'; i <= 'z'; i++) {
            upperCases.add(new RobotTyper(robot, String.valueOf(i)));
        }
        upperCases.add(new RobotTyper(robot, " "));
        upperCases.add(new RobotTyper(robot, ", "));
        upperCases.add(new RobotTyper(robot, ". "));
        upperCases.add(new RobotTyper(robot, "- "));
        upperCases.add(new RobotTyper(robot, "? "));
        upperCases.add(new RobotTyper(robot, "! "));
        for (char i = '0'; i <= '9'; i++) {
            upperCases.add(new RobotTyper(robot, String.valueOf(i)));
        }
        upperCases.add(new RobotTyper(robot, "'"));


        BufferedImage screenshot = utils.getScreenshot();

        int dilationErosionLevel = 5;

        boolean[][] masked = Utils.erode(Utils.dilate(Utils.filterBrightness(screenshot), dilationErosionLevel), dilationErosionLevel);
        Blob[] blobs = Blob.getBlobs(masked, 1, false);

        utils.setWorkinoArea(blobs[0].getBoundingBox());

        while (isActive()) {
            robot.delay(20);
            screenshot = utils.getScreenshot();
            if (currentIndex > whatToUse.size()) {
                Utils.saveImage(screenshot, "Typing");
                halt();
            }
            else {
                if (isLastCharacterCorrect(screenshot, lastAttemptGreenPixels, lastAttemptRedPixels)) {
                    if (useUpperCaseAfter.contains(whatToUse.get(currentIndex - 1).string)) {
                        whatToUse = upperCases;
                    }
                    else {
                        whatToUse = normals;
                    }
                    builder.append(whatToUse.get(currentIndex - 1).string);
                    Utils.saveImage(screenshot, "Typing");
                    currentIndex = 0;
                }
                if (currentIndex != 0) {
                    for (int i = 0; i < whatToUse.get(currentIndex - 1).sequence.length; i++) {
                        backspace.perform();
                    }
                }
                whatToUse.get(currentIndex).perform();
                currentIndex++;



                lastAttemptGreenPixels = countPixelsOfHue(screenshot, greenHue);
                lastAttemptRedPixels = countPixelsOfHue(screenshot, redHue);
            }
        }

        System.out.println(builder);
    }
}
