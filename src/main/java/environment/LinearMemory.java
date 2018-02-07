package environment;

import constants.ImplementationSpecific;
import constants.SpecificationValueBoundaryViolationException;
import constants.WebAssemblySpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
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
        this(initialPageCount, 1);
    }

    public LinearMemory(int initialPageCount, int maxPageCount) {
        if (maxPageCount > PAGE_COUNT_MAX) {
            throw new SpecificationValueBoundaryViolationException("Maximum Linear Memory page count must not be" +
                "greater than " + PAGE_COUNT_MAX);
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
     * (number of byte in i32) signextends it to 4 bytes.
     * ATTENTION: Currently the alignment is ignored, as the WebAssembly Specification lists it as a hint
     * {@see https://github.com/WebAssembly/design/blob/master/Rationale.md#alignment-hints}
     * @param address the index (starting at zero) into the linear memory specifying the starting point for the load
     * @param offset added to the address to get the actual load address
     * @param alignment ignored
     * @param byteCount the number of bytes to read, at most 4
     * @param isSigned
     * @return the value read from linear memory, possibly sign-extended
     */
    public long load(int address, int offset, int alignment, byte byteCount, boolean isSigned) {
        validateBoundsOrThrowException(address, offset, byteCount);

        int effectiveAddress = address + offset;
        int effectiveAddressPageOffsetEnd = (effectiveAddress + byteCount - 1) % PAGE_SIZE_BYTES;
        int pageNumber = effectiveAddress / PAGE_SIZE_BYTES;

        int value = 0;
        byte[] page = allocatedPages.get(pageNumber);
        for (int i = 0; i < byteCount; i++) {
            value >>= value;
            // value is stored little endian, but we need it as big endian
            value |= page[effectiveAddressPageOffsetEnd - i] << (8 * (4 - i - 1));

            if ((effectiveAddress + i + 1) % PAGE_SIZE_BYTES == 0) {
                // the next value is on the succeeding page
                page = allocatedPages.get(pageNumber);
            }
        }
        for (int i = byteCount - 1; i < 4; i++) {
            if (isSigned) {
                value >>>= value;
            } else {
                value >>= value;
            }
        }
        return value;
    }

    /**
     * Stores <code>byteCount</code> number of bytes containg <code>value</code> at <code>address + offset</code> in
     * linear memory and possibly wraps it if <code>byteCount</code> is smaller than 4 (number of bytes in i32).
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
        // TODO
    }

    private void validateBoundsOrThrowException(int address, int offset, int byteCount) {
        int biggestValidIndex = (allocatedPages.size() * PAGE_SIZE_BYTES) - 1;
        int biggestAccessedIndex = (address + offset + byteCount - 1);

        if (biggestAccessedIndex > biggestValidIndex) {
            throw new IndexOutOfBoundsException("Linear memory access is out of bounds! [value: "
                + biggestAccessedIndex + "; max allowed: " + biggestValidIndex);
        }
    }
}
