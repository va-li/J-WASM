package benchmark;

import environment.Module;
import interpreter.WasmInterpreter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.BinaryParser;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Benchmarker {
    private static final Logger LOG = LoggerFactory.getLogger(Benchmarker.class);
    private static final int BENCHMARK_ROUNDS_FACTORIAL = 100;
    private static final int BENCHMARK_ROUNDS_FIBONACCI = 5;

    public static void main(String[] args) {
        printBenchmarkResults(
            "factorial.wasm",
            benchmarkMultipleExecution("factorial.wasm", BENCHMARK_ROUNDS_FACTORIAL)
        );
        printBenchmarkResults(
            "fibonacci.wasm",
            benchmarkMultipleExecution("fibonacci.wasm", BENCHMARK_ROUNDS_FIBONACCI)
        );
    }

    private static double benchmarkSingleExecution(String filename) {
        WasmInterpreter interpreter;
        int[] args = new int[]{};
        try {
            BinaryParser factorialParser = new BinaryParser();
            Module module = factorialParser.parse(new File(filename));
            interpreter = new WasmInterpreter(module);

        } catch (IOException e) {
            LOG.error("Error while parsing '" + filename + "' for benchmark: " + e.getMessage());
            throw new RuntimeException(e);
        }

        double startTime = System.nanoTime();
        interpreter.execute(args, false);
        return secondsBetween(startTime, System.nanoTime());
    }

    private static double[] benchmarkMultipleExecution(String filename, int passes) {
        double[] values = new double[passes];
        System.out.println("'" + filename + "' - Benchmark start");
        for (int i = 0; i < passes; i++) {
            values[i] = benchmarkSingleExecution(filename);
        }
        return values;
    }

    private static void printBenchmarkResults(String filename, double[] values) {
        DecimalFormat df = new DecimalFormat("#.#########");
        DescriptiveStatistics statistics = new DescriptiveStatistics(values);
        System.out.print(
            "'" + filename + "' - Benchmark results (in seconds)\n" +
            "\t iterations: " + values.length + "\n" +
            "\t min: " + df.format(statistics.getMin()) + "\n" +
            "\t max: " + df.format(statistics.getMax()) + "\n" +
            "\t mean: " + df.format(statistics.getMean()) + "\n" +
            "\t median: " + df.format(median(values)) + "\n");
    }

    private static double median(double[] values) {
        double[] sortedValues = Arrays.copyOf(values, values.length);
        Arrays.sort(sortedValues);
        if (sortedValues.length % 2 != 0) {
            int medianIndex = (int) Math.ceil(((double) sortedValues.length) / 2d);
            return sortedValues[medianIndex];
        } else {
            int index = sortedValues.length / 2;
            return (sortedValues[index] + sortedValues[index + 1]) / 2d;
        }
    }

    /**
     * Calculates the difference between start and end in seconds
     * @param start time in nanoseconds
     * @param end tim in nanoseconds
     */
    private static double secondsBetween(double start, double end) {
        final long ONE_BILLION = 1000000000;
        return (end - start) / ONE_BILLION;
    }
}
