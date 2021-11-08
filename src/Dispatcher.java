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
        int ramStartIndex = access_ram(total_size, diskStartIndex);
        int ramEndIndex = ramStartIndex + total_size;
        job.setCurrrentCPU(cpu);
        job.setRamStart(ramStartIndex);
        job.setRamEnd(ramEndIndex);
        System.out.println(job);
    }

    static synchronized int access_ram(int total_size, int diskStartIndex) {
        int ramStartIndex = MMU.left(total_size);
        for(int i = ramStartIndex; i < ramStartIndex + total_size; i++) {
            MMU.store_ram(i, MMU.load_disk(diskStartIndex + i - ramStartIndex));
        }
        return ramStartIndex;
    }

    static void unload_job(PCB job, CPU cpu) {
        job.setCompletionTime(System.currentTimeMillis());
        job.setregisters(cpu.getregisters());
        cpu.setcurrent_job(null);
    }
}
