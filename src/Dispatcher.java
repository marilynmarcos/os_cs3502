//Dispatcher
class Dispatcher {

    //load job into cpu
    static synchronized void load_job(PCB job, CPU cpu) {
        // Load Instructions and data into RAM
        cpu.setcurrent_job(job);
        cpu.setregisters(job.getregisters());
        cpu.resetProgramCounter();
        job.setStartTime(System.currentTimeMillis());
        int total_size = job.getTotalSize();
        int diskStartIndex = job.getDiskStart();
        int ramStartIndex = accessRam(total_size, diskStartIndex);
        int ramEndIndex = ramStartIndex + total_size;
        job.setCurrrentCPU(cpu);
        job.setRamStart(ramStartIndex);
        job.setRamEnd(ramEndIndex);
        System.out.println(job);
    }

    /**
     * A synchronized method used to access RAM, the shared memory.
     * Synchronized methods only allow a single thread at a time to execute it. (Similar to Semaphore implementations).
     * @param total_size Total number of instructions and buffer size of the job.
     * @param diskStartIndex The location in disk of the job's first instruction.
     * @return The start index in RAM of an empty section big enough to hold the job's full instruction and data set.
     */
    static synchronized int accessRam(int total_size, int diskStartIndex) {
        int ramStartIndex = MMU.left(total_size);
        for(int i = ramStartIndex; i < ramStartIndex + total_size; i++) {
            MMU.store_ram(i, MMU.load_disk(diskStartIndex + i - ramStartIndex));
        }
        return ramStartIndex;
    }

    /**
     * Unload a job from a CPU.
     * @param job The job to be unloaded.
     * @param cpu The CPU the job will be unloaded from.
     */
    static void unload_job(PCB job, CPU cpu) {
        job.setCompletionTime(System.currentTimeMillis());
        job.setregisters(cpu.getregisters());
        cpu.setcurrent_job(null);
    }
}
