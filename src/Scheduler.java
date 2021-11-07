import java.util.*;

/**
 * Schedules jobs with FIFO or Priority scheduling policy.
 */
public class Scheduler {

    public static scheduler policy;
    
    /**
     * Load 30 jobs into an array
     */
    public static final ArrayList<PCB> jobs = new ArrayList<>();

    /**
     * Load instruction set into an array
     */
    public static ArrayList<CPU> instructions = new ArrayList<>();

    private static final PriorityQueue<PCB> priority_queue = new PriorityQueue<>();
    private static final LinkedList<PCB> fifo_queue = new LinkedList<>();

    /**
     * Add job
     */
    static void addJob(PCB job) {
        job.setAddedTime(System.currentTimeMillis());
        jobs.add(job);
        if (policy == scheduler.PRIORITY) {
            priority_queue.add(job);
        } else {
            fifo_queue.add(job);
        }
    }

    /**
     * Add a CPU to the list of CPUs.
     * Right now we only have one CPU, but part 2 we will need 4.
     */
    static void addCpu(CPU cpu) {
        instructions.add(cpu);
    }

    /**
     * Determine if there is a remaining job in the queue.
     */
    static synchronized boolean hasNext() {
        PCB nextJob;
        if (policy == scheduler.PRIORITY) {
            nextJob = priority_queue.peek();
        } else {
            nextJob = fifo_queue.peek();
        }
        return nextJob == null;
    }


    static synchronized PCB nextJob() {
        PCB nextJob;
        if (policy == scheduler.PRIORITY) {
            nextJob = priority_queue.poll();
        } else {
            nextJob = fifo_queue.poll();
        }

        if (nextJob != null) {
            nextJob.setJobState(PCB.JobState.RUNNING);
        }
        return nextJob;
    }

    /**
     * Signal an interrupt on a CPU.
     * @param cpu The CPU to be interrupted.
     */
    static void signalInterrupt(CPU cpu) {
        cpu.setHasInterrupt(true);
    }

    /**
     * Handle an interrupt on a CPU. Transitions the running job to a blocked state, and add it back to the queue.
     * @param job The Job that is interrupted.
     */
    static void handleInterrupt(PCB job) {
        job.setJobState(PCB.JobState.BLOCKED);
        addJob(job);
    }

    /**
     * An enum that holds possible scheduler policys.
     */
    public enum scheduler {
        FIFO,
        PRIORITY
    }
}
