package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;

public class PositionChecking extends RunnableSolver {
    public static void main(String[] args){
        new PositionChecking().play();
    }

    @Override
    public void initialize() {
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
                    System.out.println("position: " + MouseInfo.getPointerInfo().getLocation());
                    System.out.println("color: " + utils.getPixelColorAtCursor());
                    System.out.println();
                }
            }
        });
    }

    @Override
    public void run() {

        while (isActive()) {

        }
    }
}
