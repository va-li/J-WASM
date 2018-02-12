package benchmark;

import environment.WasmInterpreter;
import parser.binary.BinaryParser;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by Valentin
 * TODO documentation
 */
public class Benchmarker {
    private static final int BENCHMARK_ROUNDS_LOOP = 1;
    private static final int BENCHMARK_ROUNDS_FACTORIAL = 10000;
    private static final int BENCHMARK_ROUNDS_FIBONACCI = 50;

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        try {
            File f = new File("loop3.wasm");
            for (int i = 0; i < BENCHMARK_ROUNDS_LOOP; i++) {
                BinaryParser factorialParser = new BinaryParser();
                new WasmInterpreter(factorialParser.parse(f)).execute(new int[]{});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double loopSeconds = ((double) (System.nanoTime() - startTime) / 1000000000);

        startTime = System.nanoTime();

        try {
            File f = new File("factorial.wasm");
            for (int i = 0; i < BENCHMARK_ROUNDS_FACTORIAL; i++) {
                BinaryParser factorialParser = new BinaryParser();
                new WasmInterpreter(factorialParser.parse(f)).execute(new int[]{});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double factSeconds = ((double) (System.nanoTime() - startTime) / 1000000000);

        startTime = System.nanoTime();
        try {
            File f = new File("fibonacci.wasm");
            for (int i = 0; i < BENCHMARK_ROUNDS_FIBONACCI; i++) {
                BinaryParser fibonacciParser = new BinaryParser();
                new WasmInterpreter(fibonacciParser.parse(f)).execute(new int[]{});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double fibSeconds = ((double) (System.nanoTime() - startTime) / 1000000000);
        System.out.println(BENCHMARK_ROUNDS_LOOP + " rounds of 'loop.wasm' ( just add ) took "
                + new DecimalFormat("#.##########").format(loopSeconds) + " Seconds");
        System.out.println(BENCHMARK_ROUNDS_FACTORIAL + " rounds of 'factorial.wasm' ( fac(20) ) took "
                + new DecimalFormat("#.##########").format(factSeconds) + " Seconds");
        System.out.println(BENCHMARK_ROUNDS_FIBONACCI + " rounds of 'fibonacci.wasm' ( fib(30) ) took "
                + new DecimalFormat("#.##########").format(fibSeconds) + " Seconds");
    }
}
