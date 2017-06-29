package parser;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public interface Parser {

    /**
     * Parses the given InputStream until EOF or an error occured
     */
    void parse(File file, int[] executable) throws ParserException, IOException;
}