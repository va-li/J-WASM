package util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import parser.ParserException;

/**
 * Created by Valentin
 * TODO documentation
 */
public class Leb128 {
    /**
     * Reads an unsigned integer from {@code in}.
     * Thank you android dex for the source!
     * https://github.com/facebook/buck/blob/master/third-party/java/dx/src/com/android/dex/Leb128.java
     */
    public static int readUnsignedLeb128(ByteArrayInputStream in) {
        int result = 0;
        int cur;
        int count = 0;

        do {
            cur = in.read() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);

        if ((cur & 0x80) == 0x80) {
            throw new ParserException("invalid LEB128 sequence");
        }

        return result;
    }

    /**
     * Gets the number of bytes in the unsigned LEB128 encoding of the
     * given value.
     *
     * @param value the value in question
     * @return its write size, in bytes
     */
    public static int unsignedLeb128Size(int value) {
        // TODO: This could be much cleverer.
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }
}
