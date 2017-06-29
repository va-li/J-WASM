/**
 * Created by Valentin
 * TODO documentation
 */
public class Function {

    private final long firstInstructionLine;
    private final int argumentCount;

    public Function(long firstInstructionLine, int argumentCount) {
        this.firstInstructionLine = firstInstructionLine;
        this.argumentCount = argumentCount;
    }

    public long getFirstInstructionLine() {
        return firstInstructionLine;
    }

    public int getArgumentCount() {
        return argumentCount;
    }
}
