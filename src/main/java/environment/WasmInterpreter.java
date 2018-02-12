package environment;

import constants.BinaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.ParserException;
import util.Leb128;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Stack;

import static util.Leb128.readUnsignedLeb128;

/**
 * Created by Valentin
 * TODO documentation
 */
public class WasmInterpreter {
    private static Logger LOG = LoggerFactory.getLogger(WasmInterpreter.class);

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

    public WasmInterpreter(Module module) {
        this.module = module;
    }

    public void step() {
    }

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
            ExecEnvFrame stackFrame = callStack.peek();

            byte opCode;

            if (stackFrame.isSavedLoopExec() && !stackFrame.isSkipLoopCode()) {
                //when we are executing a loop for the NON-first time,
                //we get the instructions from our loop-instruction queue
                //and add it again to the queue to be able to execute further loop runs
                opCode = stackFrame.getLoopQueue().poll();
                stackFrame.getLoopQueue().add(opCode);
            } else {
                opCode = (byte) executingCodeStream.read();
                if (stackFrame.isSkipLoopCode()) {
                    //when the skipLoop flag is set we want to skip all the code till we got to the loop end
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
                    if (BinaryFormat.Instructions.Control.END == opCode) {
                        if (ExecEnvFrame.EndValue.LOOP.equals(stackFrame.getEndStack().pop())) {
                            stackFrame.resetLoop();
                        }
                    } else {
                        instructionPointer++;
                    }
                    continue;
                }
                if (stackFrame.isFirstLoopExec()) {
                    //when we are executing the loop for the first time,
                    //save all operations in a queue to execute them later in the additional runs
                    stackFrame.getLoopQueue().add(opCode);

                    //now repeat the loop!
                    if (BinaryFormat.Instructions.Control.END == opCode) {
                        stackFrame.getLoopQueue().add(opCode);
                        stackFrame.setFirstLoopExec(false);
                        stackFrame.setSavedLoopExec(true);
                        instructionPointer++;
                        continue;
                    }
                }
            }

            int parameter;

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
                    if (ExecEnvFrame.EndValue.LOOP.equals(stackFrame.getEndStack().pop())) {
                        instructionPointer = stackFrame.getLoopBeginInstructionPointer();
                        if (stackFrame.isFirstLoopExec()) {
                            stackFrame.setFirstLoopExec(false);
                            stackFrame.setSavedLoopExec(true);
                        }
                    }
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
                    parameter = readLoopSave(executingCodeStream, stackFrame);
                    operandStack.push(parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.GET_LOCAL:
                    parameter = readLoopSave(executingCodeStream, stackFrame);
                    operandStack.push(callStack.peek().getLocalVariableByIndex(parameter));
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.SET_LOCAL:
                    parameter = readLoopSave(executingCodeStream, stackFrame);
                    callStack.peek().setLocalVariableByIndex(operandStack.pop(), parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;
                case BinaryFormat.Instructions.Variable.TEE_LOCAL:
                    parameter = readLoopSave(executingCodeStream, stackFrame);
                    callStack.peek().setLocalVariableByIndex(operandStack.peek(), parameter);
                    instructionPointer += Leb128.unsignedLeb128Size(parameter);
                    break;

                /***************************
                 * Memory instructions
                 ****************************/
                case BinaryFormat.Instructions.Memory.I32_LOAD:
                    int alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    int offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    int address = operandStack.pop();
                    operandStack.push(module.getLinearMemory()
                        .load(address, alignment, offset, 4, LinearMemory.SIGNEDNESS.UNSIGNED));
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_LOAD8_S:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    address = operandStack.pop();
                    operandStack.push(module.getLinearMemory()
                        .load(address, alignment, offset, 1, LinearMemory.SIGNEDNESS.SIGNED));
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_LOAD8_U:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    address = operandStack.pop();
                    operandStack.push(module.getLinearMemory()
                        .load(address, alignment, offset, 1, LinearMemory.SIGNEDNESS.UNSIGNED));
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_LOAD16_S:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    address = operandStack.pop();
                    operandStack.push(module.getLinearMemory()
                        .load(address, alignment, offset, 2, LinearMemory.SIGNEDNESS.SIGNED));
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_LOAD16_U:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    address = operandStack.pop();
                    operandStack.push(module.getLinearMemory()
                        .load(address, alignment, offset, 2, LinearMemory.SIGNEDNESS.UNSIGNED));
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_STORE:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    int value = operandStack.pop();
                    address = operandStack.pop();
                    module.getLinearMemory().store(address, alignment, offset, 4, value);
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_STORE8:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    value = operandStack.pop();
                    address = operandStack.pop();
                    module.getLinearMemory().store(address, alignment, offset, 1, value);
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.I32_STORE16:
                    alignment = Leb128.readUnsignedLeb128(executingCodeStream);
                    offset = Leb128.readUnsignedLeb128(executingCodeStream);
                    value = operandStack.pop();
                    address = operandStack.pop();
                    module.getLinearMemory().store(address, alignment, offset, 2, value);
                    instructionPointer += Leb128.unsignedLeb128Size(alignment) + Leb128.unsignedLeb128Size(offset);
                    break;
                case BinaryFormat.Instructions.Memory.CURRENT_MEMORY:
                    executingCodeStream.skip(1); // an all zero byte is reserved for future use, so just skip it
                    operandStack.push(module.getLinearMemory().currentMemory());
                    instructionPointer++;
                    break;
                case BinaryFormat.Instructions.Memory.GROW_MEMORY:
                    executingCodeStream.skip(1); // an all zero byte is reserved for future use, so just skip it
                    int deltaPages = operandStack.pop();
                    operandStack.push(module.getLinearMemory().growMemory(deltaPages));
                    instructionPointer++;
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
                    stackFrame.getEndStack().push(ExecEnvFrame.EndValue.LOOP);
                    stackFrame.setFirstLoopExec(true);
                    stackFrame.setSavedLoopExec(false);
                    stackFrame.setLoopBeginInstructionPointer(instructionPointer);
                    //TODO: Wait for loop instruction
                    break;
                case BinaryFormat.Instructions.Control.BR_IF:
                    byte break_depth;
                    if (stackFrame.isFirstLoopExec()) {
                        break_depth = (byte) executingCodeStream.read();
                        stackFrame.getLoopQueue().add(break_depth);
                    } else {
                        break_depth = stackFrame.getLoopQueue().poll();
                        stackFrame.getLoopQueue().add(break_depth);
                    }
                    boolean brIfExpression = operandStack.peek() != 0;
                    if (brIfExpression) {
                        //if the branch condition is true, skip the loop
                        operandStack.pop();
                        stackFrame.resetLoop();
                        stackFrame.setSkipLoopCode(true);
                    }
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
                case 127:
                    LOG.debug("well, its ok..");
                    break;
                default:
                    throw new ParserException("Invalid (or not implemented) instruction!");
            }
//            if (stackFrame.isFirstLoopExec()) {
//                stackFrame.getLoopQueue().add(opCode);
//            }
            instructionPointer++;
        }
    }

    private int readLoopSave(ByteArrayInputStream is, ExecEnvFrame stack) {
        int value;
        if (stack.isFirstLoopExec()) {
            value = readUnsignedLeb128(is);
            stack.getIntLoopQueue().add(value);
        } else if (stack.isSavedLoopExec()) {
            value = stack.getIntLoopQueue().poll();
            stack.getIntLoopQueue().add(value);
        } else {
            value = readUnsignedLeb128(is);
        }
        return value;
    }
}