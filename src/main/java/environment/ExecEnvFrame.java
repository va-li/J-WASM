package environment;

import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class ExecEnvFrame {

    private final Function function;
    private final Integer[] localVariables;

    private long instructionPointer = 0;

    private int ifDepth = 0;
    private int operandStackBase = 0;
    private boolean ifBranch = true;
    private boolean skipCode = false;

    private final Stack<EndValue> endStack = new Stack<>();

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

    enum EndValue {
        LOOP,
        IF,
        BLOCK
    }
}