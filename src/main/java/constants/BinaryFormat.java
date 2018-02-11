package constants;

/**
 * Stores WebAssembly's byte constants.
 * <p>
 * [WASM] = WebAssembly Specification 1.0 (April 27, 2017)
 */
public final class BinaryFormat {

    private BinaryFormat() {
    }

    /**
     * [WASM 5.5.15]
     */
    public static final class Module {

        /**
         * Least significant byte at index 0.
         */
        public static final byte[] MAGIC = {0x00, 0x61, 0x73, 0x6D};

        /**
         * Least significant byte at index 0.
         */
        public static final byte[] VERSION = {0x01, 0x00, 0x00, 0x00};

        /**
         * [WASM 5.5.2]
         */
        public static final class Section {

            public static final class Custom {

                public static final byte ID = 0x00;
            }

            public static final class Type {

                public static final byte ID = 0x01;
            }

            public static final class Import {

                public static final byte ID = 0x02;
            }

            public static final class Function {

                public static final byte ID = 0x03;
            }

            public static final class Table {

                public static final byte ID = 0x04;
            }

            public static final class Memory {

                public static final byte ID = 0x05;
            }

            public static final class Global {

                public static final byte ID = 0x06;
            }

            public static final class Export {

                public static final byte ID = 0x07;
                public static final byte DESC_FUNC = 0x00;
                public static final byte DESC_TABLE = 0x01;
                public static final byte DESC_MEM = 0x02;
                public static final byte DESC_GLOBAL = 0x03;
            }

            public static final class Start {

                public static final byte ID = 0x08;
            }

            public static final class Element {

                public static final byte ID = 0x09;
            }

            public static final class Code {

                public static final byte ID = 0x0A;
            }

            public static final class Data {

                public static final byte ID = 0x0B;
            }
        }
    }

    /**
     * [WASM 5.3]
     */
    public static final class Types {

        public static final class ValueType {

            /**
             * Denotes i32 value [WASM 5.3.1]
             */
            public static final byte I32 = 0x7F;

            /**
             * Denotes i64 value [WASM 5.3.1]
             */
            public static final byte I64 = 0x7E;

            /**
             * Denotes f32 value [WASM 5.3.1]
             */
            public static final byte F32 = 0x7D;

            /**
             * Denotes f64 value [WASM 5.3.1]
             */
            public static final byte F64 = 0x7C;
        }

        /**
         * Denotes result of block as empty [WASM 5.3.2]
         */
        public static final byte RESULT_TYPE_EMPTY = 0x40;
        /**
         * Denotes beginning of function definition [WASM 5.3.3]
         */
        public static final byte FUNCTION_TYPE = 0x60;
    }

    /**
     * [WASM 5.4]
     */
    public static final class Instructions {

        /**
         * [WASM 5.4.1]
         */
        public static final class Control {

            public static final byte UNREACHABLE = 0x00;
            public static final byte NOP = 0x01;
            public static final byte BLOCK = 0x02;
            public static final byte LOOP = 0x03;
            public static final byte BR_IF = 0x0D;
            public static final byte IF = 0x04;
            public static final byte ELSE = 0x05;
            public static final byte END = 0x0B;
            public static final byte RETURN = 0x0F;
            public static final byte CALL = 0x10;
        }

        /**
         * [WASM 5.4.3]
         */
        public static final class Variable {

            public static final byte GET_LOCAL = 0x20;
            public static final byte SET_LOCAL = 0x21;
            public static final byte TEE_LOCAL = 0x22;
        }

        /**
         * [WASM 5.4.4]
         */
        public static final class Memory {

            public static final byte I32_LOAD = 0x28;
            public static final byte I32_LOAD8_S = 0x2C;
            public static final byte I32_LOAD8_U = 0x2D;
            public static final byte I32_LOAD16_S = 0x2E;
            public static final byte I32_LOAD16_U = 0x2F;

            public static final byte I32_STORE = 0x36;
            public static final byte I32_STORE8 = 0x3A;
            public static final byte I32_STORE16 = 0x3B;

            /**
             * Tailing 0x00 omitted. See [WASM 2.4.4]
             */
            public static final byte CURRENT_MEMORY = 0x3F;
            /**
             * Tailing 0x00 omitted. See [WASM 2.4.4]
             */
            public static final byte GROW_MEMORY = 0x40;
        }

        /**
         * [WASM 5.4.5]
         */
        public static final class Numeric {

            public static final byte I32_CONST = 0x41;

            public static final byte I32_EQZ = 0x45;
            public static final byte I32_EQ = 0x46;
            public static final byte I32_NE = 0x47;
            public static final byte I32_LT_S = 0x48;
            public static final byte I32_LT_U = 0x49;
            public static final byte I32_GT_S = 0x4A;
            public static final byte I32_GT_U = 0x4B;
            public static final byte I32_LE_S = 0x4C;
            public static final byte I32_LE_U = 0x4D;
            public static final byte I32_GE_S = 0x4E;
            public static final byte I32_GE_U = 0x4F;

            public static final byte I32_CLZ = 0x67;
            public static final byte I32_CTZ = 0x68;
            public static final byte I32_POPCNT = 0x69;
            public static final byte I32_ADD = 0x6A;
            public static final byte I32_SUB = 0x6B;
            public static final byte I32_MUL = 0x6C;
            public static final byte I32_DIV_S = 0x6D;
            public static final byte I32_DIV_U = 0x6E;
            public static final byte I32_REM_S = 0x6F;
            public static final byte I32_REM_U = 0x70;
            public static final byte I32_AND = 0x71;
            public static final byte I32_OR = 0x72;
            public static final byte I32_XOR = 0x73;
            public static final byte I32_SHL = 0x74;
            public static final byte I32_SHR_S = 0x75;
            public static final byte I32_SHR_U = 0x76;
            public static final byte I32_ROTL = 0x77;
            public static final byte I32_ROTR = 0x78;
        }
    }
}
