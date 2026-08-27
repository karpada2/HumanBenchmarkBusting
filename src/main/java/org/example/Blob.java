package org.example;

import java.awt.*;
import java.util.*;

public class Blob {
    private Point[] containedPoints;
    private Rectangle boundingBox;
    private boolean[][] mask;
    public Blob(Point[] points) {
        this.containedPoints = points;
        int xSum = 0;
        int ySum = 0;

        int minY = -1;
        int minX = -1;
        int maxY = -1;
        int maxX = -1;


        for (int i = 0; i < containedPoints.length; i++) {
            if (minY == -1 || containedPoints[i].y < minY) {
                minY = containedPoints[i].y;
            }
            if (minX == -1 || containedPoints[i].x < minX) {
                minX = containedPoints[i].x;
            }

            if (maxY == -1 || containedPoints[i].y > maxY) {
                maxY = containedPoints[i].y;
            }
            if (maxX == -1 || containedPoints[i].x > maxX) {
                maxX = containedPoints[i].x;
            }

            boundingBox = new Rectangle(minX, minY, maxX - minX, maxY - minY);

            xSum += containedPoints[i].x;
            ySum += containedPoints[i].y;
        }
        mask = new boolean[boundingBox.height + 1][boundingBox.width + 1];
        for (int i = 0; i < containedPoints.length; i++) {
            mask[containedPoints[i].y - boundingBox.y][containedPoints[i].x - boundingBox.x] = true;
        }
    }

    public Point getCenterOfMass() {
        double[] centroid = getCentroid();
        return new Point(boundingBox.x + (int)centroid[0], boundingBox.y + (int)centroid[1]);
    }

    public int getCornerX() {
        return boundingBox.x;
    }

    public int getCornerY() {
        return boundingBox.y;
    }

    public int getMass() {
        return containedPoints.length;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public Point[] getPoints() {
        Point[] points = new Point[containedPoints.length];
        for (int i = 0; i < containedPoints.length; i++) {
            points[i] = containedPoints[i];
        }

        return points;
    }

    public Blob merge(Blob other) {
        if (other == null) {
            return this;
        }

        Set<Point> combinedSet = new HashSet<>(Arrays.stream(other.containedPoints).toList());
        combinedSet.addAll(Arrays.stream(this.containedPoints).toList());

        Point[] empty = new Point[0];

        return new Blob(combinedSet.toArray(empty));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Blob) {
            return equals((Blob)other);
        }
        return super.equals(other);
    }

    public boolean equals(Blob other) {
        if (other.getMass() != this.getMass()) {
            return false;
        }

        Point thisOrigin = new Point(boundingBox.x, boundingBox.y);
        Point otherOrigin = new Point(other.boundingBox.x, other.boundingBox.y);

        HashSet<Point> pointsCheck = new HashSet<>(this.getMass());

        for (int i = 0; i < containedPoints.length; i++) {
            pointsCheck.add(Utils.addPoints(containedPoints[i], otherOrigin));
        }

        for (int i = 0; i < other.containedPoints.length; i++) {
            if (!pointsCheck.contains(Utils.addPoints(other.containedPoints[i], thisOrigin))) {
                return false;
            }
            pointsCheck.remove(Utils.addPoints(other.containedPoints[i], thisOrigin));
        }

        return pointsCheck.isEmpty();
    }

    public double getMoment(int p, int q) {
        double sum = 0;
        for (int x = 0; x < mask[0].length; x++) {
            for (int y = 0; y < mask.length; y++) {
                if (mask[y][x]) {
                    sum += Math.pow(x, p)*Math.pow(y, q);
                }
            }
        }
        return sum;
    }

    // arr[0] is x, arr[1] is y
    public double[] getCentroid() {
        double zeroMoment = getMoment(0, 0);
        return new double[]{getMoment(1, 0)/zeroMoment, getMoment(0, 1)/zeroMoment};
    }

    public double getCentralMoment(int p, int q) {
        double[] centroid = getCentroid();
        double xBar = centroid[0];
        double yBar = centroid[1];

        double sum = 0;
        for (int x = 0; x < mask[0].length; x++) {
            for (int y = 0; y < mask.length; y++) {
                if (mask[y][x]) {
                    sum += Math.pow(x - xBar, p)*Math.pow(y - yBar, q);
                }
            }
        }

        return sum;
    }

