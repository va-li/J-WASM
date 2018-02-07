package environment;

import java.util.List;

/**
 * A module distributable, loadable, and executable unit of code in WebAssembly.
 */
public class Module {
    private LinearMemory linearMemory;
    private List<Function>  functions;
    private int startFunctionIndex;

    public Module(int initialLocalMemoryPageCount, List<Function> functions, int startFunctionIndex) {
        this.linearMemory = new LinearMemory(initialLocalMemoryPageCount);
        this.functions = functions;
        this.startFunctionIndex = startFunctionIndex;
    }

    public LinearMemory getLinearMemory() {
        return linearMemory;
    }

    public void setLinearMemory(LinearMemory linearMemory) {
        this.linearMemory = linearMemory;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public int getStartFunctionIndex() {
        return startFunctionIndex;
    }

    public void setStartFunctionIndex(int startFunctionIndex) {
        this.startFunctionIndex = startFunctionIndex;
    }
}
