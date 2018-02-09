package benchmark;

import parser.binary.BinaryParser;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by Valentin
 * TODO documentation
 */
public class Benchmarker {
    private static int BENCHMARK_ROUNDS_FACTORIAL = 10000;
    private static int BENCHMARK_ROUNDS_FIBONACCI = 50;

    public static void main(String[] args) {

        long startTime = System.nanoTime();
        try {
            File f = new File("factorial.wasm");
            for (int i = 0; i < BENCHMARK_ROUNDS_FACTORIAL; i++) {
                BinaryParser factorialParser = new BinaryParser();
                factorialParser.parse(f);
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
                fibonacciParser.parse(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double fibSeconds = ((double) (System.nanoTime() - startTime) / 1000000000);
        System.out.println(BENCHMARK_ROUNDS_FACTORIAL + " rounds of 'factorial.wasm' ( fac(20) ) took "
            + new DecimalFormat("#.##########").format(factSeconds) + " Seconds");
        System.out.println(BENCHMARK_ROUNDS_FIBONACCI + " rounds of 'fibonacci.wasm' ( fib(30) ) took "
            + new DecimalFormat("#.##########").format(fibSeconds) + " Seconds");
    }
}
