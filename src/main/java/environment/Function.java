package environment;

/**
 * Created by Valentin
 * TODO documentation
 */
public class Function {

    private final byte[] instructions;
    private final int parameterCount;
    private final int returnValueCount;
    private final int localVariableCount;

    public Function(byte[] instructions, int parameterCount, int returnValueCount,
        int localVariableCount) {
        this.instructions = instructions;
        this.parameterCount = parameterCount;
        this.returnValueCount = returnValueCount;
        this.localVariableCount = localVariableCount;
    }

    public byte[] getInstructions() {
        return instructions;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public int getReturnValueCount() {
        return returnValueCount;
    }

    public int getLocalVariableCount() {
        return localVariableCount;
    }
}
