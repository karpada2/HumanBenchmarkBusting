package org.example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class RobotTyper {
    public static class RobotKeyStroke {
        Robot robot;
        char desiredCharacter;

        public RobotKeyStroke(Robot robot, char c) {
            this.robot = robot;
            this.desiredCharacter = c;
        }

        public char getCharacter() {
            return desiredCharacter;
        }

        private static final Map<Character, Character> SHIFTED_SYMBOLS = new HashMap<>();
        static {
            SHIFTED_SYMBOLS.put('!', '1');
            SHIFTED_SYMBOLS.put('@', '2');
            SHIFTED_SYMBOLS.put('#', '3');
            SHIFTED_SYMBOLS.put('$', '4');
            SHIFTED_SYMBOLS.put('%', '5');
            SHIFTED_SYMBOLS.put('^', '6');
            SHIFTED_SYMBOLS.put('&', '7');
            SHIFTED_SYMBOLS.put('*', '8');
            SHIFTED_SYMBOLS.put('(', '9');
            SHIFTED_SYMBOLS.put(')', '0');
            SHIFTED_SYMBOLS.put('?', '/');
            SHIFTED_SYMBOLS.put(':', ';');
            SHIFTED_SYMBOLS.put('"', '\'');
            SHIFTED_SYMBOLS.put('<', ',');
            SHIFTED_SYMBOLS.put('>', '.');
            SHIFTED_SYMBOLS.put('_', '-');
            SHIFTED_SYMBOLS.put('+', '=');
            SHIFTED_SYMBOLS.put('{', '[');
            SHIFTED_SYMBOLS.put('}', ']');
            SHIFTED_SYMBOLS.put('|', '\\');
            SHIFTED_SYMBOLS.put('~', '`');
        }

        public void perform() {
            try {
                if (desiredCharacter == ' ') {
                    robot.keyPress(KeyEvent.VK_SPACE);
                    robot.keyRelease(KeyEvent.VK_SPACE);
                } else if (desiredCharacter == '\n') {
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                } else {
                    boolean needsShift = Character.isUpperCase(desiredCharacter)
                            || SHIFTED_SYMBOLS.containsKey(desiredCharacter);

                    char baseChar = SHIFTED_SYMBOLS.getOrDefault(
                            desiredCharacter, Character.toLowerCase(desiredCharacter));

                    int keyCode = KeyEvent.getExtendedKeyCodeForChar(baseChar);

                    if (needsShift) robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);
                    if (needsShift) robot.keyRelease(KeyEvent.VK_SHIFT);
                }
            }
            catch (IllegalArgumentException e) {
                System.out.println(desiredCharacter);
                e.printStackTrace();
            }
            robot.waitForIdle();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        }
    }

    RobotKeyStroke[] sequence;
    String string;

    public RobotTyper(Robot robot, String input) {
        this.string = input;
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
