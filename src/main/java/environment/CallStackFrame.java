package environment;

import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class CallStackFrame {

    private final Function function;
    private final long[] localVariables;

    private long instructionPointer = 0;

    private int ifDepth = 0;
    private int operandStackBase = 0;
    private boolean ifBranch = true;
    private boolean skipCode = false;

    private final Stack<EndValue> endStack = new Stack<>();

    public CallStackFrame(Function function, long[] localVariables) {
        this.function = function;
        this.localVariables = localVariables;
    }

    public Function getFunction() {
        return function;
    }

    public long[] getLocalVariables() {
        return localVariables;
    }

    public long getLocalVariableByIndex(int index) {
        return localVariables[index];
    }

    public void setLocalVariableByIndex(long value, int index) {
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

    enum EndValue {
        LOOP,
        IF,
        BLOCK
    }
}