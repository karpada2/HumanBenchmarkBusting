package org.example;

import java.awt.*;
import java.awt.event.InputEvent;

public class Utils {
    Robot robot;
    public Utils(Robot robot) {
        this.robot = robot;
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
}
