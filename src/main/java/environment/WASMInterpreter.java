package environment;

import static util.Leb128.readUnsignedLeb128;

import constants.BinaryFormat;
import parser.Parser;
import parser.ParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import util.Leb128;

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

    public void execute(int[] parameters) {
        // Set up the local variables and both stacks
        Function executingFunction = functions.get(startFunctionIndex);
        Stack<CallStackFrame> callStack = new Stack<>();
        Stack<Integer> operandStack = new Stack<>();

        // Push the parameters to the stack
        for (int i = 0; i < parameters.length; i++) {
            int parameter = parameters[i];
            callStack.peek().setLocalVariableByIndex(parameter, i);
        }

        ByteArrayInputStream executingCodeStream =
                new ByteArrayInputStream(executingFunction.getInstructions());

        callStack.push(new CallStackFrame(executingFunction,
                new int[executingFunction.getLocalVariableCount()], 0));

        while (executingCodeStream.available() != 0) {
            byte opCode = (byte) executingCodeStream.read();
            int parameter;

            CallStackFrame stackFrame = callStack.peek();
            if (stackFrame.isSkipCode()) {
                //when we see another block thing increment depth
                if (BinaryFormat.Instructions.Control.IF == opCode ||
                        BinaryFormat.Instructions.Control.BLOCK == opCode ||
                        BinaryFormat.Instructions.Control.LOOP == opCode) {
                    stackFrame.setDepth(stackFrame.getDepth() + 1);
                    continue;
                }
                if (!stackFrame.isIfBranch()) { //skipCode && else
                    if (stackFrame.getDepth() == stackFrame.getIfDepth()) {
                        if (BinaryFormat.Instructions.Control.ELSE == opCode) {
                            stackFrame.setSkipCode(false);
                            continue;
                        }
                    }
                } else { //skipCode && if
                    if (stackFrame.getIfDepth() == stackFrame.getDepth()) {
                        if (BinaryFormat.Instructions.Control.END == opCode) {
                            stackFrame.setSkipCode(false);
                            stackFrame.setIfDepth(stackFrame.getIfDepth() - 1);
                            stackFrame.setDepth(stackFrame.getDepth() - 1);
                            continue;
                        }
                    }
                }
                continue;
            }

            switch (opCode) {
                /***************************
                 * Variable and constant access instructions
                 ****************************/
                case BinaryFormat.Instructions.Numeric.I32_CONST:
                    //the int is LEB128 encoded, so read it and then add the operation
                    parameter = readUnsignedLeb128(executingCodeStream);
                    operandStack.push(parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.GET_LOCAL:
                    parameter = readUnsignedLeb128(executingCodeStream);
                    operandStack.push(callStack.peek().getLocalVariableByIndex(parameter));
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.SET_LOCAL:
                    parameter = readUnsignedLeb128(executingCodeStream);
                    callStack.peek().setLocalVariableByIndex(operandStack.pop(), parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.TEE_LOCAL:
                    parameter = readUnsignedLeb128(executingCodeStream);
                    callStack.peek().setLocalVariableByIndex(operandStack.peek(), parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
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
                    throw new ParserException("You reached unreachable code!");
                    break;
                case BinaryFormat.Instructions.Control.NOP:
                    //NO-OP
                    break;
                case BinaryFormat.Instructions.Control.BLOCK:
                    stackFrame.setDepth(stackFrame.getDepth() + 1);
                    break;
                case BinaryFormat.Instructions.Control.LOOP:
                    //TODO: Wait for loop instruction
                    break;
                case BinaryFormat.Instructions.Control.IF:
                    stackFrame.setDepth(stackFrame.getDepth() + 1);
                    stackFrame.setIfDepth(stackFrame.getIfDepth() + 1);
                    boolean ifExpression = operandStack.pop() != 0;
                    stackFrame.setIfBranch(ifExpression);
                    if (!ifExpression) { //we have else code here
                        stackFrame.setSkipCode(true);
                    }
                    break;
                case BinaryFormat.Instructions.Control.CALL:
                    /***** Function call *****/
                    int calledFunctionIndex = Leb128.readUnsignedLeb128(executingCodeStream);
                    instructionPointer += Leb128.unsignedLeb128Size(calledFunctionIndex);
                    Function calledFunction = functions.get(calledFunctionIndex);

                    // Set the return address for the current function
                    instructionPointer++;
                    callStack.peek().setInstructionPointer(instructionPointer);

                    // Push the new function with its parameters to the call stack
                    callStack.push(new CallStackFrame(calledFunction, new int[calledFunction.getLocalVariableCount()], 0));
                    for (int i = calledFunction.getParameterCount() - 1; i > 0; i--) {
                        callStack.peek().setLocalVariableByIndex(operandStack.pop(), i);
                    }

                    // Set the new code and instruction pointer
                    executingCodeStream = new ByteArrayInputStream(calledFunction.getInstructions());
                    instructionPointer = callStack.peek().getInstructionPointer();
                    break;

                case BinaryFormat.Instructions.Control.RETURN:
                    /***** Function return *****/
                    int expectedReturnValueCount = callStack.peek().getFunction().getReturnValueCount();
                    int actualReturnValueCount = operandStack.size() - operandStackBase;

                    if (expectedReturnValueCount != actualReturnValueCount) {
                        throw new ParserException("Wrong number of return values! Expected: " +
                                expectedReturnValueCount + ", actual: " + actualReturnValueCount);
                        if (actualReturnValueCount > expectedReturnValueCount) {
                            throw new RuntimeException("Wrong number of return values! Expected: " +
                                    expectedReturnValueCount + ", actual: " + actualReturnValueCount);
                        }


                        if (callStack.size() == 1) {
                            // Exit execution
                            if (operandStack.size() != 0) {
                                throw new ParserException("Start function must not return anything!");
                            }
                            return;
                        } else {
                            // Return to the previous function context
                            callStack.pop();
                            instructionPointer = callStack.peek().getInstructionPointer();
                        }
                        break;
                        case BinaryFormat.Instructions.Control.END:
                            stackFrame.setDepth(stackFrame.getDepth() - 1);
                            break;
                        case -1:
                            throw new ParserException("Unexpected end of file! @code 0x10 body");
                        default:
                            throw new ParserException("Invalid (or not implemented) instruction!");

                    }
            }
        }
    }
}