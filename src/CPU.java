import java.math.BigInteger;
import java.util.Arrays;


// This class handles the execution of instructions read from the memory.
public class CPU extends Thread {

    // CPU Identification Info
    private PCB current_job;
    private cpu_state cpu_state;

    // Determine opcode by indexing opcd
    private final String[] opcd = {"RD", "WR", "ST", "LW", "MOV", "ADD", "SUB", "MUL", "DIV", "AND",
            "OR", "MOVI", "ADDI", "MULI", "DIVI", "LDI", "SLT", "SLTI", "HLT", "NOP", "JMP", "BEQ",
            "BNE", "BEZ", "BNZ", "BGZ", "BLZ"};

    // bin instructions
    private String bin;
    private String address;
    private int reg1_index;
    private int reg2_index;
    private int reg3_index;
    private int addressIndex;

    // Memory
    private Register[] register = new Register[16];
    private final String[] cache = new String[Driver.cache_size];

    // Program continuation variables
    private int pc;
    private boolean continueExec = true;
    private boolean interrupt = false;

    // Metrics
    private final long start;
    private long completion;
    private int ioProcesses = 0;
    private int jobCount;

    public CPU (int id) {
        this.start = System.currentTimeMillis();
        this.cpu_state = cpu_state.FREE;
    }


    // This method loads an instruction at a specified address from RAM.
    private String fetch(int index) {
        return cache[index].substring(0, 8);
    }

    // Decode a bin string of instructions.
    // Chars 0-1 indicate instruction format.
    // Chars 2-7 indicate instruction OPCD.
    // Chars 8-32 indicate additional register, addresses, etc.
    private void decode(String hex) {
        StringBuilder binString = new StringBuilder();
        binString.append(new BigInteger(hex, 16).toString(2));
        // Adds leading zeros if bin string is less than 32 chars long
        while (binString.toString().length() < 32) {
            binString.insert(0, "0");
        }
        bin = binString.toString();

        // Chars 0-1 indicate type of instruction (arithmetic, conditional, etc)
        instructionFormat(bin.substring(0, 2));

        // Chars 2-7 specify opcode of action. This is converted to decimal and used to index opcd
        String opcodeBinary = bin.substring(2, 8);
        evaluate(opcd[Integer.parseInt(opcodeBinary, 2)]);
    }

