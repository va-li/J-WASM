package environment;

import static util.Leb128.readUnsignedLeb128;

import com.sun.org.apache.xpath.internal.operations.Mod;
import constants.BinaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.ParserException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import util.Leb128;

/**
 * Created by Valentin
 * TODO documentation
 */
public class WASMInterpreter {
    private static Logger LOG = LoggerFactory.getLogger(WASMInterpreter.class);

    /**
     * The Instruction Pointer points to a byte in the instruction stream (a byte array) that should be interpreted and
     * executed.
     */
    private long instructionPointer;

    /**
     * The WebAssembly module to be interpreted and executed
     */
    private Module module;

    /**
     * On the Call Stack the execution environment (instruction pointer, local variables, etc.) is saved, before a
     * function call is executed.
     */
    private Stack<ExecEnvFrame> callStack = new Stack<>();

    /**
     * On the Operand Stack values are pushed and popped to be used as operands for various arithmetic or logic
     * operations
     */
    private Stack<Integer> operandStack = new Stack<>();

    public WASMInterpreter(Module module) {
        this.module = module;
    }

    public void step() {}

    public void execute(int[] parameters) {
        LOG.debug("Starting execution...");

        Function executingFunction = module.getStartFunction();

        // Push the parameters to the stack
        callStack.push(new ExecEnvFrame(executingFunction,
                new Integer[executingFunction.getLocalVariableCount() + executingFunction.getParameterCount()]));

        for (int i = 0; i < parameters.length; i++) {
            int parameter = parameters[i];
            callStack.peek().setLocalVariableByIndex(parameter, i);
        }

        ByteArrayInputStream executingCodeStream =
                new ByteArrayInputStream(executingFunction.getInstructions());


        while (executingCodeStream.available() != 0) {
            byte opCode = (byte) executingCodeStream.read();
            int parameter;

            ExecEnvFrame stackFrame = callStack.peek();
            if (stackFrame.isSkipCode()) {
                //when we see another block thing throw it into our stack
                if (BinaryFormat.Instructions.Control.IF == opCode) {
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.IF);
                    instructionPointer++;
                    continue;
                }
                if (BinaryFormat.Instructions.Control.BLOCK == opCode) {
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.BLOCK);
                    instructionPointer++;
                    continue;
                }
                if (BinaryFormat.Instructions.Control.LOOP == opCode) {
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.LOOP);
                    instructionPointer++;
                    continue;
                }


