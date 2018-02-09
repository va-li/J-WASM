package environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static constants.ImplementationSpecific.LinearMemory.PAGE_COUNT_MAX;
import static constants.WebAssemblySpecification.LinearMemory.PAGE_SIZE_BYTES;

/**
 * A linear memory is a contiguous, byte-addressable range of memory spanning from offset 0 and extending up to a
 * varying memory size. This size is always a multiple of the WebAssembly page
 * {@see WebAssemblySpecification.LinearMemory.PAGE_SIZE_BYTES}.
 */
public class LinearMemory {
    private static Logger LOG = LoggerFactory.getLogger(LinearMemory.class);

    /**
     * Maximum number of pages allowed for this linear memory instance
     */
    private int maxPageCount = PAGE_COUNT_MAX;

    /**
     * List of pages (byte arrays) that is "allocated". allocatedPages.size() must always be less or equal maxPageCount
     */
    private List<byte[]> allocatedPages = new ArrayList<>();

    public LinearMemory(int initialPageCount) {
        this(initialPageCount, PAGE_COUNT_MAX);
    }

    public LinearMemory(int initialPageCount, int maxPageCount) {
        if (maxPageCount > PAGE_COUNT_MAX) {
            throw new IllegalArgumentException("Maximum Linear Memory page count must not be" +
                "greater than " + PAGE_COUNT_MAX);
        }
        if (maxPageCount < 1) {
            throw new IllegalArgumentException("Maximum Linear Memory page count cannot be less than 1");
        }

        this.maxPageCount = maxPageCount;
        allocatePages(initialPageCount);
    }

    /**
     * Queries the size of the memory and returns the number of linear memory pages currently allocated.
     * @return number of linear memory pages currently allocated.
     */
    public int currentMemory() {
        return allocatedPages.size();
    }

    /**
     * Grows linear memory by a given unsigned delta of pages.
     * @param deltaPages the number of pages that should be allocated additionally to the already allocated pages
     * @return the previous memory size in units of pages or -1 on failure
     */
    public int growMemory(int deltaPages) {
        if (allocatedPages.size() + deltaPages > maxPageCount) {
            return -1;
        }

        int oldPageCount = allocatedPages.size();
        allocatePages(deltaPages);

        return oldPageCount;
    }

