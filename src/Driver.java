import java.io.IOException;
import java.util.concurrent.*;

/**
 * Main Class Driver
 */
public class Driver {
    static int disk_size = 2048;
    static int ram_size = 1024;
    static int cache_size = 128;
    static int job_count = 30;
    static int thread_delay = 0;
    

    public static void main(String[] args) throws InterruptedException, IOException, BrokenBarrierException {
        MetricCollector.printInfo();
        exec(1, Scheduler.scheduler.FIFO);
        exec(1, Scheduler.scheduler.PRIORITY);
    }

    /**
     * Reset the simulation for another run.
     * @param policy The policy of the scheduler (FIFO vs PRIORITY).
     */
    static void reset(Scheduler.scheduler policy) {
        Scheduler.instructions.clear();
        Scheduler.jobs.clear();
        Scheduler.policy = policy;
        Loader.load();
        MMU.init();
    }

    /**
     * Execution method of a run of the simulation.
     * Starts all threads and initializes components.
     * @param cores Number of cores to run the simulation with.
     * @param policy The policy of the scheduler (FIFO vs PRIORITY).
     * @throws IOException When MetricCollector cannot write to files.
     * @throws InterruptedException When ExecutorService cannot execute threads.
     */
    static void exec(int cores, Scheduler.scheduler policy) throws IOException, InterruptedException {
        // Reset simulation
        reset(policy);

        // Initialize MetricCollector to a new file
        MetricCollector.init(policy.toString() + ".csv");

        // Create CPU threads without starting
        for (int i = 0; i < cores; i++) {
            CPU cpu = new CPU(i);
            Scheduler.addCpu(cpu);
        }

        // Use ExecutorService to start threads all at once
        ExecutorService executorService = Executors.newCachedThreadPool();
        MetricCollector.start_time(System.currentTimeMillis());
        for (CPU cpu : Scheduler.instructions) {
            executorService.execute(cpu);
        }

        // Wait for all threads to complete
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        MetricCollector.print_metrics();
    }
}
