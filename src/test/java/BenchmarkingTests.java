import edu.temple.UMB.KeyReplayer;
import edu.temple.UMB.Recorder;
import edu.temple.UMB.Replayer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

// allows before all annotation to be non-static, see https://docs.junit.org/current/api/org.junit.jupiter.api/org/junit/jupiter/api/TestInstance.Lifecycle.html
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BenchmarkingTests {
    // TODO: test fast keyboard inputs (and mouse!)
    private final String predeterminedEvents = """
START KEY EVENTS
705 PRESSED 30
815 RELEASED 30
927 PRESSED 31
1035 RELEASED 31
1420 PRESSED 32
1555 RELEASED 32
1748 PRESSED 33
1854 RELEASED 33
2935 PRESSED 1
3035 RELEASED 1
END KEY EVENTS
START MOUSE EVENTS
END MOUSE EVENTS
EOF""";
    private String tmpDirPath = "./tmp_testing_dir";
    private File tmpDirFile = null;
    private String prederminedEventsPath = tmpDirPath + "/predeterminedEvents.txt";
    private File predeterminedEventsFile = null;

    private String tmpRecorderOutPath = tmpDirPath + "/tmpRecorderOutput.txt";

    // the goal of this class is to have tests that can provide quantitative performance benchmarking.
    // we will make a replayer class to schedule some predertimed invents at specified times.
    // then we can make a recorder class to record those events and compare the timestamps.

    @BeforeAll
    void init() throws RuntimeException, IOException {
        // before all the tests, setup the file it will read from.
        // check if the tmp dir exists, create if not
        tmpDirFile = new File(tmpDirPath);
        if (!tmpDirFile.exists()) {
            if (!tmpDirFile.mkdir()) {
                throw new RuntimeException("Unable to create tmp directory");
            }
        }
        // check if file exists, create if not
        predeterminedEventsFile = new File(prederminedEventsPath);
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
        // delete our tmp file and dir
        // Files.deleteIfExists(Paths.get(predeterminedEventsFile.getAbsolutePath()));
        // Files.deleteIfExists(Paths.get(tmpDirFile.getAbsolutePath()));
    }

    @Test
    void benchmarkRecording() throws InterruptedException {
        File out = new File(tmpRecorderOutPath);
        // so now we need to set up a replayer, feed it the predetermined events, then set up a recorder.
        // we will need to have them both start at exactly the same time, or the tests could be off. maybe use scheduled executor?
        // i will need to modify the key replayer. i need a way to prepare it, then start it at the same time like so
        Replayer replayer = new Replayer(predeterminedEventsFile.getAbsolutePath());
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

        //asdf
    }
}