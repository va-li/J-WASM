import environment.Module;
import interpreter.WasmInterpreter;
import parser.BinaryParser;

import java.io.File;
import java.io.IOException;

/**
 * Program entry point
 * Handles commandline argument parsing and calls the parser and interpreter.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsageMessage();
            return;
        }

        boolean dumpLinearMemory = false;
        int argCount = args.length - 1;
        int argIndex = 1;
        while (argCount > 0) {
            switch (args[argIndex]) {
                case "-d":
                case "--dump-linear-memory":
                    dumpLinearMemory = true;
                    break;
                case "-h":
                case "--help":
                default:
                    printUsageMessage();
                    break;
            }
            argCount--;
        }

        File exectuable = new File(args[0]);

        if (!exectuable.exists() || exectuable.isDirectory()) {
            printUsageMessage();
            return;
        }

        Module module = new BinaryParser().parse(exectuable);

        new WasmInterpreter(module).execute(dumpLinearMemory);
    }

    private static void printUsageMessage() {
        System.out.println(
            "Usage: j-wasm <file-name.wasm> [options]\n"
                + "\t\tExecutes the passed WebAssebly program 'file-name.wasm'.\n\n"
                + "\t-h, --help\n"
                + "\t\t Prints this usage message.\n"
                + "\t-d, --dump-linear-memory\n"
                + "\t\t Dumps the linear memory contents to a file after execution inside the execution directory.\n"
        );
    }
}