                if (!stackFrame.isIfBranch()) { //skipCode && else
                    if (stackFrame.getEndStack().size() == stackFrame.getIfDepth()) {
                        if (BinaryFormat.Instructions.Control.ELSE == opCode) {
                            stackFrame.setSkipCode(false);
                            instructionPointer++;
                            continue;
                        }
                    }
                } else { //skipCode && if
                    if (stackFrame.getIfDepth() == stackFrame.getEndStack().size()) {
                        if (BinaryFormat.Instructions.Control.END == opCode) {
                            stackFrame.setSkipCode(false);
                            stackFrame.getEndStack().pop();
                            stackFrame.setIfDepth(stackFrame.getIfDepth() - 1);
                            instructionPointer++;
                            continue;
                        }
                    }
                }
                if (BinaryFormat.Instructions.Control.END == opCode) {
                    if (stackFrame.getEndStack().size() == 0) {
                        LOG.debug(Arrays.toString(operandStack.toArray()));
                        return;
                    }
                    stackFrame.getEndStack().pop();
                }
                instructionPointer++;
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
                    operandStack.push(
                            Long.compareUnsigned(operandStack.pop(), operandStack.pop()) > 0 ? 1
                                    : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GT_S:
                    operandStack.push(operandStack.pop() < operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GT_U:
                    operandStack.push(
                            Long.compareUnsigned(operandStack.pop(), operandStack.pop()) < 0 ? 1
                                    : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LE_S:
                    operandStack.push(operandStack.pop() >= operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_LE_U:
                    operandStack.push(
                            Long.compareUnsigned(operandStack.pop(), operandStack.pop()) >= 0 ? 1
                                    : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GE_S:
                    operandStack.push(operandStack.pop() <= operandStack.pop() ? 1 : 0);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_GE_U:
                    operandStack.push(
                            Long.compareUnsigned(operandStack.pop(), operandStack.pop()) <= 0 ? 1
                                    : 0);
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
                 * Simple (add, sub, mul) sign-agnostic arithmetic operations can simply be performed with Java singed
                 * integer type, as the two's complement operations are the same as for unsigned operands.
                 *********************************/
                case BinaryFormat.Instructions.Numeric.I32_ADD:
                    int secondOperand = operandStack.pop();
                    int firstOperand = operandStack.pop();
                    operandStack.push(firstOperand + secondOperand);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_SUB:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(firstOperand - secondOperand);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_MUL:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(firstOperand * secondOperand);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_DIV_S:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(firstOperand / secondOperand);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_DIV_U:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(Integer.divideUnsigned(firstOperand, secondOperand));
                    break;
                case BinaryFormat.Instructions.Numeric.I32_REM_S:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(firstOperand % secondOperand);
                    break;
                case BinaryFormat.Instructions.Numeric.I32_REM_U:
                    secondOperand = operandStack.pop();
                    firstOperand = operandStack.pop();
                    operandStack.push(Integer.remainderUnsigned(firstOperand, secondOperand));
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
                case BinaryFormat.Instructions.Control.NOP:
                    //NO-OP
                    break;
                case BinaryFormat.Instructions.Control.BLOCK:
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.BLOCK);
                    break;
                case BinaryFormat.Instructions.Control.LOOP:
                    //TODO: Wait for loop instruction
                    break;
                case BinaryFormat.Instructions.Control.ELSE:
                    //TODO: hopefully this is right...
                    stackFrame.setSkipCode(true);
                    break;
                case BinaryFormat.Instructions.Control.IF:
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.IF);
                    stackFrame.setIfDepth(stackFrame.getIfDepth() + 1);
                    boolean ifExpression = operandStack.pop() != 0;
                    stackFrame.setIfBranch(ifExpression);
                    if (!ifExpression) { //we have else code here
                        stackFrame.setSkipCode(true);
                    }
                    executingCodeStream.read();
                    break;
                case BinaryFormat.Instructions.Control.CALL:
                    /***** Function call *****/
                    int calledFunctionIndex = Leb128.readUnsignedLeb128(executingCodeStream);
                    instructionPointer += Leb128.unsignedLeb128Size(calledFunctionIndex);
                    Function calledFunction = module.getFunctions().get(calledFunctionIndex);

                    // Set the return address for the current function
                    callStack.peek().setInstructionPointer(instructionPointer);
                    callStack.peek().setOperandStackBase(operandStack.size());

                    // Push the new function with its parameters to the call stack
                    callStack.push(new ExecEnvFrame(calledFunction,
                            new Integer[calledFunction.getLocalVariableCount() + calledFunction.getParameterCount()]));
                    for (int i = calledFunction.getParameterCount() - 1; i >= 0; i--) {
                        callStack.peek().setLocalVariableByIndex(operandStack.pop(), i);
                    }

                    // Set the new code and instruction pointer
                    instructionPointer = callStack.peek().getInstructionPointer();
                    executingCodeStream = new ByteArrayInputStream(calledFunction.getInstructions());
                    break;

                case BinaryFormat.Instructions.Control.RETURN:
                    /***** Function return *****/

                    if (callStack.size() == 1) {
                        int expectedReturnValueCount = callStack.peek().getFunction()
                                .getReturnValueCount();
                        int actualReturnValueCount = operandStack.size() - callStack.peek().getOperandStackBase();

                        if (expectedReturnValueCount != actualReturnValueCount) {
                            throw new ParserException("Wrong number of return values! Expected: " +
                                    expectedReturnValueCount + ", actual: " + actualReturnValueCount);
                        }
                        // Exit execution
                        return;
                    } else {
                        // Return to the previous function context
                        callStack.pop();
                        Function returnedFunction = callStack.peek().getFunction();
                        instructionPointer = callStack.peek().getInstructionPointer();
                        executingCodeStream = new ByteArrayInputStream(returnedFunction.getInstructions(), (int) instructionPointer + 1, returnedFunction.getInstructions().length);
                    }
                    break;
                case BinaryFormat.Instructions.Control.END:
                    //This should be the final end of the execution
                    LOG.info("Output: " + operandStack.pop());
                    break;
                case 0x1A: // drop
                    //operandStack.pop();
                    break;
                case -1:
                    throw new ParserException("Unexpected end of file! @code 0x10 body");
                default:
                    throw new ParserException("Invalid (or not implemented) instruction!");

            }
            instructionPointer++;
        }
    }
}