package org.example;

import java.awt.*;
import java.util.*;

public class Blob {
    private Point[] containedPoints;
    private Point centerOfMass;
    private Rectangle boundingBox;
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
        centerOfMass = new Point(xSum/containedPoints.length, ySum/containedPoints.length);
    }

    public Point getCenterOfMass() {
        return centerOfMass;
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

        Blob[] empty = new Blob[0];
        return blobs.toArray(empty);
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
