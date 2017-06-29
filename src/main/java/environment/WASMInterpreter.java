package environment;

import static util.Leb128.readUnsignedLeb128;

import constants.BinaryFormat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class WASMInterpreter {

    private List<Function> functions;
    private int startFunctionIndex;

    private long instructionPointer;

    public WASMInterpreter(List<Function> functions, int startFunctionIndex) {
        this.functions = functions;
        this.startFunctionIndex = startFunctionIndex;
    }

    public void execute() {
        // Set up the local variables and both stacks
        Function executingFunction = functions.get(startFunctionIndex);
        Stack<CallStackFrame> callStack = new Stack<>();
        Stack<Integer> operandStack = new Stack<>();

        ByteArrayInputStream executingCodeStream =
                new ByteArrayInputStream(executingFunction.getInstructions());

        callStack.push(new CallStackFrame(executingFunction,
                new int[executingFunction.getLocalVariableCount()], 0));

        while (executingCodeStream.available() != 0) {
            byte opCode = (byte) executingCodeStream.read();

            switch (opCode) {
                /***************************
                 * Variable and constant access instructions
                 ****************************/
                case BinaryFormat.Instructions.Numeric.I32_CONST:
                    //the int is LEB128 encoded, so read it and then add the operation
                    operandStack.push(readUnsignedLeb128(executingCodeStream));
                    break;
                case BinaryFormat.Instructions.Variable.GET_LOCAL:
                    callStack.peek().getLocalVariableByIndex(readUnsignedLeb128(executingCodeStream));
                    break;
                case BinaryFormat.Instructions.Variable.SET_LOCAL:
                    callStack.peek().setLocalVariableByIndex(operandStack.pop(), readUnsignedLeb128(executingCodeStream));
                    break;
                case BinaryFormat.Instructions.Variable.TEE_LOCAL:
                    callStack.peek().setLocalVariableByIndex(operandStack.peek(), readUnsignedLeb128(executingCodeStream));
                    break;

                /*****************************
                 * Test instructions
                 *****************************/
                case BinaryFormat.Instructions.Numeric.I32_EQZ:
                    operandStack.push(operandStack.pop() == 0 ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_EQ:
                    operandStack.push(operandStack.pop().equals(operandStack.pop()) ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_NE:
                    operandStack.push(!operandStack.pop().equals(operandStack.pop()) ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LT_S:
                    operandStack.push(operandStack.pop() > operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LT_U:
                    operandStack.push(Integer.compareUnsigned(operandStack.pop(), operandStack.pop()) > 0 ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GT_S:
                    operandStack.push(operandStack.pop() < operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GT_U:
                    operandStack.push(Integer.compareUnsigned(operandStack.pop(), operandStack.pop()) < 0 ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LE_S:
                    operandStack.push(operandStack.pop() >= operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LE_U:
                    operandStack.push(Integer.compareUnsigned(operandStack.pop(), operandStack.pop()) >= 0 ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GE_S:
                    operandStack.push(operandStack.pop() <= operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GE_U:
                    operandStack.push(Integer.compareUnsigned(operandStack.pop(), operandStack.pop()) <= 0 ? 1 : 0);
                    break;

                /*****************************
                 * Unary instructions
                 *****************************/
                case BinaryFormat.Instructions.Numeric.I32_CLZ:
                    operandStack.push(Integer.numberOfLeadingZeros(operandStack.pop()));
                    break;
                case BinaryFormat.Instructions.Numeric.I32_CTZ:
                    operandStack.push(Integer.numberOfTrailingZeros(operandStack.pop()));
                    break;
                case BinaryFormat.Instructions.Numeric.I32_POPCNT:
                    operandStack.push(Integer.bitCount(operandStack.pop()));
                    break;


                /*********************************
                 * Arithmetic instructions
                 *********************************/
                case BinaryFormat.Instructions.Numeric.I32_ADD:
                    operandStack.push(operandStack.pop() + operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_SUB:
                    operandStack.push(operandStack.pop() - operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_MUL:
                    operandStack.push(operandStack.pop() * operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_DIV_S:
                    operandStack.push(operandStack.pop() / operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_DIV_U:
                    operandStack.push(Math.floorDiv(operandStack.pop(), operandStack.pop()));
                    break;
                case BinaryFormat.Instructions.Numeric.I32_REM_S:
                    operandStack.push(operandStack.pop() % operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_REM_U:
                    operandStack.push(Math.floorMod(operandStack.pop(), operandStack.pop()));
                    break;

                /*********************************
                 * Logical instructions
                 *********************************/
                case BinaryFormat.Instructions.Numeric.I32_AND:
                    operandStack.push(operandStack.pop() & operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_OR:
                    operandStack.push(operandStack.pop() | operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_XOR:
                    operandStack.push(operandStack.pop() ^ operandStack.pop());
                    break;

                /*********************************
                 * Bitwise instructions
                 *********************************/
                case BinaryFormat.Instructions.Numeric.I32_SHL:
                    operandStack.push(operandStack.pop() << operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_SHR_S:
                    operandStack.push(operandStack.pop() >> operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_SHR_U:
                    operandStack.push(operandStack.pop() >>> operandStack.pop());
                    break;
                case BinaryFormat.Instructions.Numeric.I32_ROTL:
                    operandStack.push(Integer.rotateLeft(operandStack.pop(), operandStack.pop()));
                    break;
                case BinaryFormat.Instructions.Numeric.I32_ROTR:
                    operandStack.push(Integer.rotateRight(operandStack.pop(), operandStack.pop()));
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
        }
    }
}
