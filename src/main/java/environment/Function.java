package environment;

/**
 * Created by Valentin
 * TODO documentation
 */
public class Function {

    private byte[] instructions;
    private int parameterCount;
    private int returnValueCount;
    private int localVariableCount;

    public Function(int parameterCount, int returnValueCount) {
        this.parameterCount = parameterCount;
        this.returnValueCount = returnValueCount;
    }

    public byte[] getInstructions() {
        return instructions;
    }

    public void setInstructions(byte[] instructions) {
        this.instructions = instructions;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    public int getReturnValueCount() {
        return returnValueCount;
    }

    public void setReturnValueCount(int returnValueCount) {
        this.returnValueCount = returnValueCount;
    }

    public int getLocalVariableCount() {
        return localVariableCount + parameterCount;
    }

    public void setLocalVariableCount(int localVariableCount) {
        this.localVariableCount = localVariableCount;
    }
}
