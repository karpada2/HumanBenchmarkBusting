package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AimTrainer extends RunnableSolver {
    public static void main(String[] args) {
        new AimTrainer().play();
    }

    Color targetColor = new Color(149, 195, 232);
    Point lastClick = null;

    @Override
    public void run() {
        BufferedImage initialFullScreen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        Rectangle workingArea = Utils.getWorkingArea(initialFullScreen);

        utils.setWorkinoArea(workingArea);

        int targetsClicked = 0;

        while (isActive()) {
            if (targetsClicked >= 31) {
                halt();
            }
            else {
                boolean[][] mask = Utils.erode(Utils.dilate(Utils.filterForColor(utils.getScreenshot(), targetColor), 3), 3);

                Point target = Utils.averagePositionOfTrueValues(mask);

                if (lastClick == null || !Utils.equals(lastClick, target, 5)) {
                    utils.move(utils.toGlobal(target));
                    utils.pressM1();
                    targetsClicked++;
                    lastClick = target;
                }
            }
        }
    }
}
