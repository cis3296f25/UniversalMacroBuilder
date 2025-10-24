import edu.temple.tuo18747.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainTests {

    @BeforeEach
    public void setup() {
        Main.in_file_str = null;
        Main.out_file_str = null;
    }
    @Test
    public void testNoArgs() {
        String[] args = {};
        assertEquals("ERROR: No arguments given!", Main.argChecks(args));
    }

    @Test
    public void testNoOutputFile() {
        String[] args = {"-output"};
        assertEquals("ERROR: Argument -output requires an argument!", Main.argChecks(args));
    }

    @Test
    public void testNoInputFile() {
        String[] args = {"-input"};
        assertEquals("ERROR: Argument -input requires an argument!", Main.argChecks(args));
    }

    @Test
    public void badArgs() {
        String[] args = {"-invalid"};
        assertEquals("ERROR: Unknown argument: -invalid", Main.argChecks(args));
    }

    @Test
    public void multipleInputs() {
        String[] args0 = {"-input", "infile", "-input"};
        assertEquals("ERROR: Output or input file already specified!", Main.argChecks(args0));
        String[] args1 = {"-input", "infile", "-input", "infile1"};
        assertEquals("ERROR: Output or input file already specified!",Main.argChecks(args1));
    }

    @Test
    public void multipleOutputs() {
        String[] args0 = {"-output", "outfile", "-output"};
        assertEquals("ERROR: Output or input file already specified!", Main.argChecks(args0));
        String[] args1 = {"-output", "outfile", "-output", "outfile1"};
        assertEquals("ERROR: Output or input file already specified!",Main.argChecks(args1));
    }

    @Test
    public void multipleInputsAndOutput() {
        String[] args0 = {"-input", "infile", "-output", "outfile1"};
        assertEquals("ERROR: Output or input file already specified!", Main.argChecks(args0));
        String[] args1 = {"-input", "infile", "-output", "outfile1", "-input", "infile2"};
        assertEquals("ERROR: Output or input file already specified!", Main.argChecks(args1));
    }

    @Test
    public void correctInput() {
        String[] args = {"-input", "infile"};
        assertNull(Main.argChecks(args));
    }

    @Test
    public void correctOutput() {
        String[] args = {"-output", "outfile"};
        assertNull(Main.argChecks(args));
    }
}
