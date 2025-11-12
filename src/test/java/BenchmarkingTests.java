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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// allows before all annotation to be non-static, see https://docs.junit.org/current/api/org.junit.jupiter.api/org/junit/jupiter/api/TestInstance.Lifecycle.html
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BenchmarkingTests {
    // TODO: test fast keyboard inputs (and mouse!)
    private final String predeterminedEvents = "START KEY EVENTS\n500 PRESSED 30\n1000 RELEASED 30\n1500 PRESSED 31\n2000 RELEASED 31\n2500 PRESSED 32\n3000 RELEASED 32\n3500 PRESSED 33\n4000 RELEASED 33\n4250 PRESSED 1\n4300 RELEASED 1\nEND KEY EVENTS\nSTART MOUSE EVENTS\nEND MOUSE EVENTS\nEOF";
    private String prederminedEventsPath = "./predeterminedEvents.txt";
    private File predeterminedEventsFile = null;
    private String tmpDirPath = "./tmp_testing_dir";
    private File tmpDirFile = null;

    private String tmpRecorderOutPath = "tmpRecorderOutput.txt";
    private File tmpRecorderFile = null;

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
        Files.deleteIfExists(Paths.get(predeterminedEventsFile.getAbsolutePath()));
        Files.deleteIfExists(Paths.get(tmpDirFile.getAbsolutePath()));
    }

    @Test
    void benchmarkRecording() {
        System.out.println("Look Ma, I'm runnable!");
        File out = new File(tmpRecorderOutPath);
        // so now we need to set up a replayer, feed it the predetermined events, then set up a recorder.
        // we will need to have them both start at exactly the same time, or the tests could be off. maybe use scheduled executor?
        // i will need to modify the key replayer. i need a way to prepare it, then start it at the same time like so
        Replayer replayer = new Replayer(predeterminedEventsFile.getAbsolutePath());
        Recorder recorder = new Recorder(out, "ESCAPE");
        ScheduledExecutorService syncExec = Executors.newSingleThreadScheduledExecutor();
        syncExec.schedule(() -> {
            replayer.start();
            recorder.start();
        }, 1, TimeUnit.SECONDS);
        // then we can wait for execution and assert things and stuff
    }
}
