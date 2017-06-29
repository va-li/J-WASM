package parser.binary;

import constants.BinaryFormat;
import environment.Function;
import environment.WASMInterpreter;
import parser.Parser;
import parser.ParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static util.Leb128.readUnsignedLeb128;

public class BinaryParser implements Parser {

    private int currSection = -0x01;
    //private byte[] code;
    private List<Function> functions = new ArrayList<>();
    private int startFunctionIndex = -1;

    public static void main(String[] args) throws IOException {
        Parser p = new BinaryParser();
        p.parse(new File("wabt_tests/test.wasm"));

        //for breakpoint purpose
        int i = 1;
    }

    public void parse(File file) throws IOException, ParserException {

        byte[] code = Files.readAllBytes(Paths.get(file.toURI()));

        ByteArrayInputStream is = new ByteArrayInputStream(code);

        //test if the file has a correct header
        byte helper[] = new byte[4];
        is.read(helper);
        if (!Arrays.equals(helper, BinaryFormat.Module.MAGIC)) {
            throw new ParserException("No valid .wasm File");
        }

        helper = new byte[4];
        is.read(helper);
        if (!Arrays.equals(helper, BinaryFormat.Module.VERSION)) {
            throw new ParserException("Not a valid Version!");
        }
        int sectionID;
        while ((sectionID = is.read()) != -1) {
            switch (sectionID) {
                case BinaryFormat.Module.Section.Type.ID:
                    if (this.currSection >= BinaryFormat.Module.Section.Type.ID) {
                        throw new ParserException("Wrong order of sections! @Type(0x01)");
                    }
                    currSection = BinaryFormat.Module.Section.Type.ID;
                    readTypeSection(is);
                    break;
                case BinaryFormat.Module.Section.Function.ID:
                    if (this.currSection >= BinaryFormat.Module.Section.Function.ID) {
                        throw new ParserException("Wrong order of sections! @Function(0x03)");
                    }
                    currSection = BinaryFormat.Module.Section.Function.ID;
                    readFunctionSection(is);
                    break;
                case BinaryFormat.Module.Section.Memory.ID:
                    if (this.currSection >= BinaryFormat.Module.Section.Memory.ID) {
                        throw new ParserException("Wrong order of sections! @Memory(0x05)");
                    }
                    currSection = BinaryFormat.Module.Section.Memory.ID;
                    readMemorySection(is);
                    break;
                case BinaryFormat.Module.Section.Start.ID:
                    if (this.currSection >= BinaryFormat.Module.Section.Start.ID) {
                        throw new ParserException("Wrong order of sections! @Start(0x08)");
                    }
                    currSection = BinaryFormat.Module.Section.Start.ID;
                    readStartSection(is);
                    break;
                case BinaryFormat.Module.Section.Code.ID:
                    if (this.currSection >= BinaryFormat.Module.Section.Code.ID) {
                        throw new ParserException("Wrong order of sections! @Code(0x0A)");
                    }
                    currSection = BinaryFormat.Module.Section.Code.ID;
                    readCodeSection(is);
                    break;
                case -1:
                    //EOF reached
                default:
                    throw new ParserException("Not a valid section type");
            }
        }
        new WASMInterpreter(this.functions, this.startFunctionIndex).execute();
    }

    private void readCodeSection(final ByteArrayInputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        int numFun = is.read();
        int currFun = 0;

        while (numFun > 0) {
            //body size guess for skipping purpose
            int bodySizeGuess = is.read();

            int localVarCount = readUnsignedLeb128(is);
            this.functions.get(currFun).setLocalVariableCount(localVarCount);

            byte[] funcBody = new byte[bodySizeGuess - unsignedLeb128Size(localVarCount)];
            is.read(funcBody);
            this.functions.get(currFun).setInstructions(funcBody);

            //we need to start at index 0 and inc with each loop
            currFun++;


            numFun--;
        }
    }

    private void readStartSection(final ByteArrayInputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        //set the index of the start function in the module
        this.startFunctionIndex = is.read();
    }

    private void readMemorySection(final ByteArrayInputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        //the number of total memory specifications in the module BUT only one entry is allowed...
        int numMem = is.read();
        if (numMem != 1) {
            throw new ParserException("Only one memory block allowed!");
        }

        //this flag specifies if a max memory is given
        int flags = is.read();
        int maxMem = -1;
        int initMem = readUnsignedLeb128(is);

        if (flags == 1) {
            maxMem = readUnsignedLeb128(is);
        }
    }

    private void readFunctionSection(final ByteArrayInputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        int numFun = is.read();

        while (numFun > 0) {

            int signatureIndex = is.read();

            numFun--;
        }
    }

    private void readTypeSection(final ByteArrayInputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();
        //number of type (seems like the function header count?)
        int numTypes = is.read();


        //iterate through all the function headers
        while (numTypes > 0) {
            if (is.read() != BinaryFormat.Types.FUNCTION_TYPE) {
                throw new ParserException("Function Headers are not specified correct!");
            }
            //number of parameters of the function
            int numParams = is.read();

            while (numParams > 0) {
                //add the parameters to a list, which will be passed to a function object
                switch ((byte) is.read()) {
                    case BinaryFormat.Types.ValueType.I32:

                        break;
                    default:
                        throw new ParserException("Invalid parameter type at function header!");
                }
                numParams--;
            }

            //this is not a list because multiple return values are not currently supported!

            int numResults = is.read();
            while (numResults > 0) {
                //add results to a list, which will be passed to a function object
                switch ((byte) is.read()) {
                    case BinaryFormat.Types.ValueType.I32:
                        //TODO: do smth
                        break;
                    default:
                        throw new ParserException("Invalid (or not supported) result type at function header!");
                }
                numResults--;
            }

            this.functions.add(new Function(numParams, numResults));
            numTypes--;
        }
    }


    /**
     * Gets the number of bytes in the unsigned LEB128 encoding of the
     * given value.
     *
     * @param value the value in question
     * @return its write size, in bytes
     */
    public static int unsignedLeb128Size(int value) {
        // TODO: This could be much cleverer.
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }
}