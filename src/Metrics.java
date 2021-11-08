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
    static void list_metrics() throws IOException {
        bufferedWriter.write("# Job Metrics\n");
        //metrics from specification document
        bufferedWriter.write("Job ID,Waiting Time,Completion Time,I/O Processes,Job RAM % Used,Job Cache % Used\n");
        for (PCB job : Scheduler.jobs) {
            long waiting_time = job.getStartTime() - globalStartTime;
            //calculate percentage of cache each job uses
            double job_cache = (double) Math.round((double) job.getCacheUsage() / Driver.cache_size * 1000) / 1000;
            //calculate percentage of ram each job uses
            double job_ram = (double) Math.round((double) job.getTotalSize() / Driver.ram_size * 1000) / 1000;

            //if we use commas to separate everything we can upload to excel to create graphs
            bufferedWriter.write(job.getJobId() + "," + waiting_time + "," +
                    job.getCompletionTime() + "," + job.getNumIoProcesses() + "," + job_ram
                    + "," + job_cache + "\n");
        }
    }

    static void print_metrics() throws IOException {
        list_metrics();
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
