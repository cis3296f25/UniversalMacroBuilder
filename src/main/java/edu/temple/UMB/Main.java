package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;

import static java.lang.System.exit;

public class Main {
    // only one of these strings and files will be populated per run. we could use an enum to help with telling which mode we're in
    // but we can also just be careful with null checks
    public static String out_file_str = null;
    public static String in_file_str = null;

    private static final String MACRO_FOLDER_NAME = "macros";

    public static void main(String[] args) throws InterruptedException, AWTException, Exception {
        //check if macro dir exists
        File macroDir = new File(MACRO_FOLDER_NAME);
        if (!macroDir.exists()){
            if (macroDir.mkdir()){
                System.out.println("[INFO] Created macros directory: " + macroDir.getAbsolutePath());
            }
            else {
                System.out.println("[ERROR] Could not create macros directory!");
                exit(1);
            }
        }
        //list existing macros to terminal
        listMacros(macroDir);
        
        String argsRes = argChecks(args);
        if (argsRes != null) {
            System.out.println("Usage: UniversalMacroBuilder.jar (-output <out_path> | -input <in_path>)");
            throw new IllegalArgumentException(argsRes);
        }

        // call either the capture or replayer classes
        if (in_file_str != null) {
            File inFile = new File(macroDir, in_file_str);
            if (!inFile.exists()){
                System.out.println("[ERROR] Macro file not found: " + inFile.getAbsolutePath());
                exit(1);
            }

            System.out.println("[INFO] Replaying macro: " + inFile.getName());
            Replayer replayer = new Replayer(inFile.getAbsolutePath());
        } else if (out_file_str != null) {
            File outFile = new File(macroDir, out_file_str);

            //ask if user wants to overwrite
            if (outFile.exists()){
                System.out.println("[WARNING] File already exists: " + outFile.getName());
                System.out.println("Overwrite? (y/n): ");
                int response = System.in.read();
                if (response != 'y' && response != 'Y') {
                    System.out.println("Recording cancelled.");
                    exit(0);
                }
            }

            System.out.println("[INFO] Recording macro: " + outFile.getName());
            Recorder recorder = new Recorder(outFile);
            recorder.start();
        } else {
            System.out.println("How the hell did you end up here?");
            exit(1);
        }

    }

    //list macros
    private static void listMacros(File macroDir){
        File[] files = macroDir.listFiles();
        System.out.println("====== Saved Macros ======");
        if (files == null || files.length == 0) {
            System.out.println("No macros recorded yet.");
        } 
        else {
            for (File f : files) {
                System.out.println("- " + f.getName());
            }
        }
        System.out.println("==========================\n");
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
