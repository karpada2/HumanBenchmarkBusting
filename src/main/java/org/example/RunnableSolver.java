package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RunnableSolver {
    Robot robot;
    Utils utils;
    AtomicBoolean shouldHalt = new AtomicBoolean(false);
    AtomicBoolean shouldStart = new AtomicBoolean(false);

    public void play() throws Exception {
        // Quiet down JNativeHook's verbose logging
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        GlobalScreen.registerNativeHook();

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
                    shouldHalt.set(true);
                }
            }
        });


        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
                    shouldStart.set(true);
                }
            }
        });

        AudioInputStream ais = AudioSystem.getAudioInputStream(new File("src/main/resources/ding.wav"));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(2));


        innerInitialize();
        initialize();

        clip.start();

        while (!shouldStart.get()) {
            Thread.sleep(10);
        }

        start();

        clip.setFramePosition(0);
        clip.start();

        while (!shouldHalt.get()) {
            run();
        }

        GlobalScreen.unregisterNativeHook();
        clip.setFramePosition(0);
        clip.start();

        int a = 0;

        while (clip.getMicrosecondPosition() < clip.getMicrosecondLength()) {
            a++;
            Thread.sleep(10);
        }
        System.out.println(a);
        System.out.println("ended");
    }

    public void halt() {
        shouldHalt.set(true);
    }


    private void innerInitialize() throws Exception {
        robot = new Robot();
        utils = new Utils(robot);
    }


    public abstract void initialize();

    public abstract void start();

    public abstract void run();
}
