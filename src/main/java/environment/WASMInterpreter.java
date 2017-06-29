package environment;

import java.util.List;
import java.util.Stack;

/**
 * Created by Valentin
 * TODO documentation
 */
public class WASMInterpreter {
    private Stack<CallStackFrame> callStack;
    private Stack<Integer> operandStack;

    private List<Function> functions;
    private int startFunctionIndex;

    private long instructionPointer;

    public WASMInterpreter(List<Function> functions, int startFunctionIndex) {
        this.functions = functions;
        this.startFunctionIndex = startFunctionIndex;
    }

    public void execute() {
        Function executingFunction = null;
    }
}
