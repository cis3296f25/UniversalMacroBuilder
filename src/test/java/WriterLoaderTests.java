import edu.temple.UMB.Event;
import edu.temple.UMB.Writer;
import edu.temple.UMB.Loader;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for writing event files and loading them back into memory.
 */
class WriterLoaderTests {


    // a small helper class to test with
    static class MockEvent extends Event {
        private final long timestamp;
        private final String action;

        MockEvent(long timestamp, String action) {
            super(timestamp);
            this.timestamp = timestamp;
            this.action = action;
        }

        @Override
        public String toString() {
            return timestamp + " " + action;
        }
    }

    private Path tempDir;

    @BeforeEach
    // each time we will make a tmp dir to test with
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("umb_test_");
    }

    @AfterEach
    // after each we will delete everything in said temp dir
    void cleanup() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    // here we test that the writer writes as we expect it to
    @Test
    void testWriterWritesKeyEventsFile() throws IOException {
        File testFile = tempDir.resolve("key_events.txt").toFile();
        Writer writer = new Writer(Writer.Type.KEY);

        // list of mock events we'll test with
        List<MockEvent> events = List.of(
                new MockEvent(100L, "PRESS_A"),
                new MockEvent(200L, "RELEASE_A")
        );

        writer.writeToFile(testFile, events);

        // read file contents
        List<String> lines = Files.readAllLines(testFile.toPath());

        // ensure everything checks out
        assertEquals("START KEY EVENTS", lines.get(0));
        assertEquals("100 PRESS_A", lines.get(1));
        assertEquals("200 RELEASE_A", lines.get(2));
        assertEquals("END KEY EVENTS", lines.get(3));
    }

    // test that the writer works for mouse events too
    @Test
    void testWriterWritesMouseEventsFile() throws IOException {
        File testFile = tempDir.resolve("mouse_events.txt").toFile();
        Writer writer = new Writer(Writer.Type.MOUSE);

        List<MockEvent> events = List.of(new MockEvent(12345L, "MOVE_100_200"));
        writer.writeToFile(testFile, events);

        List<String> lines = Files.readAllLines(testFile.toPath());

        assertEquals("START MOUSE EVENTS", lines.get(0));
        assertEquals("12345 MOVE_100_200", lines.get(1));
        assertEquals("END MOUSE EVENTS", lines.get(2));
        assertEquals("EOF", lines.get(3));
    }

    // test that loader loads as we expect it to
    @Test
    void testLoaderParsesValidKeyEventsFile() throws IOException {
        File input = tempDir.resolve("input.txt").toFile();

        // Create mock input file
        Files.write(input.toPath(), List.of(
                "START KEY EVENTS",
                "100 PRESS A",
                "200 RELEASE A",
                "END KEY EVENTS",
                "EOF"
        ));

        Loader loader = new Loader(input);
        LinkedHashMap<Long, String> map = loader.loadJNativeEventsFromFile();

        assertEquals(2, map.size());
        assertEquals("PRESS_A", map.get(100L));
        assertEquals("RELEASE_A", map.get(200L));
    }

    // test that nothing blows up in our face if it gets passed an empty file
    @Test
    void testLoaderHandlesEmptyFileGracefully() throws IOException {
        File input = tempDir.resolve("empty.txt").toFile();
        Files.write(input.toPath(), List.of());

        Loader loader = new Loader(input);
        LinkedHashMap<Long, String> map = loader.loadJNativeEventsFromFile();

        // Expect no entries, no exception
        assertTrue(map.isEmpty());
    }

    // test that loader doesnt read past the end key events message
    @Test
    void testLoaderStopsAtEndMarker() throws IOException {
        File input = tempDir.resolve("partial.txt").toFile();

        Files.write(input.toPath(), List.of(
                "START KEY EVENTS",
                "111 PRESS A",
                "END KEY EVENTS",
                "999 RELEASE A" // should not be read
        ));

        Loader loader = new Loader(input);
        LinkedHashMap<Long, String> map = loader.loadJNativeEventsFromFile();

        assertEquals(1, map.size());
        assertEquals("PRESS_A", map.get(111L));
    }

    // TODO: test that Loader loads mouse events correctly (after mouse recording PR is finished)
}
