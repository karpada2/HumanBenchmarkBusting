package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jdk.jshell.execution.Util;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;


public class SequenceMemory extends RunnableSolver {
    public static void main(String[] args) {
        new SequenceMemory().play();
    }

    public static int getPointIndex(Point point) {
        if (point.equals(new Point(1180, 335))) {
            return 1;
        }
        else if (point.equals(new Point(1312, 335))) {
            return 2;
        }
        else if (point.equals(new Point(1444, 335))) {
            return 3;
        }
        else if (point.equals(new Point(1180, 467))) {
            return 4;
        }
        else if (point.equals(new Point(1312, 467))) {
            return 5;
        }
        else if (point.equals(new Point(1444, 467))) {
            return 6;
        }
        else if (point.equals(new Point(1180, 599))) {
            return 7;
        }
        else if (point.equals(new Point(1312, 599))) {
            return 8;
        }
        else if (point.equals(new Point(1444, 599))) {
            return 9;
        }
        return 0;
    }

    public static void printPressOrder(Collection<Point> points) {
        int[] output = new int[points.size()];
        Iterator<Point> iterator = points.iterator();
        for (int i = 0; i < points.size(); i++) {
            output[i] = getPointIndex(iterator.next());
        }
        System.out.println(Arrays.toString(output));
    }

    static Color startButtonYellow = new Color(255, 209, 84);
    static Color normalSquareColor = new Color(37, 115, 193);
    static Color clickSquareColor = new Color(255, 255, 255);



    Deque<Point> pressOrder = new ArrayDeque<>();
    Point lastAdded = null;
    ArrayList<Point> actualPressOrder = new ArrayList<>();

    boolean isListening = true;
    boolean isSolving = false;

    boolean workingAreaListen = true;

    int expectedSize = 1;

    Point debugStartPoint = new Point(1313, 569);
    Rectangle debugWorkingArea = new Rectangle(68, 178, 2461, 524);

    boolean debug = true;

    @Override
    public void run() {
        if (!debug) {
            BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

            utils.setWorkinoArea(workingArea);

            boolean[][] yellowResult = Utils.filterForColor(utils.getScreenshot(), startButtonYellow);
            Point startButtonPosition = utils.toGlobal(Utils.averagePositionOfTrueValues(yellowResult));
            utils.move(startButtonPosition);
            utils.pressM1();
        }
        else {
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
                        isSolving = false;
                    }
                }
            });
            utils.setWorkinoArea(debugWorkingArea);
            utils.move(debugStartPoint);
            utils.pressM1();
        }

        long startTime = System.currentTimeMillis();


        while (isActive()) {
            BufferedImage screenshot = utils.getScreenshot();
            if (isListening) {
                if (workingAreaListen) {
                    if (Utils.any(Utils.filterForColor(screenshot, normalSquareColor), 50)) {
                        boolean[][] allSquaresMask = Utils.dilate(Utils.erode(Utils.union(Utils.filterForColor(screenshot, normalSquareColor), Utils.filterForColor(screenshot, clickSquareColor)), 10), 30);
                        utils.setWorkinoArea(Blob.getBlobs(allSquaresMask, 1)[0].getBoundingBox());
                        workingAreaListen = false;
                    }
                }
                else {
                    boolean[][] temp = Utils.erode(Utils.filterForColor(screenshot, clickSquareColor), 10);
                    Blob[] blobs = Blob.getBlobs(temp, 1);

                    if (pressOrder.size() < expectedSize) {
                        if (Blob.getBlobs(Utils.erode(Utils.filterForColor(screenshot, normalSquareColor), 10), 1).length == 8 && (pressOrder.isEmpty() || !utils.toGlobal(blobs[0].getCenterOfMass()).equals(lastAdded))) {
                            lastAdded = utils.toGlobal(blobs[0].getCenterOfMass());
                            pressOrder.add(lastAdded);
                        }
                    }
                    if (pressOrder.size() == expectedSize) {
                        String fileName = "java_img_" + startTime + "_" + System.currentTimeMillis() + "_" + getPointIndex(lastAdded);
                        Utils.saveImage(screenshot, "SequenceMemory", fileName);
                        actualPressOrder.add(lastAdded);
                        pressOrder.clear();

                        lastAdded = null;
                        expectedSize++;

                        isListening = false;
                        isSolving = true;

                        printPressOrder(actualPressOrder);
                    }
                }
            }
            else if (isSolving && Blob.getBlobs(Utils.filterForColor(screenshot, normalSquareColor), 1).length == 9) {
                for (int i = 0; i < actualPressOrder.size(); i++) {
                    utils.move(actualPressOrder.get(i));
                    utils.pressM1();
                }

                isSolving = false;
            }
            else if (!isSolving && !isListening) {
                Blob[] blobs = Blob.getBlobs(Utils.filterForColor(screenshot, normalSquareColor), 1);
                isListening = blobs.length == 9;
            }
        }
    }
}
