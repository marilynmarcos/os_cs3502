import java.io.*;

public class Metrics {

    static long globalStartTime;

    static BufferedWriter bufferedWriter;

    //create new file to track metrics
    static void init(String filename) throws IOException {
        File metrics = new File("./metrics/" + filename);
        bufferedWriter = new BufferedWriter(new PrintWriter(metrics));
    }

    //formatting of job metrics
    static void listJobMetrics() throws IOException {
        bufferedWriter.write("# Job Metrics\n");
        //metrics from specification document
        bufferedWriter.write("Job ID,Waiting Time,Completion Time,I/O Processes,Job RAM % Used,Job Cache % Used\n");
        for (PCB job : Scheduler.jobs) {
            long waitingTime = job.getStartTime() - globalStartTime;
            double jobPercentRam = (double) Math.round((double) job.getTotalSize() / Driver.ram_size * 1000) / 1000;
            double jobPercentCache = (double) Math.round((double) job.getCacheUsage() / Driver.cache_size * 1000) / 1000;

            //if we use commas to seperate everything we can upload to excel to create graphs
            bufferedWriter.write(job.getJobId() + "," + waitingTime + "," +
                    job.getCompletionTime() + "," + job.getNumIoProcesses() + "," + jobPercentRam
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
