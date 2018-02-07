package constants;

/**
 * This class contains constants that are set and dictated by the WebAssembly specification.
 */
public final class WebAssemblySpecification {
    public static final class LinearMemory {
        /**
         * The size of one linear memory page in bytes. This is fixed at 64KiB (64 * 2^10) by the WebAssembly Specification.
         */
        public static final int PAGE_SIZE_BYTES = 65536;
    }
}
