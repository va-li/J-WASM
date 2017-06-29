import java.util.List;

/**
 * Created by Valentin
 * TODO documentation
 */
public class CallStackFrame {

    private final Function function;
    private final int[] localVariables;
    private long instructionPointer;

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

    public long getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(long instructionPointer) {
        this.instructionPointer = instructionPointer;
    }
}
