package parser;

import constants.BinaryFormat;
import constants.ImplementationSpecific;
import environment.Function;
import environment.LinearMemory;
import environment.Module;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static util.Leb128.readUnsignedLeb128;
import static util.Leb128.unsignedLeb128Size;

public class BinaryParser {

    private int previousSection = -0x01;
    //private byte[] code;
    private List<Function> functions = new ArrayList<>();
    private int startFunctionIndex = -1;
    private LinearMemory linearMemory;


    public Module parse(File file) throws IOException, ParserException {
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
                    if (this.previousSection >= BinaryFormat.Module.Section.Type.ID) {
                        throw new ParserException("Wrong order of sections! @Type(0x01)");
                    }
                    previousSection = BinaryFormat.Module.Section.Type.ID;
                    readTypeSection(is);
                    break;
                case BinaryFormat.Module.Section.Function.ID:
                    if (this.previousSection >= BinaryFormat.Module.Section.Function.ID) {
                        throw new ParserException("Wrong order of sections! @Function(0x03)");
                    }
                    previousSection = BinaryFormat.Module.Section.Function.ID;
                    readFunctionSection(is);
                    break;
                case BinaryFormat.Module.Section.Memory.ID:
                    if (this.previousSection >= BinaryFormat.Module.Section.Memory.ID) {
                        throw new ParserException("Wrong order of sections! @Memory(0x05)");
                    }
                    previousSection = BinaryFormat.Module.Section.Memory.ID;
                    readMemorySection(is);
                    break;
                case BinaryFormat.Module.Section.Start.ID:
                    if (this.previousSection >= BinaryFormat.Module.Section.Start.ID) {
                        throw new ParserException("Wrong order of sections! @Start(0x08)");
                    }
                    previousSection = BinaryFormat.Module.Section.Start.ID;
                    readStartSection(is);
                    break;
                case BinaryFormat.Module.Section.Code.ID:
                    if (this.previousSection >= BinaryFormat.Module.Section.Code.ID) {
                        throw new ParserException("Wrong order of sections! @Code(0x0A)");
                    }
                    previousSection = BinaryFormat.Module.Section.Code.ID;
                    readCodeSection(is);
                    break;
                case BinaryFormat.Module.Section.Data.ID:
                    if (this.previousSection >= BinaryFormat.Module.Section.Data.ID) {
                        throw new ParserException("Wrong order of sections! @Code(0x0B)");
                    }
                    previousSection = BinaryFormat.Module.Section.Data.ID;
                    readDataSection(is);
                    break;
                case BinaryFormat.Module.Section.Custom.ID:
                    int sectionSizeGuess = is.read();
                    is.skip(sectionSizeGuess);
                    break;
                default:
                    throw new ParserException("Not a valid section type");
            }
        }

        return new Module(linearMemory, functions, startFunctionIndex);
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
        int maxMem = ImplementationSpecific.LinearMemory.PAGE_COUNT_MAX;
        int initMem = readUnsignedLeb128(is);

        if (flags == 1) {
            maxMem = readUnsignedLeb128(is);
        }

        linearMemory = new LinearMemory(initMem, maxMem);
    }


    private void readDataSection(final ByteArrayInputStream is) throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        int dataSegmentCount = is.read();

        for (int i =  0; i < dataSegmentCount; i++) {
            int memoryIndex = is.read();
            if (memoryIndex != 0) {
                throw new ParserException("Only memory index zero is supported!");
            } else if (linearMemory == null) {
                throw new ParserException("No linear memory defined for data!");
            }

            int constExpr = is.read();
            int address = is.read();
            int endExpr = is.read();
            int dataSegmentSize = is.read();

            if (constExpr != BinaryFormat.Instructions.Numeric.I32_CONST
                || endExpr != BinaryFormat.Instructions.Control.END) {
                throw new ParserException("Malformed data segment offset!");
            }

            for (int j = 0; j < dataSegmentSize; j++) {
                linearMemory.store(address + j, 0, 0, 1, is.read());
            }
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
            int numParamsP = numParams;
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
            int numResultsP = numResults;

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

            this.functions.add(new Function(numParamsP, numResultsP));
            numTypes--;
        }
    }


}