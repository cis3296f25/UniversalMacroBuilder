import java.lang.annotation.Native;
import java.util.EventListener;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class DemoKeyCapture implements NativeKeyListener {
    private static EventRecorder recorder;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        System.out.println("Key Pressed: " + keyText);
        recorder.recordKeyEvent(keyText);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) { }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }

    public static void main(String[] args) throws Exception {
        // disable logging
        java.util.logging.Logger logger =
                java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(java.util.logging.Level.OFF);
        logger.setUseParentHandlers(false);

        //create output file w/ inputted name or under recorded_macro.txt
        //creates recorder
        String filePath = (Main.in_file_str != null) ? Main.in_file_str : "recorded_macro.txt";
        //String filePath = "recorded_macro.txt";
        recorder = new EventRecorder(filePath);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new DemoKeyCapture());
    }
}
