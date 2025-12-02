package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static java.lang.System.exit;

/**
 * Entry point for UniversalMacroBuilder. Parses command-line arguments and starts recording or replaying macros.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    // only one of these strings and files will be populated per run. we could use an enum to help with telling which mode we're in
    // but we can also just be careful with null checks
    public static String out_file_str = null;
    public static String in_file_str = null;
   
    public static String stopKey = "ESCAPE";
    public static boolean listMacrosFlag = false;
    private static final String MACRO_FOLDER_NAME = "macros";
    public static Integer repeatCount = null;
    private static final Scanner SC = new Scanner(System.in);

    /**
     * Application entry point.
     */
    public static void main(String[] args) throws Exception {
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
        if (argsRes != null && !argsRes.equals("ERROR: No arguments given!")) {
            System.out.println(
                "java -jar UniversalMacroBuilder.jar " +
                "(-output <out_path> | -input <in_path>) " +
                "[-stopkey <stopkey>] " +
                "[-repeat [count]] " +
                "[-l]"
            );
            throw new IllegalArgumentException(argsRes);
        } else if (argsRes != null) {
            // enter interactive mode. it will return an array of strings, which can conveniently just be passed to a new instance of main
            String[] constructedArgs = interactiveMode(macroDir);
            // main(constructedArgs); TODO: uncomment this line when interactive mode builds command correctly
            exit(0);
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
            
            // DEFAULT: 1 replay
            int rc = (repeatCount == null ? 1 : repeatCount);

            // Normal repeat via Replayer handling it internally
            new Replayer(inFile.getAbsolutePath(), rc).start();

        } else if (out_file_str != null) {
            File outFile = new File(macroDir, out_file_str);

            //ask if user wants to overwrite
            if (outFile.exists()){
                logger.info("File exists: {}", outFile.getAbsolutePath());
                System.out.println("[WARNING] File already exists: " + outFile.getName());
                System.out.println("Overwrite? (y/n): ");
                int response = SC.nextInt();
                if (response != 'y' && response != 'Y') {
                    logger.fatal("User disallowed overwriting of: {}", outFile.getAbsolutePath());
                    System.out.println("Recording cancelled.");
                    SC.close();
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
            SC.close();
            exit(1);
        }

        // done with program
        SC.close();
        exit(0);
    }

    private static String[] interactiveMode(File macroDir) throws IOException {
        // TODO: this will prompt users for info to build the command.
        ArrayList<String> new_args = new ArrayList<>();
        // it will start by asking whether the user wants to record or replay
        // TODO: add a macro manipulation mode for renaming and deleting files
        int selected_action = getAndValidateIntInput("What would you like to do?\n\t1: Record a macro.\n\t2: Replay a macro.\nSelect action: ", new int[]{1, 2});
        if  (selected_action == 1) {
            new_args.add("-output");
            String new_macro_name = getNewMacroName("Name of the macro to be recorded: ");
            System.out.println(new_macro_name);
            File outFile = new File(macroDir, new_macro_name);

            //ask if user wants to overwrite
            if (outFile.exists()){
                logger.info("File exists: {}", outFile.getAbsolutePath());
                System.out.println("[WARNING] File already exists: " + outFile.getName());
                Boolean response = yesOrNoPrompt("Overwrite? (y/n)");
                if (!response) {
                    logger.fatal("User disallowed overwriting of: {}", outFile.getAbsolutePath());
                    System.out.println("Recording cancelled.");
                    SC.close();
                    exit(0);
                }
                logger.info("User approved overwriting of: {}", outFile.getAbsolutePath());
            }
            new FileWriter(outFile, false).close(); 
            logger.info("Recording to file: {}", outFile.getAbsolutePath());
            System.out.println("[INFO] Recording macro: " + outFile.getName());
            Recorder recorder = new Recorder(outFile, stopKey);
            recorder.start();
        }
        else if (selected_action == 2){
            new_args.add("-input");
            String macro_name = getNewMacroName("Name of the macro to be replayed: ");
            System.out.println(macro_name);
            File inFile = new File(macroDir, macro_name);

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

            Boolean repeating = yesOrNoPrompt("Would you like the macro to repeat? (y/n)");
            if (repeating){
                repeatCount = getNumberOfRepeats("How many times would you like it to repeat? (enter -1 for infinite)\n");
            }
            logger.info("Replaying macro: {}", inFile.getAbsolutePath());
            System.out.println("[INFO] Replaying macro: " + inFile.getName());
            
            // DEFAULT: 1 replay
            int rc = (repeatCount == null ? 1 : repeatCount);

            // Normal repeat via Replayer handling it internally
            new Replayer(inFile.getAbsolutePath(), rc).start();
        }

        return null;
    }

    private static String getNewMacroName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine();
            if (line == null || line.isEmpty()) {
                System.out.println("Try again.");
            } else {
                return line;
            }
        }
    }

    private static int getNumberOfRepeats(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!SC.hasNextInt()) {
                System.out.println("Try again.");
            }
            return SC.nextInt();
        }
    }

// returns true for yes and false for no
private static Boolean yesOrNoPrompt(String prompt) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
        System.out.println(prompt);
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("y")) {
            return true;
        } else if (input.equalsIgnoreCase("n")) {
            return false;
        } else {
            System.out.println("Try again. Enter 'y' or 'n'.");
        }
    }
}





    // gets an int from the cmd line and validates it to ensure it exists in valid.
    // if valid is null then there is no validation.
    private static int getAndValidateIntInput(String prompt, int[] valid) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine();
            int in;

            try {
                in = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (valid == null)
                return in;

            boolean ok = false;
            for (int v : valid) {
                if (v == in) {
                    ok = true;
                    break;
                }
            }

            if (ok)
                return in;

            System.out.println("Invalid input. Please try again.");
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
     * Validates command-line arguments.
     * Rules:
     * - Either {@code -input <file>} or {@code -output <file>} must be provided, but not both.
     * - Each option must be followed by a filename.
     * - {@code -stopkey <name>} optionally sets the stop key for recording.
     * - {@code -l} lists available macros and cannot be combined with input or output.
     * @param args the arguments passed to {@link #main(String[])}
     * @return {@code null} if valid, otherwise an error string suitable for an exception message
     */
    public static String argChecks(String[] args) {
        if (args == null || args.length == 0) {
            logger.fatal("No arguments given!");
            return "ERROR: No arguments given!";
        }
        logger.trace("Arguments provided: {}", String.join(" ", args));
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
                case "-repeat" -> {
                    // repeat only makes sense when replaying, not recording
                    if (out_file_str != null) {
                        logger.fatal("-repeat cannot be used when recording!");
                        return "ERROR: -repeat can only be used with -input!";
                    }

                    // if next token exists AND is a number → treat it as count
                    if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
                        repeatCount = Integer.parseInt(args[i + 1]);
                        i++;
                    } else {
                        // no number provided = infinite loop
                        repeatCount = -1;   // use -1 as "infinite"
                    }
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
