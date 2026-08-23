package org.example;

import java.awt.*;
import java.awt.event.KeyEvent;

public class RobotTyper {
    public class RobotKeyStroke {
        Robot robot;
        char desiredCharacter;

        public RobotKeyStroke(Robot robot, char c) {
            this.robot = robot;
            this.desiredCharacter = c;
        }

        public void perform() {
            if (desiredCharacter == ' ') {
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_SPACE);
            }
            else if (desiredCharacter == '\n') {
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            }
            else {
                boolean isUpperCase = Character.isUpperCase(desiredCharacter);
                if (isUpperCase) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }
                robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(desiredCharacter)));
                robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(desiredCharacter)));
                if (isUpperCase) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }
            }
            robot.waitForIdle();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        }
    }

    RobotKeyStroke[] sequence;

    public RobotTyper(Robot robot, String input) {
        sequence = new RobotKeyStroke[input.length()];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = new RobotKeyStroke(robot, input.charAt(i));
        }
    }

    public void perform() {
        for (int i = 0; i < sequence.length; i++) {
            sequence[i].perform();
        }
    }
}
