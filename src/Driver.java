import java.io.IOException;
import java.util.concurrent.*;

/**
 * Main Class Driver
 */
public class Driver {
    static int disk_size = 2048;
    static int ram_size = 1024;
    static int cache_size = 128;
    static int thread_delay = 0;
    

    public static void main(String[] args) throws InterruptedException, IOException, BrokenBarrierException {
        exec(1, Scheduler.scheduler.FIFO);
        exec(1, Scheduler.scheduler.PRIORITY);
    }

    static void reset(Scheduler.scheduler policy) {
        Scheduler.instructions.clear();
        Scheduler.jobs.clear();
        Scheduler.policy = policy;
        Loader.load();
        MMU.init();
    }


    static void exec(int cores, Scheduler.scheduler policy) throws IOException, InterruptedException {
        // Reset simulation
        reset(policy);

        // Initialize MetricCollector to a new file
        Metrics.init(policy.toString() + ".csv");

        // Create CPU threads without starting
        for (int i = 0; i < cores; i++) {
            CPU cpu = new CPU(i);
            Scheduler.addCpu(cpu);
        }

        // Use ExecutorService to start threads all at once
        ExecutorService executorService = Executors.newCachedThreadPool();
        Metrics.start_time(System.currentTimeMillis());
        for (CPU cpu : Scheduler.instructions) {
            executorService.execute(cpu);
        }

        // Wait for all threads to complete
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        Metrics.print_metrics();
    }
}