    public double getEta(int p, int q) {
        return getCentralMoment(p, q)/(Math.pow(getCentralMoment(0, 0), 1 + ((p + q)/2.0)));
    }

    public double[] getFeaturesAsMoments() {
        return new double[]{
                getEta(2, 0),
                getEta(0, 2),
                getEta(1, 1),
                getEta(3, 0),
                getEta(0, 3),
                getEta(2, 1),
                getEta(1, 2)
        };
    }

    public double getBlobsSimilarity(Blob other) {
        double[] myFeatures = getFeaturesAsMoments();
        double[] otherFeatures = other.getFeaturesAsMoments();
//        double epsilon = 0.0001; // prevents log func from doing shenanigans
//        double distance = 0;
        double dotProduct = 0;
        double myLength = 0;
        double otherLength = 0;

        for (int i = 0; i < myFeatures.length; i++) {
//            double x1 = Math.copySign(Math.log(Math.abs(myFeatures[i]) + epsilon), myFeatures[i]);
//            double x2 = Math.copySign(Math.log(Math.abs(otherFeatures[i]) + epsilon), otherFeatures[i]);
            double x1 = Math.log(myFeatures[i] + Math.sqrt(myFeatures[i]*myFeatures[i] + 1));
            double x2 = Math.log(otherFeatures[i] + Math.sqrt(otherFeatures[i]*otherFeatures[i] + 1));
//            distance += (x1 - x2)*(x1 - x2);
            dotProduct += x1*x2;
            myLength += x1*x1;
            otherLength += x2*x2;
        }

//        distance = Math.sqrt(distance);
//        return distance;

        myLength = Math.sqrt(myLength);
        otherLength = Math.sqrt(otherLength);

        double cosineSimilarity = dotProduct/(myLength*otherLength);

        return cosineSimilarity;
    }

    public static Blob[] getBlobs(boolean[][] data, int minSize) {
        return getBlobs(data, minSize, true);
    }

    public static Blob[] getBlobs(boolean[][] data, int minSize, boolean orthogonalOnly) {
        ArrayList<Blob> blobs = new ArrayList<>(10);
        boolean[][] visited = new boolean[data.length][data[0].length];
        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[0].length; x++) {
                if (data[y][x] && !visited[y][x]) {
                    Blob blob = getBlob(data, x, y, visited, orthogonalOnly);
                    if (blob != null && blob.getMass() >= minSize) {
                        blobs.add(blob);
                    }
                }
            }
        }

        Blob[] result = new Blob[blobs.size()];
        blobs.sort(Comparator.comparing(Blob::getMass));
        for (int i = 0; i < blobs.size(); i++) {
            result[i] = blobs.get(blobs.size() - i - 1);
        }

        return result;
    }

    private static Blob getBlob(boolean[][] data, int x, int y, boolean[][] visited, boolean orthogonalOnly) {
        ArrayList<Point> finalResult = new ArrayList<>(5*(int)(Math.sqrt(data.length*data[0].length)));
        Queue<Point> pointsToCheck = new ArrayDeque<Point>();
        pointsToCheck.add(new Point(x, y));
        while (!pointsToCheck.isEmpty()) {
            Point curr = pointsToCheck.remove();
            if (curr.x < 0 || curr.y < 0 || curr.x >= data[0].length || curr.y >= data.length || visited[curr.y][curr.x]) {
                continue;
            }
            else {
                visited[curr.y][curr.x] = true;
                if (data[curr.y][curr.x]) {
                    pointsToCheck.add(new Point(curr.x + 1, curr.y));
                    pointsToCheck.add(new Point(curr.x - 1, curr.y));
                    pointsToCheck.add(new Point(curr.x, curr.y + 1));
                    pointsToCheck.add(new Point(curr.x, curr.y - 1));
                    if (!orthogonalOnly) {
                        pointsToCheck.add(new Point(curr.x + 1, curr.y + 1));
                        pointsToCheck.add(new Point(curr.x - 1, curr.y - 1));
                        pointsToCheck.add(new Point(curr.x - 1, curr.y + 1));
                        pointsToCheck.add(new Point(curr.x + 1, curr.y - 1));
                    }
                    finalResult.add(curr);
                }
            }
        }

        try {
            Point[] empty = new Point[0];
            return new Blob(finalResult.toArray(empty));
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "{Blob: center: " + getCenterOfMass().toString() + ", mass: " + getMass() + "}";
    }
}
