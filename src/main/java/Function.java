/**
 * Created by Valentin
 * TODO documentation
 */
public class Function {

    private final long firstInstructionLine;
    private final int parameterCount;
    private final int returnValueCount;

    public Function(long firstInstructionLine, int parameterCount, int returnValueCount) {
        this.firstInstructionLine = firstInstructionLine;
        this.parameterCount = parameterCount;
        this.returnValueCount = returnValueCount;
    }

    public long getFirstInstructionLine() {
        return firstInstructionLine;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public int getReturnValueCount() {
        return returnValueCount;
    }
}
