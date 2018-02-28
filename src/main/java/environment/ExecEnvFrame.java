package environment;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * This class is similar to an Activation Frame/Record in other machines, as it stores local variables and the
 * instruction pointer, in addition to many other values/references needed for the execution of a WASM module.
 */
public class ExecEnvFrame {

    public enum EndValue {
        LOOP,
        IF,
        BLOCK
    }

    private final Function function;
    private final Integer[] localVariables;

    private int instructionPointer = 0;
    private int loopBeginInstructionPointer = 0;

    private int ifDepth = 0;
    private int operandStackBase = 0;
    private boolean ifBranch = true;
    private boolean skipCode = false;
    private boolean firstLoopExec = false;
    private boolean savedLoopExec = false;
    private boolean skipLoopCode = false;

    private final Stack<EndValue> endStack = new Stack<>();

    private final Queue<Byte> loopQueue = new LinkedList<>();
    private final Queue<Integer> intLoopQueue = new LinkedList<>();

    public ExecEnvFrame(Function function, Integer[] localVariables) {
        this.function = function;
        this.localVariables = localVariables;
    }

    public Function getFunction() {
        return function;
    }

    public Integer[] getLocalVariables() {
        return localVariables;
    }

    public int getLocalVariableByIndex(int index) {
        return localVariables[index];
    }

    public void setLocalVariableByIndex(int value, int index) {
        localVariables[index] = value;
    }

    public int getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(int instructionPointer) {
        this.instructionPointer = instructionPointer;
    }

    public int getIfDepth() {
        return ifDepth;
    }

    public void setIfDepth(int ifDepth) {
        this.ifDepth = ifDepth;
    }

    public int getOperandStackBase() {
        return operandStackBase;
    }

    public void setOperandStackBase(int operandStackBase) {
        this.operandStackBase = operandStackBase;
    }

    public boolean isIfBranch() {
        return ifBranch;
    }

    public void setIfBranch(boolean ifBranch) {
        this.ifBranch = ifBranch;
    }

    public boolean isSkipCode() {
        return skipCode;
    }

    public void setSkipCode(boolean skipCode) {
        this.skipCode = skipCode;
    }

    public Stack<EndValue> getEndStack() {
        return endStack;
    }

    public boolean isFirstLoopExec() {
        return firstLoopExec;
    }

    public void setFirstLoopExec(boolean firstLoopExec) {
        this.firstLoopExec = firstLoopExec;
    }

    public boolean isSavedLoopExec() {
        return savedLoopExec;
    }

    public void setSavedLoopExec(boolean savedLoopExec) {
        this.savedLoopExec = savedLoopExec;
    }

    public Queue<Byte> getLoopQueue() {
        return loopQueue;
    }

    public int getLoopBeginInstructionPointer() {
        return loopBeginInstructionPointer;
    }

    public void setLoopBeginInstructionPointer(int loopBeginInstructionPointer) {
        this.loopBeginInstructionPointer = loopBeginInstructionPointer;
    }

    public void setSkipLoopCode(boolean skipLoopCode) {
        this.skipLoopCode = skipLoopCode;
    }

    public boolean isSkipLoopCode() {
        return skipLoopCode;
    }

    public void resetLoop() {
        this.loopQueue.clear();
        this.intLoopQueue.clear();
        this.loopBeginInstructionPointer = 0;
        this.firstLoopExec = false;
        this.savedLoopExec = false;
        this.skipLoopCode = false;
    }

    public Queue<Integer> getIntLoopQueue() {
        return intLoopQueue;
    }
}