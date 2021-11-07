import java.io.*;

public class MetricCollector {

    static BufferedWriter bufferedWriter;
    static long globalStartTime;

    /**
     * Initialize writer to a new file for the simulation.
     * @param filename Name of the file.
     * @throws IOException When file cannot be created.
     */
    static void init(String filename) throws IOException {
        File metrics = new File("./metrics/" + filename);
        bufferedWriter = new BufferedWriter(new PrintWriter(metrics));
    }

    /**
     * Print information regarding data values to a readme file.
     * @throws IOException When file cannot be written to.
     */


    /**
     * Write the individual Job Metrics to the file.
     * @throws IOException When file cannot be written to.
     */
    static void listJobMetrics() throws IOException {
        bufferedWriter.write("# Job Metrics\n");
        bufferedWriter.write("Job ID,Waiting Time,Completion Time,I/O Processes,MMU RAM % Used,Job RAM % Used,Job Cache % Used\n");
        for (PCB job : Scheduler.jobs) {
            long waitingTime = job.getStartTime() - globalStartTime;
            double mmuPercentRam = (double) Math.round((double) job.getRamUsage() / Driver.ram_size * 1000) / 1000;
            double jobPercentRam = (double) Math.round((double) job.getTotalSize() / Driver.ram_size * 1000) / 1000;
            double jobPercentCache = (double) Math.round((double) job.getCacheUsage() / Driver.cache_size * 1000) / 1000;
            bufferedWriter.write(job.getJobId() + "," + waitingTime + "," +
                    job.getCompletionTime() + "," + job.getNumIoProcesses() + "," + mmuPercentRam + "," + jobPercentRam
                    + "," + jobPercentCache + "\n");
        }
    }

    static void print_metrics() throws IOException {
        listJobMetrics();
        bufferedWriter.newLine();
        close();
    }

    static void start_time(long time) {
        globalStartTime = time;
    }

    static void close() throws IOException {
        bufferedWriter.close();
    }
}
