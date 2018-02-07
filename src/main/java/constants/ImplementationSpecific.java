package constants;

/**
 * This class contains constants that are set for this specific WebAssembly interpreter implementation and are not
 * specified or dictated by the WebAssembly specification.
 */
public final class ImplementationSpecific {
    public static final class LinearMemory {
        /**
         * The maximum number of pages to be allocated for linear memory.
         * This corresponds to a maximum of 8MiB of linear memory given a page size of 64KiB.
         */
        public static final int PAGE_COUNT_MAX = 128;
    }
}
