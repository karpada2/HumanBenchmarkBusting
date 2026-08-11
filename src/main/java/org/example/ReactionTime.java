package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ReactionTime extends RunnableSolver {
    public static void main(String[] args){
        new ReactionTime().play();
    }

    Point pressPoint = new Point(1000, 360);
    int repeats = 10;
    Color blue = new Color(43, 135, 209);
//    Color red = new Color(206, 38, 54);
    Color green = new Color(75, 219, 106);

    Color[] waitForOrdered = new Color[]{blue, green};
    int index = 0;

    @Override
    public void run() {
        BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

        utils.move(new Point(workingArea.x + workingArea.width/2, workingArea.y + workingArea.height/2));
        while (isActive()) {
            if (repeats == 0) {
                halt();
            }
            if (utils.getPixelColorAtCursor().equals(waitForOrdered[index])) {
                utils.pressM1();
                index++;
                index = index%waitForOrdered.length;
                repeats--;
            }
        }
    }
}