    private void allocatePages(int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            allocatedPages.add(new byte[PAGE_SIZE_BYTES]);
        }
    }

    /**
     * Loads <code>byteCount</code> number of bytes at <code>address + offset</code> from linear memory, interpretes it
     * as little endian, if <code>isSigned</code> is <code>true</code> and byteCount is smaller than 4
     * (number of byte in i32) signextends it to 4 bytes. <code>address</code> and <code>offset</code> are interpreted
     * as unsigned values!
     * ATTENTION: Currently the alignment is ignored, as the WebAssembly Specification lists it as a hint
     * {@see https://github.com/WebAssembly/design/blob/master/Rationale.md#alignment-hints}
     * @param address the index (starting at zero) into the linear memory specifying the starting point for the load
     * @param offset added to the address to get the actual load address
     * @param alignment ignored
     * @param byteCount the number of bytes to read, at most 4
     * @param isSigned determines wether the returned value is to be interpreted as signed or unsigned and sing-extend
     *                 it if byteCount is smaller than 4
     * @return the value read from linear memory, possibly sign-extended
     */
    public int load(int address, int offset, int alignment, int byteCount, boolean isSigned) {
        validateBoundsOrThrowException(address, offset, byteCount);

        /* WebAssembly interpretes the address and offset as unsigned values and their sum as infinite precision
         * unsigned value, therefor we need the following conversions to get the right page and offset in that page */

        long effectiveAddressUnsigned = Integer.toUnsignedLong(address) + Integer.toUnsignedLong(offset);

        // expects PAGE_SIZE_BYTES <= Integer.MAX_VALUE
        int smallestPageOffset =
            (int) (effectiveAddressUnsigned % Integer.toUnsignedLong(PAGE_SIZE_BYTES));

        // expects PAGE_SIZE_BYTES >= 2
        int pageNumber = (int) (effectiveAddressUnsigned / Integer.toUnsignedLong(PAGE_SIZE_BYTES));
        byte[] page = allocatedPages.get(pageNumber);

        final int bytesInI32 = 4;
        ByteBuffer b = ByteBuffer.allocate(bytesInI32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        final byte msbMask = (byte) 0x80;
        byte msb = 0;
        for (int i = 0; i < byteCount; i++) {
            int currentPageOffset = (smallestPageOffset + i) % PAGE_SIZE_BYTES;
            b.put(page[currentPageOffset]);
            msb = (byte) (page[currentPageOffset] & msbMask);

            int nextPageOffset = (currentPageOffset + 1) % PAGE_SIZE_BYTES;
            if (nextPageOffset < currentPageOffset) {
                // We've red the last value on this page, the next value is on the preceeding page
                page = allocatedPages.get(pageNumber + 1);
            }
        }

        for (int i = byteCount; i < bytesInI32; i++) {
            if (isSigned && msb != 0) {
                b.put((byte) 0xFF);
            }
        }

        return b.getInt(0);
    }

    /**
     * Stores <code>byteCount</code> number of bytes containg <code>value</code> at <code>address + offset</code> in
     * linear memory and possibly wraps it if <code>byteCount</code> is smaller than 4 (number of bytes in i32).
     * <code>address</code> and <code>offset</code> are interpreted as unsigned values!
     * ATTENTION: Currently the alignment is ignored, as the WebAssembly Specification lists it as a hint
     * {@see https://github.com/WebAssembly/design/blob/master/Rationale.md#alignment-hints}
     * @param address the index (starting at zero) into the linear memory specifying the starting point for the store
     * @param offset added to the address to get the actual store address
     * @param alignment ignored
     * @param byteCount the number of bytes to store, at most 4
     * @param value the value to store, possibly wrapped
     */
    public void store(int address, int offset, int alignment, byte byteCount, int value) {
        validateBoundsOrThrowException(address, offset, byteCount);

        /* WebAssembly interpretes the address and offset as unsigned values and their sum as infinite precision
         * unsigned value, therefor we need the following conversions to get the right page and offset in that page */

        long effectiveAddressUnsigned = Integer.toUnsignedLong(address) + Integer.toUnsignedLong(offset);

        // expects PAGE_SIZE_BYTES <= Integer.MAX_VALUE
        int smallestPageOffset =
            (int) ((effectiveAddressUnsigned) % Integer.toUnsignedLong(PAGE_SIZE_BYTES));

        // expects PAGE_SIZE_BYTES >= 2
        int pageNumber = (int) (effectiveAddressUnsigned / Integer.toUnsignedLong(PAGE_SIZE_BYTES));
        byte[] page = allocatedPages.get(pageNumber);

        final int bytesInI32 = 4;
        ByteBuffer b = ByteBuffer.allocate(bytesInI32);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(value);

        byte[] data = b.array();
        for (int i = 0; i < byteCount; i ++) {
            int currentPageOffset = (smallestPageOffset + i) % PAGE_SIZE_BYTES;

            page[currentPageOffset] = data[i];

            int nextPageOffset = (currentPageOffset + 1) % PAGE_SIZE_BYTES;
            if (nextPageOffset < currentPageOffset) {
                // We've red the last value on this page, the next value is on the preceeding page
                page = allocatedPages.get(pageNumber + 1);
            }
        }
    }

    private void validateBoundsOrThrowException(int address, int offset, int byteCount) {
        long addressUnsigned = Integer.toUnsignedLong(address);
        long offestUnsigned = Integer.toUnsignedLong(offset);
        long byteCountUnsigned = Integer.toUnsignedLong(byteCount);

        // (- 1) becaus those are indices starting at 0
        long biggestValidIndex = (allocatedPages.size() * PAGE_SIZE_BYTES) - 1;
        long biggestAccessedIndex = addressUnsigned + offestUnsigned + byteCountUnsigned - 1;

        if ((addressUnsigned + offestUnsigned) < 0) {
            throw new IndexOutOfBoundsException("Linear memory access is out of bounds! [value: "
                + (address + offset) + "; min allowed: 0");
        }

        if (biggestAccessedIndex > biggestValidIndex) {
            throw new IndexOutOfBoundsException("Linear memory access is out of bounds! [value: "
                + biggestAccessedIndex + "; max allowed: " + biggestValidIndex);
        }
    }
}
