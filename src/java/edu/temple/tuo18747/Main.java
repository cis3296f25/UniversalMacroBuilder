package edu.temple.tuo18747;

import java.awt.*;
import java.io.File;

import static java.lang.System.exit;

public class Main {
    // only one of these strings and files will be populated per run. we could use an enum to help with telling which mode we're in
    // but we can also just be careful with null checks
    public static String out_file_str = null;
    public static String in_file_str = null;
    File out_file = null;
    File in_file = null;

    public static void main(String[] args) throws InterruptedException, AWTException {
        String argsRes = argChecks(args);
        if (argsRes != null) {
            System.out.println("Usage: UniversalMacroBuilder.jar (-output <out_path> | -input <in_path>)");
            throw new IllegalArgumentException(argsRes);
        }

        // since we are just performing proof of feasibility we will simply call a class that captures some input
        // or call a class to replicate some input
        if (in_file_str != null) {
            DemoKeyCapture.main(null);
        } else if (out_file_str != null) {
            DemoKeyReplayer.main(null);
        } else {
            System.out.println("How the hell did you end up here?");
            exit(1);
        }

    }

    /**
     * Checks args given to ensure the following:
     * <ul>
     * <li> Either input or output is given.</li>
     * <li> After either input or output must be a filename. TODO: this is not checked for validity/permissions yet.</li>
     * <li> Both input and output may not be given.</li>
     * <li> No unknown arguments are given.</li>
     * </ul>
     * @param args: The command line arguments to check for validity.
     * @return Null if all checks are successful, else an error message for the caller to throw with IllegalArgumentException.
     */
    public static String argChecks(String[] args) {
        if (args.length == 0) {
            return "ERROR: No arguments given!";
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-output")) {
                if (out_file_str != null || in_file_str != null) {
                    return "ERROR: Output or input file already specified!";
                } else if (i+1 < args.length) {
                    out_file_str = args[i+1];
                    i++;
                } else {
                    return "ERROR: Argument -output requires an argument!";
                }
            } else if (args[i].equals("-input")) {
                if (out_file_str != null || in_file_str != null) {
                    return "ERROR: Output or input file already specified!";
                } else if (i+1 < args.length ) {
                    in_file_str = args[i+1];
                    i++;
                } else  {
                    return "ERROR: Argument -input requires an argument!";
                }
            } else {
                return "ERROR: Unknown argument: " + args[i];
            }
        }

        // sanity check but included for future cases
        if (out_file_str == null && in_file_str == null) {
            return "ERROR: Either -input or -output must be specified!";
        }

        // TODO: verify file paths
        return null;
    }
}
