package edu.temple.UMB;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CrossPlatformShutdown {
    private static final Logger logger = LogManager.getLogger(CrossPlatformShutdown.class);
    private static final AtomicBoolean called = new AtomicBoolean(false);

    /**
     * Register a cleanup callback that will be invoked on normal JVM shutdown (Linux/Unix)
     * and also when Windows console control events happen (Ctrl+C, Close, Break, etc).
     *
     * This method is safe to call on any platform. On non-Windows platforms it will
     * simply install the JVM shutdown hook and skip the Windows handler.
     *
     * @param cleanup the Runnable to run during shutdown
     */
    public static void install(Runnable cleanup) {
        // Ensure cleanup only runs once
        Runnable once = () -> {
            if (called.compareAndSet(false, true)) {
                try {
                    cleanup.run();
                } catch (Throwable t) {
                    logger.error("Exception during shutdown cleanup", t);
                }
            }
        };

        // JVM shutdown hook (cross-platform; works reliably on POSIX)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutdown hook triggered.");
            once.run();
        }));

        // Try installing the Windows console handler only on Windows
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            logger.debug("Not Windows; skipping Windows console handler installation.");
            return;
        }

        try {
            boolean ok = Kernel32.INSTANCE.SetConsoleCtrlHandler(new Kernel32.HandlerRoutine() {
                @Override
                public boolean callback(int ctrlType) {
                    logger.info("Windows console control event: {}", ctrlType);
                    once.run();
                    // returning true indicates we handled the event
                    return true;
                }
            }, true);

            if (!ok) {
                logger.warn("SetConsoleCtrlHandler returned false.");
            } else {
                logger.debug("Windows console handler installed.");
            }
        } catch (Throwable t) {
            // Catch NoClassDefFoundError, UnsatisfiedLinkError, SecurityException, etc.
            logger.warn("Failed to install Windows console handler (JNA may be missing).", t);
        }
    }

    // JNA mapping for kernel32. Only used when running on Windows.
    private interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        // Handler: return true if handled (prevents other handlers / default)
        interface HandlerRoutine extends com.sun.jna.Callback {
            boolean callback(int ctrlType);
        }

        boolean SetConsoleCtrlHandler(HandlerRoutine handler, boolean add);
    }
}
