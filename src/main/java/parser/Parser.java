package parser;

import environment.Function;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface Parser {

    /**
     * Parses the given InputStream until EOF or an error occured
     */
    List<Function> parse(File file) throws ParserException, IOException;
}