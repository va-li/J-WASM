package parser;

import environment.Module;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public interface Parser {

    /**
     * Parses the given InputStream until EOF or an error occured
     */
    Module parse(File file) throws ParserException, IOException;
}