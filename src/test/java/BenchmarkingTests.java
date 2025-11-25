import edu.temple.UMB.Loader;
import edu.temple.UMB.Recorder;
import edu.temple.UMB.Replayer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.*;


/**
 * Benchmarks end-to-end record and replay timing characteristics.
 */
// allows before all annotation to be non-static, see https://docs.junit.org/current/api/org.junit.jupiter.api/org/junit/jupiter/api/TestInstance.Lifecycle.html
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BenchmarkingTests {
    boolean benchmarking = true;
    // TODO: test fast keyboard inputs (and mouse!)
    private final String predeterminedEvents = """
START KEY EVENTS
819 PRESSED 35
941 RELEASED 35
942 PRESSED 18
1052 RELEASED 18
1108 PRESSED 38
1198 RELEASED 38
1292 PRESSED 38
1376 RELEASED 38
1728 PRESSED 24
1812 RELEASED 24
1836 PRESSED 57
1938 RELEASED 57
2026 PRESSED 20
2090 PRESSED 35
2122 RELEASED 20
2184 PRESSED 18
2196 RELEASED 35
2262 PRESSED 19
2272 RELEASED 18
2341 RELEASED 19
2372 PRESSED 18
2468 PRESSED 57
2488 RELEASED 18
2575 RELEASED 57
2684 PRESSED 38
2787 RELEASED 38
2897 PRESSED 24
2985 RELEASED 24
3091 PRESSED 49
3191 PRESSED 34
3209 RELEASED 49
3217 PRESSED 57
3281 RELEASED 34
3319 RELEASED 57
3417 PRESSED 20
3495 PRESSED 18
3517 RELEASED 20
3591 RELEASED 18
3675 PRESSED 31
3789 RELEASED 31
3845 PRESSED 20
3929 RELEASED 20
4928 PRESSED 52
5016 RELEASED 52
5290 PRESSED 57
5390 RELEASED 57
5843 PRESSED 17
5947 RELEASED 17
5951 PRESSED 35
6059 RELEASED 35
6163 PRESSED 24
6267 RELEASED 24
6297 PRESSED 57
6389 RELEASED 57
6441 PRESSED 22
6545 RELEASED 22
6589 PRESSED 25
6671 PRESSED 57
6701 RELEASED 25
6757 RELEASED 57
6858 PRESSED 20
6949 PRESSED 18
6975 RELEASED 20
7067 RELEASED 18
7148 PRESSED 31
7274 RELEASED 31
7304 PRESSED 20
7410 RELEASED 20
7795 PRESSED 23
7898 RELEASED 23
7918 PRESSED 49
8026 PRESSED 34
8046 RELEASED 49
8082 PRESSED 57
8120 RELEASED 34
8182 RELEASED 57
8234 PRESSED 20
8302 PRESSED 35
8326 RELEASED 20
8422 PRESSED 18
8423 RELEASED 35
8524 RELEASED 18
8526 PRESSED 21
8630 RELEASED 21
8656 PRESSED 57
8741 RELEASED 57
8855 PRESSED 38
8955 RELEASED 38
9087 PRESSED 24
9143 RELEASED 24
9233 PRESSED 49
9335 RELEASED 49
9355 PRESSED 34
9413 PRESSED 57
9443 RELEASED 34
9519 RELEASED 57
9589 PRESSED 50
9625 PRESSED 30
9695 RELEASED 50
9731 RELEASED 30
9811 PRESSED 46
9929 RELEASED 46
10049 PRESSED 19
10158 RELEASED 19
10226 PRESSED 24
10342 RELEASED 24
11038 PRESSED 31
11142 RELEASED 31
11200 PRESSED 1
11250 RELEASED 1
END KEY EVENTS
START MOUSE EVENTS
END MOUSE EVENTS
EOF
""";
    private String tmpDirPath = "tmp_testing_dir";
    private File tmpDirFile = null;
    private String predeterminedEventsPath = tmpDirPath + "/predeterminedEvents.txt";
    private File predeterminedEventsFile = null;

    private String tmpRecorderOutPath = tmpDirPath + "/tmpRecorderOutput.txt";


    // with this we can guarantee under 500ms mean absolute difference (scheduling overhead) and under 5ms mean absolute gap (the correctness of inter-event spacing)
    Long MAD_CUTOFF = 500L;
    Long MAG_CUTOFF = 5L;

    @BeforeAll
    void init() throws RuntimeException, IOException {
        if (!benchmarking) {
            return;
        }
        // before all the tests, setup the file it will read from.
        // check if the tmp dir exists, create if not
        tmpDirFile = new File(tmpDirPath);
        if (!tmpDirFile.exists()) {
            if (!tmpDirFile.mkdirs()) {
                throw new RuntimeException("Unable to create tmp directory");
            }
        }
        // check if file exists, create if not
        predeterminedEventsFile = new File(predeterminedEventsPath);
        if (!predeterminedEventsFile.exists()) {
            if (!predeterminedEventsFile.createNewFile()) {
                throw new RuntimeException("Unable to create predetermined events file");
            }
        }
        // write to file. empty file first. this way if the predEvents gets update we do not have to worry about changes being reflected
        Files.writeString(Paths.get(predeterminedEventsFile.getAbsolutePath()), predeterminedEvents);
    }

    @AfterAll
    void cleanup() throws IOException {
        if (!benchmarking) {
            return;
        }
        // delete our tmp file and dir
        Files.deleteIfExists(Paths.get(tmpRecorderOutPath));
        Files.deleteIfExists(predeterminedEventsFile.toPath());
        Files.deleteIfExists(tmpDirFile.toPath());
    }

    @Test
    void benchmark() throws InterruptedException {
        if (!benchmarking) {
            System.out.println("WARNING: Benchmarking is not enabled!");
            return;
        }
        File out = new File(tmpRecorderOutPath);
        // so now we need to set up a replayer, feed it the predetermined events, then set up a recorder.
        // latch allows us to countdown to execution, getting pretty perfect execution times
        Replayer replayer = new Replayer(predeterminedEventsFile.getAbsolutePath(), 1);
        Recorder recorder = new Recorder(out, "ESCAPE");
        ExecutorService exec = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        exec.submit(() -> {
            try {
                latch.await();  // both wait for signal
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            replayer.start();
        });

        exec.submit(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            recorder.start();
        });

        // synchronize start
        latch.countDown();

        // wait long enough for both to finish
        exec.shutdown();
        exec.awaitTermination(15, TimeUnit.SECONDS);

        // now we can parse our out file and get some stats from it.
        // there will be some difference in overall timestamps (probably around 50-200ms) due to differences in startup overhead (despite our best efforts to reduce this).
        // that's an important stat, but we really care about variance, or the average gap betweens two events.
        // we can actually just use our loader classes to load the out file
        Loader l =  new Loader(out);
        LinkedHashMap<Long, String> recordedEvents;
        try {
            recordedEvents = l.loadJNativeEventsFromFile();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ArrayList<Long> recordedTS = new ArrayList<>(recordedEvents.keySet());

        // we could just manually parse through the predetermined events string or we can have loader do it for us.
        // need to always remove the last two events of predTS as theyre for the exit escape key and wont get recorded
        l = new Loader(predeterminedEventsFile);
        LinkedHashMap<Long, String> predEvents;
        try {
            predEvents = l.loadJNativeEventsFromFile();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ArrayList<Long> predTS = new ArrayList<>(predEvents.keySet());
        predTS.removeLast();
        predTS.removeLast();

        System.out.println("Predetermined events timestamps: " + predTS);
        System.out.println("Recorded events timestamps: " + recordedTS);

        // and now we can compare
        // first stat we want is the mean absolute difference between each. this is most likely overhead, and shouldnt matter too much
        // we will also want to look at the mean absolute gap to observe that the inter-event recording is being preserved (which is much more important than the MAD anyways.
        long madRunning = 0L;
        long magRunning = 0L;
        for (int i = 0; i < recordedTS.size(); i++) {
            madRunning += Math.abs(predTS.get(i) - recordedTS.get(i));
            if (i == 0) { continue; }
            long predGap = predTS.get(i) - predTS.get(i - 1);
            long recGap  = recordedTS.get(i) - recordedTS.get(i - 1);
            magRunning += Math.abs(predGap - recGap);
        }
        long mad = madRunning / recordedTS.size();
        long mag = magRunning / (recordedTS.size() - 1);
        System.out.println("Mean Absolute Difference: " + mad);
        System.out.println("Mean Absolute Gap: " + mag);

        // some hard limits on timings that we should always pass
        assertTrue(mad < MAD_CUTOFF);
        assertTrue(mag < MAG_CUTOFF);

        // TODO: if were feeling nice, make a custom macro here and feed it to a new replayer that just holds backspace for like a second to get rid of stuff typed above
    }
}