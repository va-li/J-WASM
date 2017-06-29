import java.util.HashMap;

/**
 * Created by Valentin
 * TODO documentation
 */
public class WASMInterpreter {

    private HashMap<Integer, Function> functionTable;
    private CallStack callStack;
    private OperandStack operandStack;

    public WASMInterpreter(
        HashMap<Integer, Function> functionTable, CallStack callStack,
        OperandStack operandStack) {
        this.functionTable = functionTable;
        this.callStack = callStack;
        this.operandStack = operandStack;
    }

    public HashMap<Integer, Function> getFunctionTable() {
        return functionTable;
    }

    public CallStack getCallStack() {
        return callStack;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }
}
