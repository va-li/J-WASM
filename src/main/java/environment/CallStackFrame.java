package environment;

import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class CallStackFrame {

    private final Function function;
    private final int[] localVariables;
    private long instructionPointer;

    private int depth = 0;
    private int ifDepth = 0;
    private boolean ifBranch = true;
    private boolean skipCode = true;

    private final Stack<EndValue> endStack = new Stack<>();

    public CallStackFrame(Function function, int[] localVariables,
                          long instructionPointer) {
        this.function = function;
        this.localVariables = localVariables;
        this.instructionPointer = instructionPointer;
    }

    public Function getFunction() {
        return function;
    }

    public int[] getLocalVariables() {
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getIfDepth() {
        return ifDepth;
    }

    public void setIfDepth(int ifDepth) {
        this.ifDepth = ifDepth;
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

    enum EndValue {
        LOOP,
        IF,
        BLOCK
    }
}