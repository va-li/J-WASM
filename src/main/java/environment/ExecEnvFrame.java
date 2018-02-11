package environment;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class ExecEnvFrame {

    private final Function function;
    private final Integer[] localVariables;

    private long instructionPointer = 0;
    private long loopBeginInstructionPointer = 0;

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

    public long getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(long instructionPointer) {
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

    public long getLoopBeginInstructionPointer() {
        return loopBeginInstructionPointer;
    }

    public void setLoopBeginInstructionPointer(long loopBeginInstructionPointer) {
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

    enum EndValue {
        LOOP,
        IF,
        BLOCK
    }
}