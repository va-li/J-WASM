package parser.binary;

import ast.Function;
import ast.Module;
import ast.ValueType;
import ast.instructions.controls.IfThenElseInstr;
import ast.instructions.controls.NoOperationInstr;
import ast.instructions.controls.UnreachableInstr;
import ast.instructions.expressions.AbstractExpression;
import ast.instructions.expressions.call.FunctionCall;
import ast.instructions.expressions.call.FunctionCallBuilder;
import ast.instructions.expressions.constants.I32Const;
import ast.instructions.expressions.operators.binary.*;
import ast.instructions.expressions.operators.relational.*;
import ast.instructions.expressions.operators.test.I32Eqz;
import ast.instructions.expressions.operators.unary.I32Clz;
import ast.instructions.expressions.operators.unary.I32Ctz;
import ast.instructions.expressions.operators.unary.I32Popcnt;
import ast.instructions.expressions.variables.I32GetLocal;
import constants.BinaryFormat;
import parser.Parser;
import parser.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class BinaryParser implements Parser {

    private int currSection = -0x01;
    private byte[] code;


    public static void main(String[] args) throws IOException {
        Parser p = new BinaryParser();
        p.parse(new File("wabt_tests/test.wasm"));

        //for breakpoint purpose
        int i = 1;
    }

    public void parse(File file) throws IOException, ParserException {

        this.code = Files.readAllBytes(Paths.get(file.toURI()));


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
    }

    private void readCodeSection(final InputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        int numFun = is.read();
        int currFun = 0;

        while (numFun > 0) {
            //body size guess for skipping purpose
            int bodySizeGuess = is.read();

            //???
            int localDeclCount = is.read();

            //we need to start at index 0 and inc with each loop
            currFun++;

            int instruction = is.read();
            while (instruction != BinaryFormat.Instructions.Control.END) {
                switch (instruction) {
                    /***************************
                     * Variable and constant access instructions
                     ****************************/
                    case BinaryFormat.Instructions.Numeric.I32_CONST:
                        //the int is LEB128 encoded, so read it and then add the operation
                        helpStack.push(new I32Const(readUnsignedLeb128(is)));
                        break;
                    case BinaryFormat.Instructions.Variable.GET_LOCAL:
                        helpStack.push(new I32GetLocal(is.read()));
                        break;
                    case BinaryFormat.Instructions.Variable.SET_LOCAL:
                        //TODO
                        break;
                    case BinaryFormat.Instructions.Variable.TEE_LOCAL:
                        //TODO
                        break;

                    /*****************************
                     * Test instructions
                     *****************************/
                    case BinaryFormat.Instructions.Numeric.I32_EQZ:
                        f.addInstruction(new I32Eqz(helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_EQ:
                        f.addInstruction(new I32Eq(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_NE:
                        f.addInstruction(new I32Ne(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_LT_S:
                        f.addInstruction(new I32LtS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_LT_U:
                        f.addInstruction(new I32LtU(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_GT_S:
                        f.addInstruction(new I32GtS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_GT_U:
                        f.addInstruction(new I32GtU(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_LE_S:
                        f.addInstruction(new I32LeS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_LE_U:
                        f.addInstruction(new I32LeU(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_GE_S:
                        f.addInstruction(new I32GeS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_GE_U:
                        f.addInstruction(new I32GeU(helpStack.pop(), helpStack.pop()));
                        break;

                    /*****************************
                     * Unary instructions
                     *****************************/
                    case BinaryFormat.Instructions.Numeric.I32_CLZ:
                        f.addInstruction(new I32Clz(helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_CTZ:
                        f.addInstruction(new I32Ctz(helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_POPCNT:
                        f.addInstruction(new I32Popcnt(helpStack.pop()));
                        break;


                    /*********************************
                     * Arithmetic instructions
                     *********************************/
                    case BinaryFormat.Instructions.Numeric.I32_ADD:
                        f.addInstruction(new I32Add(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_SUB:
                        f.addInstruction(new I32Sub(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_MUL:
                        f.addInstruction(new I32Mul(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_DIV_S:
                        f.addInstruction(new I32DivS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_DIV_U:
                        f.addInstruction(new I32DivU(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_REM_S:
                        f.addInstruction(new I32RemS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_REM_U:
                        f.addInstruction(new I32RemU(helpStack.pop(), helpStack.pop()));
                        break;

                    /*********************************
                     * Logical instructions
                     *********************************/
                    case BinaryFormat.Instructions.Numeric.I32_AND:
                        f.addInstruction(new I32And(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_OR:
                        f.addInstruction(new I32Or(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_XOR:
                        f.addInstruction(new I32XOr(helpStack.pop(), helpStack.pop()));
                        break;

                    /*********************************
                     * Bitwise instructions
                     *********************************/
                    case BinaryFormat.Instructions.Numeric.I32_SHL:
                        f.addInstruction(new I32Shl(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_SHR_S:
                        f.addInstruction(new I32ShrS(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_SHR_U:
                        f.addInstruction(new I32ShrU(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_ROTL:
                        f.addInstruction(new I32Rotl(helpStack.pop(), helpStack.pop()));
                        break;
                    case BinaryFormat.Instructions.Numeric.I32_ROTR:
                        f.addInstruction(new I32Rotr(helpStack.pop(), helpStack.pop()));
                        break;


                    /******************************
                     * Control instructions
                     *****************************/
                    case BinaryFormat.Instructions.Control.UNREACHABLE:
                        f.addInstruction(new UnreachableInstr());
                        break;
                    case BinaryFormat.Instructions.Control.NOP:
                        f.addInstruction(new NoOperationInstr());
                        break;
                    case BinaryFormat.Instructions.Control.BLOCK:
                        //TODO: I don't think we'll implement this
                        break;
                    case BinaryFormat.Instructions.Control.LOOP:
                        //TODO: Wait for loop instruction
                        break;
                    case BinaryFormat.Instructions.Control.IF:
                        int instr = is.read();
                        while (instr != BinaryFormat.Instructions.Control.ELSE) {
                            switch (instr) {
                                case -1:
                                    throw new ParserException("Unexpected end of file! @If then else");
                            }
                        }
                        //f.addInstruction();
                        break;
                    case BinaryFormat.Instructions.Control.CALL:
                        int funIndex = is.read();
                        //TODO: Implement Parameters
                        f.addInstruction(FunctionCallBuilder.getI32FunctionCall(module.getFunction(funIndex).getReturnType(), funIndex, null));
                        break;

                    case BinaryFormat.Instructions.Control.RETURN:
                        //TODO: evaluate HOW
                        break;
                        case
                    case -1:
                        throw new ParserException("Unexpected end of file! @code 0x10 body");
                    default:
                        throw new ParserException("Invalid (or not implemented instrcution) instruction!");
                }
                instruction = is.read();
            }
            numFun--;
        }
    }

    private void readStartSection(final InputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        //set the index of the start function in the module
        int startFunIdx = is.read();
    }

    private void readMemorySection(final InputStream is)
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

    private void readFunctionSection(final InputStream is)
            throws IOException, ParserException {
        //section size guess for skipping purpose
        int sectionSizeGuess = is.read();

        int numFun = is.read();

        while (numFun > 0) {

            //TODO: don't think we need this?
            int signatureIndex = is.read();

            numFun--;
        }
    }

    private void readTypeSection(final InputStream is)
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
                        //TODO: do smth with valueType
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

            numTypes--;
        }
    }

    /**
     * Reads an unsigned integer from {@code in}.
     * Thank you android dex for the source!
     * https://github.com/facebook/buck/blob/master/third-party/java/dx/src/com/android/dex/Leb128.java
     */
    private int readUnsignedLeb128(InputStream in) throws IOException {
        int result = 0;
        int cur;
        int count = 0;

        do {
            cur = in.read() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);

        if ((cur & 0x80) == 0x80) {
            throw new ParserException("invalid LEB128 sequence");
        }

        return result;
    }
}