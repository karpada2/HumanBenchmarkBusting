package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    static Color startButtonColor = new Color(255, 209, 84);
    static Color backgroundColor = new Color(43, 135, 209);
    static Color textColor = new Color(255, 255, 255);
    Robot robot;
    Rectangle workinoArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    public Utils(Robot robot) {
        this.robot = robot;
    }

    public BufferedImage getScreenshot() {
        return robot.createScreenCapture(workinoArea);
    }

    public void setWorkinoArea(Rectangle workinoArea) {
        this.workinoArea = new Rectangle(workinoArea.x + this.workinoArea.x, workinoArea.y + this.workinoArea.y, workinoArea.width, workinoArea.height);
    }

    // global (screen) coords
    public Point toGlobal(Point point) {
        return addPoints(workinoArea.getLocation(), point);
    }

    public Point toLocal(Point point) {
        return addPoints(negative(workinoArea.getLocation()), point);
    }

    public Point negative(Point point) {
        return new Point(-point.x, -point.y);
    }

    public Color getPixelColorAtCursor() {
        return getPixelColorAt(MouseInfo.getPointerInfo().getLocation());
    }

    public Color getPixelColorAt(Point point) {
        return robot.getPixelColor(point.x, point.y);
    }

    public void move(Point point) {
        robot.mouseMove(point.x, point.y);
    }

    public void pressM1() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void downM1() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void upM1() {
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static boolean equals(Point p1, Point p2, double tolerance) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)) < tolerance;
    }




    public static BufferedImage openImage(String taskName, String fileName) {
        try {
            File inFile = new File("/home/electrocaruzo/Pictures/HumanBenchmarkScreenshots/" + taskName + "/", fileName + ".png");

            return ImageIO.read(inFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveImage(BufferedImage img, String taskName, String fileName) {
        try {
            File outFile = new File("/home/electrocaruzo/Pictures/HumanBenchmarkScreenshots/" + taskName + "/", fileName + ".png");
            outFile.mkdirs();

            ImageIO.write(img, "png", outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveImage(BufferedImage img, String taskName) {
        String fileName = "java_img_" + System.currentTimeMillis();
        saveImage(img, taskName, fileName);
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

    public static Point addPoints(Point p1, Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }

    public static Color getColor(BufferedImage image, int x, int y) {
        return new Color(image.getRGB(x, y));
    }

    public static Color getColor(BufferedImage image, Point point) {
        return getColor(image, point.x, point.y);
    }

    public static void sortByRows(Blob[] blobs, int yTolerance) {
        // 1. Sort by y first
        Arrays.sort(blobs, Comparator.comparingInt(p -> p.getCornerY()));

        // 2. Walk through and assign a "row id" whenever the gap exceeds tolerance
        Map<Blob, Integer> rowOf = new HashMap<>();
        int rowId = 0;
        int rowAnchorY = blobs[0].getCornerY();
        rowOf.put(blobs[0], rowId);

        for (int i = 1; i < blobs.length; i++) {
            if (blobs[i].getCornerY() - rowAnchorY > yTolerance) {
                rowId++;
                rowAnchorY = blobs[i].getCornerY(); // new row's reference point
            }
            rowOf.put(blobs[i], rowId);
        }

        // 3. Now sort by (rowId, x)
        Arrays.sort(blobs, Comparator
                .comparingInt((Blob blob) -> rowOf.get(blob))
                .thenComparingInt(blob -> blob.getCornerX()));
    }

    // positive fill means higher than, negative means lower than
    public static Blob[] filterBlobsByFillPercentage(Blob[] blobs, double fill) {
        double[] fillPercentages = new double[blobs.length];
        int count = 0;
        for (int i = 0; i < blobs.length; i++) {
            fillPercentages[i] = (double)blobs[i].getMass()/(blobs[i].getBoundingBox().width*blobs[i].getBoundingBox().height);
            if (fill < 0) {
                count += fillPercentages[i] <= -fill ? 1 : 0;
            }
            else {
                count += fillPercentages[i] >= fill ? 1 : 0;
            }
        }
        Blob[] result = new Blob[count];
        int index = 0;
        for (int i = 0; i < blobs.length; i++) {
            if (fill < 0) {
                if (fillPercentages[i] <= -fill) {
                    result[index] = blobs[i];
                    index++;
                }
            }
            else {
                if (fillPercentages[i] >= fill) {
                    result[index] = blobs[i];
                    index++;
                }
            }
        }
        return result;
    }

    public static int getClosestMatch(Blob[] options, Blob match) {
        int maxIndex = 0;
        double maxValue = match.getBlobsSimilarity(options[0]);
        for (int i = 1; i < options.length; i++) {
            if (match.getBlobsSimilarity(options[i]) > maxValue) {
                maxIndex = i;
                maxValue = match.getBlobsSimilarity(options[i]);
            }
        }
        return maxIndex;
    }

    public static boolean any(boolean[][] data, int stepSize) {
        for (int x = 0; x < data[0].length; x += stepSize) {
            for (int y = 0; y < data.length; y += stepSize) {
                if (data[y][x]) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean any(boolean[][] data) {
        return any(data, 1);
    }

    public static boolean[][] filterForColor(BufferedImage image, Color originalColor) {
        int convertedColor = originalColor.getRGB();
        boolean[][] result = new boolean[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                result[y][x] = image.getRGB(x, y) == convertedColor;
            }
        }
        return result;
    }

    public static boolean[][] filterBrightness(BufferedImage image) {
        int threshold = 200;
        boolean[][] result = new boolean[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color curr = new Color(image.getRGB(x, y));
                result[y][x] = curr.getRed() > threshold && curr.getGreen() > threshold && curr.getBlue() > threshold;
            }
        }
        return result;
    }

    public static boolean erodeNextValue(boolean[][] image, int level, int x, int y) {
        for (int x1 = x - level; x1 <= x + level; x1++) {
            for (int y1 = y - level; y1 <= y + level; y1++) {
                if (y1 < 0 || y1 >= image.length || x1 < 0 || x1 >= image[y1].length || !image[y1][x1]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean[][] erode(boolean[][] image, int level) {
        boolean[][] result = new boolean[image.length][image[0].length];
        for (int x = 0; x < image[0].length; x++) {
            for (int y = 0; y < image.length; y++) {
                result[y][x] = erodeNextValue(image, level, x, y);
            }
        }
        return result;
    }

    public static boolean[][] dilate(boolean[][] image, int level) {
        boolean[][] result = new boolean[image.length][image[0].length];
        for (int x = 0; x < image[0].length; x++) {
            for (int y = 0; y < image.length; y++) {
                if (image[y][x]) {
                    for (int x1 = x - level; x1 <= x + level; x1++) {
                        for (int y1 = y - level; y1 <= y + level; y1++) {
                            if (y1 >= 0 && y1 < image.length && x1 >= 0 && x1 < image[y1].length) {
                                result[y1][x1] = true;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static Point averagePositionOfTrueValues(boolean[][] data) {
        int xSum = 0;
        int ySum = 0;
        int count = 0;
        for (int x = 0; x < data[0].length; x++) {
            for (int y = 0; y < data.length; y++) {
                if (data[y][x]) {
                    xSum += x;
                    ySum += y;
                    count++;
                }
            }
        }
        return new Point(xSum/count, ySum/count);
    }

    public static boolean[][] verticalEdgeDetection(boolean[][] inputBoolean) {
        int[][] result = new int[inputBoolean.length][inputBoolean[0].length];
        int[][] input = new int[inputBoolean.length+2][inputBoolean[0].length+2];
        for (int x = 0; x < inputBoolean[0].length; x++) {
            for (int y = 0; y < inputBoolean.length; y++) {
                input[y+1][x+1] = inputBoolean[y][x] ? 1 : 0;
            }
        }

        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                int x1 = x + 1;
                int y1 = y + 1;
                result[y][x] =  (input[y1 - 1][x1 - 1]) + (0 * input[y1 - 1][x1 - 0]) + (-1 * input[y1 - 1][x1 + 1]) +
                                (input[y1 - 0][x1 - 1]) + (0 * input[y1 - 0][x1 - 0]) + (-1 * input[y1 - 0][x1 + 1]) +
                                (input[y1 + 1][x1 - 1]) + (0 * input[y1 + 1][x1 - 0]) + (-1 * input[y1 + 1][x1 + 1]);
            }
        }


        boolean[][] actualResult = new boolean[result.length][result[0].length];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                actualResult[y][x] = result[y][x] != 0;
            }
        }

        return actualResult;
    }

    public static boolean[][] horizontalEdgeDetection(boolean[][] inputBoolean) {
        int[][] result = new int[inputBoolean.length][inputBoolean[0].length];
        int[][] input = new int[inputBoolean.length+2][inputBoolean[0].length+2];
        for (int x = 0; x < inputBoolean[0].length; x++) {
            for (int y = 0; y < inputBoolean.length; y++) {
                input[y+1][x+1] = inputBoolean[y][x] ? 1 : 0;
            }
        }

        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                int x1 = x + 1;
                int y1 = y + 1;
                result[y][x] =  (1 * input[y1 - 1][x1 - 1]) + (1 * input[y1 - 1][x1 - 0]) + (1 * input[y1 - 1][x1 + 1]) +
                                (0 * input[y1 - 0][x1 - 1]) + (0 * input[y1 - 0][x1 - 0]) + (0 * input[y1 - 0][x1 + 1]) +
                                (-1 * input[y1 + 1][x1 - 1]) + (-1 * input[y1 + 1][x1 - 0]) + (-1 * input[y1 + 1][x1 + 1]);
            }
        }

        boolean[][] actualResult = new boolean[result.length][result[0].length];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                actualResult[y][x] = result[y][x] != 0;
            }
        }

        return actualResult;
    }

    public static boolean[][] inverse(boolean[][] arr) {
        boolean[][] result = new boolean[arr.length][arr[0].length];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                result[y][x] = !arr[y][x];
            }
        }
        return result;
    }

    public static boolean[][] union(boolean[][] arr1, boolean[][] arr2) {
        boolean[][] result = new boolean[arr1.length][arr1[0].length];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                result[y][x] = arr1[y][x] || arr2[y][x];
            }
        }
        return result;
    }

    public static boolean[][] intersection(boolean[][] arr1, boolean[][] arr2) {
        boolean[][] result = new boolean[arr1.length][arr1[0].length];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                result[y][x] = arr1[y][x] && arr2[y][x];
            }
        }
        return result;
    }

    public static Rectangle getBoundingBox(boolean[][] data) {
        boolean flag = true;
        int minY = -1;
        for (int y = 0; y < data.length && flag; y++) {
            for (int x = 0; x < data[0].length && flag; x++) {
                if (data[y][x]) {
                    minY = y;
                    flag = false;
                }
            }
        }

        flag = true;
        int maxY = -1;
        for (int y = data.length - 1; y >= 0 && flag; y--) {
            for (int x = data[0].length - 1; x >= 0 && flag; x--) {
                if (data[y][x]) {
                    maxY = y;
                    flag = false;
                }
            }
        }

        flag = true;
        int minX = -1;
        for (int x = 0; x < data[0].length && flag; x++) {
            for (int y = 0; y < data.length && flag; y++) {
                if (data[y][x]) {
                    minX = x;
                    flag = false;
                }
            }
        }

        flag = true;
        int maxX = -1;
        for (int x = data[0].length - 1; x >= 0 && flag; x--) {
            for (int y = data.length - 1; y >= 0 && flag; y--) {
                if (data[y][x]) {
                    maxX = x;
                    flag = false;
                }
            }
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static Rectangle getWorkingArea(BufferedImage fullscreen) {
        boolean[][] backgroundResult = Utils.filterForColor(fullscreen, backgroundColor);
        backgroundResult = Utils.erode(backgroundResult, 2);
        backgroundResult = Utils.erode(Utils.dilate(backgroundResult, 30), 30);

        return getBoundingBox(backgroundResult);
    }


    public static BufferedImage visualizeBooleanArray(boolean[][] array) {
        BufferedImage result = new BufferedImage(array[0].length, array.length, BufferedImage.TYPE_BYTE_GRAY);
        byte[] pixels = ((DataBufferByte) result.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                pixels[y*result.getWidth() + x] =
                        (byte)(array[y][x] ? 255 : 0);
            }
        }

        return result;
    }

    public static BufferedImage drawRectangle(BufferedImage image, Rectangle rectangle) {
        for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
            image.setRGB(x, rectangle.y + rectangle.height, Color.RED.getRGB());
            image.setRGB(x, rectangle.y, Color.RED.getRGB());
        }
        for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
            image.setRGB(rectangle.x + rectangle.width - 1, y, Color.RED.getRGB());
            image.setRGB(rectangle.x, y, Color.RED.getRGB());
        }

        return image;
    }

    public static BufferedImage drawBlob(BufferedImage image, Blob blob) {
        Point[] points = blob.getPoints();
        for (int i = 0; i < points.length; i++) {
            image.setRGB(points[i].x, points[i].y, Color.CYAN.getRGB());
        }

        return image;
    }

    public static BufferedImage drawPoint(BufferedImage image, Point point) {
        image.setRGB(point.x, point.y, Color.GREEN.getRGB());
        return image;
    }
}
