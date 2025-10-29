package edu.temple.UMB;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.ArrayList;

public class KeyCapture implements NativeKeyListener {

    public ArrayList<Integer> keyEvents = new ArrayList<Integer>();
    // TODO: Actually record input
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        keyEvents.add(e.getKeyCode());
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) { }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }

    public static void main(String[] args) {
        // disable logging
        java.util.logging.Logger logger =
                java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(java.util.logging.Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new KeyCapture());
    }
}
