//FIFO and priority policies
import java.util.*;


public class Scheduler {

    public static scheduler policy;
    
    // Load 30 jobs into an array
    public static final ArrayList<PCB> jobs = new ArrayList<>();
    
     //Load instruction set into an array
    public static ArrayList<CPU> instructions = new ArrayList<>();

    private static final PriorityQueue<PCB> priority_queue = new PriorityQueue<>();
    private static final LinkedList<PCB> fifo_queue = new LinkedList<>();

    //Add job
    static void addJob(PCB job) {
        job.setAddedTime(System.currentTimeMillis());
        jobs.add(job);
        if (policy == scheduler.PRIORITY) {
            priority_queue.add(job);
        } else {
            fifo_queue.add(job);
        }
    }

    //Add a CPU to the list of CPUs.
    //Right now we only have one CPU, but part 2 we will need 4.
    static void add_CPU(CPU cpu) {
        instructions.add(cpu);
    }

    // Determine if there is a remaining job in the queue.
    static synchronized PCB next() {
        PCB next;
        if (policy == scheduler.PRIORITY) {
            next = priority_queue.poll();
        } else {
            next = fifo_queue.poll();
        }

        if (next != null) {
            next.setJobState(PCB.JobState.RUNNING);
        }
        return next;
    }
    static synchronized boolean has_next() {
        PCB next;
        if (policy == scheduler.PRIORITY) {
            next = priority_queue.peek();
        } else {
            next = fifo_queue.peek();
        }
        return next == null;
    }
    
    public enum scheduler {
        FIFO,
        PRIORITY
    }
}
