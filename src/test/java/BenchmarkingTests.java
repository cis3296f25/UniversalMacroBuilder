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
756 PRESSED 30
798 PRESSED 31
867 PRESSED 32
949 RELEASED 30
957 PRESSED 33
973 RELEASED 31
1009 RELEASED 32
1057 RELEASED 33
1250 PRESSED 1
1325 RELEASED 1
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