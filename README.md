# Project Name
Universal Macro Builder is a tool for automating repetitive tasks by recording and replaying mouse and keyboard input.

TODO: get a good screen capture of functionality here.
# How to run
- Download the latest JAR from the Release section on the right on GitHub.
- Run with
```
java -jar <path-to-jar>.jar <arguments>
```
Arguments are as specified below:

`-output <path-to-output-file> -stopkey <key>`:  enters record mode and writes the recorded macro to the output file specified.

Stop key codes are inputted as strings and resolved according to https://javadoc.io/static/com.1stleg/jnativehook/2.0.3/constant-values.html#org.jnativehook.keyboard.NativeKeyEvent.VC_N. For example, an input of `NUM_LOCK` will properly resolve to `VC_NUM_LOCK`, whereas `NUMLOCK` will fail and default to `VC_ESCAPE`.

`-input <path-to-input-file>`: enters replay mode and loads macro from the input file specified.

Either `-output` or `-input` must be provided, and they each require paths to be given as the next argument.

# How to contribute
Follow this project board to know the latest status of the project: https://github.com/orgs/cis3296f25/projects/66/views/1

### How to build
- Use this github repository: https://github.com/cis3296f25/UniversalMacroBuilder
- Main is reserved for stable releases. Please create PRs to a staging branch.
- Use provided pom.xml for dependencies.
- Run `mvn package` to build.
