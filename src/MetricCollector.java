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
    static void printInfo() throws IOException {
        init("README.md");
        bufferedWriter.write("# JOB COMPLETION METRICS\n");
        bufferedWriter.write("Job ID: ID number of the job.\n");
        bufferedWriter.write("Waiting Time: Time in seconds that the job was waiting to be run.\n");
        bufferedWriter.write("Completion Time: Time in seconds that the job took to complete.\n");
        bufferedWriter.write("I/O Processes: Number of I/O processes that the job makes during its lifespan.\n");
        bufferedWriter.write("MMU RAM % Used: At the time the job is run, percentage of RAM used by all jobs.\n");
        bufferedWriter.write("Job RAM % Used: The percentage of RAM used by this job.\n");
        bufferedWriter.write("Job Cache % Used: The percentage of cache of the assigned cpu used by this job.\n");
        close();
    }

    /**
     * Print all Simulation Metrics to the file.
     * @throws IOException When file cannot be written to.
     */
    static void printAllMetrics() throws IOException {
        listJobMetrics();
        bufferedWriter.newLine();
        close();
    }

    /**
     * Write the individual Job Metrics to the file.
     * @throws IOException When file cannot be written to.
     */
    static void listJobMetrics() throws IOException {
        bufferedWriter.write("# JOB COMPLETION METRICS\n");
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


    /**
     * Close the buffered writer.
     * @throws IOException When the writer fails to close.
     */
    static void close() throws IOException {
        bufferedWriter.close();
    }

    static void setGlobalStartTime(long time) {
        globalStartTime = time;
    }
}
