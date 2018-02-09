import environment.Module;
import environment.WASMInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.binary.BinaryParser;

import java.io.File;
import java.io.IOException;

/**
 * Program entry point
 * Handles commandline argument parsing and calls the parser and interpreter.
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsageMessage();
            return;
        }

        switch (args[0]) {
            case "-h":
            case "--help":
                printUsageMessage();
                break;

            default:
                File exectuable = new File(args[0]);
                int[] programArguments = new int[0];

                LOG.debug("Loading executable '{}'.", exectuable.getName());
                if (!exectuable.exists() || exectuable.isDirectory()) {
                    LOG.error("'{}' is not a file.", exectuable.getName());
                    printUsageMessage();
                    return;
                }

                LOG.debug("Parsing program arguments.");
                if (args.length > 1) {
                    programArguments = new int[args.length - 1];

                    int argIdx = args.length - 1;
                    while (argIdx > 0) {
                        programArguments[argIdx] = Integer.parseInt(args[argIdx]);
                    }
                }

                Module module = new BinaryParser().parse(exectuable);

                new WASMInterpreter(module).execute(programArguments);
        }
    }

    private static void printUsageMessage() {
        LOG.info(
            "Usage: j-wasm <file-name.wasm> [arguments]\n"
                + "\t\tExecutes the passed WebAssebly program 'file-name.wasm' with the passed arguments 'args'.\n\n"
                + "\t-h, --help\n"
                + "\t\t Prints this usage message."
        );
    }
}
