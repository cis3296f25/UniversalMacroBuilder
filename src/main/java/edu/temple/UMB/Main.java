package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static java.lang.System.exit;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    // only one of these strings and files will be populated per run. we could use an enum to help with telling which mode we're in
    // but we can also just be careful with null checks
    public static String out_file_str = null;
    public static String in_file_str = null;
   
    public static String stopKey = "ESCAPE";
    public static boolean listMacrosFlag = false;
    private static final String MACRO_FOLDER_NAME = "macros";

    public static void main(String[] args) throws InterruptedException, AWTException, Exception {
        logger.info("Starting UMB.");
        //check if macro dir exists
        File macroDir = new File(MACRO_FOLDER_NAME);
        if (!macroDir.exists()){
            if (macroDir.mkdir()){
                System.out.println("[INFO] Created macros directory: " + macroDir.getAbsolutePath());
                logger.info("Created macros directory: {}", macroDir.getAbsolutePath());
            }
            else {
                System.out.println("[ERROR] Could not create macros directory!");
                logger.fatal("Could not create macros directory!");
                exit(1);
            }
        } else {
            logger.debug("Macro folder already exists.");
        }
        String argsRes = argChecks(args);
        if (argsRes != null) {
            System.out.println("java -jar UniversalMacroBuilder.jar (-output <out_path> | -input <in_path>) [-stopkey <stopkey>] [-l]");
            throw new IllegalArgumentException(argsRes);
        }

        if(listMacrosFlag){
            //list existing macros to terminal
            listMacros(macroDir);
            exit(0);
        }

        // call either the capture or replayer classes
        if (in_file_str != null) {
            File inFile = new File(macroDir, in_file_str);
            if (!inFile.exists()){
                logger.fatal("File not found: {}", in_file_str);
                System.out.println("[ERROR] Macro file not found: " + inFile.getAbsolutePath());
                listMacros(macroDir);

                //user can select which macro
                File selected = promptUserForMacro(macroDir);
                if(selected ==null){
                    System.out.println("No valid selection. Exiting.");
                    exit(1);
                }
                inFile = selected;
            }

            logger.info("Replaying macro: {}", inFile.getAbsolutePath());
            System.out.println("[INFO] Replaying macro: " + inFile.getName());
            Replayer replayer = new Replayer(inFile.getAbsolutePath());
        } else if (out_file_str != null) {
            File outFile = new File(macroDir, out_file_str);

            //ask if user wants to overwrite
            if (outFile.exists()){
                logger.info("File exists: {}", outFile.getAbsolutePath());
                System.out.println("[WARNING] File already exists: " + outFile.getName());
                System.out.println("Overwrite? (y/n): ");
                int response = System.in.read();
                if (response != 'y' && response != 'Y') {
                    logger.fatal("User disallowed overwriting of: {}", outFile.getAbsolutePath());
                    System.out.println("Recording cancelled.");
                    exit(0);
                }
                logger.info("User approved overwriting of: {}", outFile.getAbsolutePath());
            }
            new FileWriter(outFile, false).close();
            logger.info("Recording to file: {}", outFile.getAbsolutePath());
            System.out.println("[INFO] Recording macro: " + outFile.getName());
            Recorder recorder = new Recorder(outFile, stopKey);
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
            for (int i = 0; i < files.length; i++) {
                System.out.println((i + 1) + ") " + files[i].getName());
            }
        }
        System.out.println("==========================\n");
    }

    //prompt for getting the user to select a macro
    private static File promptUserForMacro(File macroDir) {
        File[] files = macroDir.listFiles();
        if (files == null || files.length == 0) return null;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter macro number to replay: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input.");
            return null;
        }
        int choice = scanner.nextInt();
        return files[choice - 1];
    }

    /**
     * Checks args given to ensure the following:
     * <ul>
     * <li> Either input or output is given.</li>
     * <li> After either input or output must be a filename.
     * <li> Both input and output may not be given.</li>
     * <li> No unknown arguments are given.</li>
     * </ul>
     * @param args: The command line arguments to check for validity.
     * @return Null if all checks are successful, else an error message for the caller to throw with IllegalArgumentException.
     */
    public static String argChecks(String[] args) {
        logger.trace("Arguments provided: {}", String.join(" ", args));
        if (args.length == 0) {
            logger.fatal("No arguments given!");
            return "ERROR: No arguments given!";
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-output" -> {
                    if (out_file_str != null || in_file_str != null) {
                        logger.fatal("Output or input file already specified!");
                        return "ERROR: Output or input file already specified!";
                    } else if (i + 1 < args.length) {
                        out_file_str = args[i + 1];
                        i++;
                    } else {
                        logger.fatal("No output file provided!");
                        return "ERROR: Argument -output requires an argument!";
                    }
                }
                case "-input" -> {
                    if (out_file_str != null || in_file_str != null) {
                        logger.fatal("Output or input file already specified!");
                        return "ERROR: Output or input file already specified!";
                    } else if (i + 1 < args.length) {
                        in_file_str = args[i + 1];
                        i++;
                    } else {
                        logger.fatal("No input file provided!");
                        return "ERROR: Argument -input requires an argument!";
                    }
                }
                case "-stopkey" -> {
                    if (i + 1 < args.length) {
                        stopKey = args[i + 1];
                        i++;
                    } else {
                        logger.fatal("No stopkey provided!");
                        return "ERROR: Argument -stopkey requires an argument!";
                    }
                }
                case "-l" -> {
                    if (out_file_str != null || in_file_str != null) {
                        logger.fatal("-l used with input or output!");
                        return "ERROR: -l should not be used with input or output!";
                    }
                    listMacrosFlag = true;
                }
                default -> {
                    logger.fatal("Unknown argument: " + args[i]);
                    return "ERROR: Unknown argument: " + args[i];
                }
            }
        }
        // sanity check but included for future cases
        if (!listMacrosFlag && out_file_str == null && in_file_str == null) {
            return "ERROR: Either -input or -output must be specified!";
        }

        return null;
    }
}