    // Main thread execution of the CPU class. Each CPU will independently check for remaining jobs and execute them accordingly.
    @Override
    public void run() {
        // Checking if job completed successfully
        while (!Scheduler.hasNext()) {
            PCB nextJob = Scheduler.nextJob();
            if (nextJob != null) {
                jobCount++;
                cpu_state = cpu_state.EXECUTING;
                Dispatcher.load_job(nextJob, this);
                nextJob.setRamUsage(MMU.ram_usage());
                loadInstructionsToCache();
                nextJob.setCacheUsage(getCacheUsage());
                while (continueExec && pc < current_job.getNumberofInstructions()) {
                    // Artificial exec time for each instruction
                    try {
                        Thread.sleep(Driver.thread_delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String hex = fetch(pc);
                    pc++;
                    current_job.incrementProgramCounter();
                    decode(hex);
                }
                MMU.clear_all(current_job.getRamStart(), current_job.getRamEnd());
                Dispatcher.unload_job(current_job, this);
                cpu_state = cpu_state.FREE;
                clearCache();
            }
        }

        completion = System.currentTimeMillis();
    }

    // Getter/setter methods
    void resetProgramCounter() {
        this.pc = 0;
        this.continueExec = true;
    }
    void setcurrent_job(PCB job) {
        this.current_job = job;
    }
    PCB getcurrent_job() {
        return current_job;
    }
    void setHasInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
    void setregisters(Register[] register) {
        this.register = register;
    }
    Register[] getregisters() {
        return register;
    }
    public long getCompletionTime() {
        return completion - start;
    }
    public int getIoProcesses() {
        return ioProcesses;
    }
    public int getJobCount() {
        return jobCount;
    }

    // Clear cache array
    void clearCache() {
        Arrays.fill(cache, "");
    }

    // Reads info for the current job from RAM into cache.
    void loadInstructionsToCache() {
        for (int i = 0; i < current_job.getTotalSize(); i++) {
            cache[i] = MMU.load_ram(current_job.getRamStart() + i);
        }
    }

    /* Get the number of currently loaded instructions in the cache.*/
    public int getCacheUsage() {
        int usage = 0;
        for (String s : cache) {
            if (s != null && !s.equals("")) {
                usage++;
            }
        }
        return usage;
    }

    // Initializes register indexes based on instruction format.

    private void instructionFormat(String format) {
        switch (format) {
            case "00": { // ARITHMETIC
                String reg1 = bin.substring(8, 12);
                reg1_index = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2_index = Integer.parseInt(reg2, 2);

                String reg3 = bin.substring(16, 20);
                reg3_index = Integer.parseInt(reg3, 2);
                break;
            }
            case "01": // CONDITIONAL
            case "11": { // INPUT/OUTPUT
                String reg1 = bin.substring(8, 12);
                reg1_index = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2_index = Integer.parseInt(reg2, 2);

                address = bin.substring(16, 32);
                addressIndex = Integer.parseInt(address, 2) / 4;
                break;
            }
            case "10": { // UNCONDITIONAL
                address = bin.substring(8, 32);
                addressIndex = Integer.parseInt(address, 2) / 4;
                break;
            }
        }
    }

    // Method to provide operations for each OPCODE.
    private void evaluate(String opcode) {
        switch (opcode) {
            case "RD": {
                if (addressIndex == 0) {
                    register[reg1_index].data = Integer.parseInt(cache[register[reg2_index].data], 16);
                } else {
                    register[reg1_index].data = Integer.parseInt(cache[addressIndex], 16);
                }
                ioProcesses++;
                current_job.incrementIoProcesses();
                break;
            }
            case "WR": {
                cache[addressIndex] = Integer.toHexString(register[reg1_index].data);
                ioProcesses++;
                current_job.incrementIoProcesses();
                break;
            }
            case "ST": {
                if (addressIndex == 0) {
                    cache[register[reg2_index].data] = Integer.toHexString(register[reg1_index].data);
                } else {
                    cache[addressIndex] = Integer.toHexString(register[reg1_index].data);
                }
                break;
            }
            case "LW": {
                if (addressIndex == 0) {
                    register[reg2_index].data = Integer.parseInt(cache[register[reg1_index].data], 16);
                } else {
                    register[reg2_index].data = Integer.parseInt(cache[addressIndex], 16);
                }
                break;
            }
            case "MOV": {
                register[reg3_index].data = register[reg1_index].data;
                break;
            }
            case "ADD": {
                register[reg3_index].data = register[reg1_index].data + register[reg2_index].data;
                break;
            }
            case "SUB": {
                register[reg3_index].data = register[reg1_index].data - register[reg2_index].data;
                break;
            }
            case "MUL": {
                register[reg3_index].data = register[reg1_index].data * register[reg2_index].data;
                break;
            }
            case "DIV": {
                if (register[reg2_index].data != 0) {
                    register[reg3_index].data = register[reg1_index].data / register[reg2_index].data;
                }
                break;
            }
            case "AND": {
                if (register[reg1_index].data != 0 && register[reg2_index].data != 0) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg3_index].data = 0;
                }
                break;
            }
            case "OR": {
                if (register[reg1_index].data == 1 || register[reg2_index].data == 1) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg2_index].data = 0;
                }
                break;
            }
            case "MOVI": {
                register[reg2_index].data = Integer.parseInt(address, 2);
                break;
            }
            case "ADDI": {
                register[reg2_index].data++;
                break;
            }
            case "MULI": {
                register[reg2_index].data = register[reg2_index].data * addressIndex;
                break;
            }
            case "DIVI": {
                if (addressIndex != 0) {
                    register[reg2_index].data = register[reg2_index].data / addressIndex;
                }
                break;
            }
            case "LDI": {
                register[reg2_index].data = addressIndex;
                break;
            }
            case "SLT": {
                if (register[reg1_index].data < register[reg2_index].data) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg3_index].data = 0;
                }
                break;
            }
            case "SLTI": {
                if (register[reg1_index].data < addressIndex) {
                    register[reg2_index].data = 1;
                } else {
                    register[reg2_index].data = 0;
                }
                break;
            }
            case "HLT": {
                continueExec = false;
                break;
            }
            case "NOP": {
                pc++;
                break;
            }
            case "JMP": {
                pc = addressIndex;
                break;
            }
            case "BEQ": {
                if (register[reg1_index].data == register[reg2_index].data) {
                    pc = addressIndex;
                }
                break;
            }
            case "BNE": {
                if (register[reg1_index].data != register[reg2_index].data) {
                    pc = addressIndex;
                }
                break;
            }
            case "BEZ": {
                if (register[reg2_index].data == 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BNZ": {
                if (register[reg1_index].data != 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BGZ": {
                if (register[reg1_index].data > 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BLZ": {
                if (register[reg1_index].data < 0) {
                    pc = addressIndex;
                }
                break;
            }
        }
    }

    // enum that depicts the current state of the CPU.

    public enum cpu_state {
        FREE,
        EXECUTING
    }

    @Override
    public String toString() {
        return " | State: " + cpu_state;
    }
}
