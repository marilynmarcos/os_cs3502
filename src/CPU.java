import java.math.BigInteger;
import java.util.Arrays;


 // This class handles the execution of instructions read from the memory.
public class CPU extends Thread {

    // CPU Identification Info
    private PCB currentJob;
    private final int cpuId;
    private CpuState cpuState;

    // Determine opcode by indexing opcodeArray
    private final String[] opcodeArray = {"RD", "WR", "ST", "LW", "MOV", "ADD", "SUB", "MUL", "DIV", "AND",
            "OR", "MOVI", "ADDI", "MULI", "DIVI", "LDI", "SLT", "SLTI", "HLT", "NOP", "JMP", "BEQ",
            "BNE", "BEZ", "BNZ", "BGZ", "BLZ"};

    // bin instructions
    private String bin;
    private String address;
    private int reg1;
    private int reg2;
    private int reg3;
    private int addressIndex;

    // Memory
    private Register[] register = new Register[16];
    private final String[] cache = new String[Driver.cacheSize];

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
        this.cpuId = id;
        this.cpuState = CpuState.FREE;
    }

  
     // This method loads an instruction at a specified address from RAM.
    private String fetch(int index) {
        return cache[index].substring(0, 8);
    }

     // Decode a bin string of instructions.
     // Chars 0-1 indicate instruction format.
     // Chars 2-7 indicate instruction OPCODE.
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

        // Chars 2-7 specify opcode of action. This is converted to decimal and used to index opcodeArray
        String opcodeBinary = bin.substring(2, 8);
        evaluate(opcodeArray[Integer.parseInt(opcodeBinary, 2)]);
    }

     // Main thread execution of the CPU class.
     Each CPU will independently check for remaining jobs and execute them accordingly.
\    @Override
    public void run() {
        // Checking if job completed successfully
        while (!Scheduler.hasNext()) {
            PCB nextJob = Scheduler.nextJob();
            if (nextJob != null) {
                jobCount++;
                cpuState = CpuState.EXECUTING;
                Dispatcher.loadJob(nextJob, this);
                nextJob.setRamUsage(MMU.getRamUsage());
                loadInstructionsToCache();
                nextJob.setCacheUsage(getCacheUsage());
                while (continueExec && pc < currentJob.getNumInstructions()) {
                    // Artificial exec time for each instruction
                    try {
                        Thread.sleep(Driver.msThreadDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String hex = fetch(pc);
                    pc++;
                    currentJob.incrementProgramCounter();
                    decode(hex);
                }
                MMU.clearBits(currentJob.getRamStart(), currentJob.getRamEnd());
                Dispatcher.unloadJob(currentJob, this);
                cpuState = CpuState.FREE;
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
    void setCurrentJob(PCB job) {
        this.currentJob = job;
    }
    PCB getCurrentJob() {
        return currentJob;
    }
    void setHasInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
    void setRegisters(Register[] register) {
        this.register = register;
    }
    Register[] getRegisters() {
        return register;
    }
    public int getCpuId() {
        return cpuId;
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
        for (int i = 0; i < currentJob.getTotalSize(); i++) {
            cache[i] = MMU.loadRam(currentJob.getRamStart() + i);
        }
    }

     * Get the number of currently loaded instructions in the cache.
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
                reg1 = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2 = Integer.parseInt(reg2, 2);

                String reg3 = bin.substring(16, 20);
                reg3 = Integer.parseInt(reg3, 2);
                break;
            }
            case "01": // CONDITIONAL
            case "11": { // INPUT/OUTPUT
                    String reg1 = bin.substring(8, 12);
                reg1 = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2 = Integer.parseInt(reg2, 2);

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
                    register[reg1].data = Integer.parseInt(cache[register[reg2].data], 16);
                } else {
                    register[reg1].data = Integer.parseInt(cache[addressIndex], 16);
                }
                ioProcesses++;
                currentJob.incrementIoProcesses();
                break;
            }
            case "WR": {
                cache[addressIndex] = Integer.toHexString(register[reg1].data);
                ioProcesses++;
                currentJob.incrementIoProcesses();
                break;
            }
            case "ST": {
                if (addressIndex == 0) {
                    cache[register[reg2].data] = Integer.toHexString(register[reg1].data);
                } else {
                    cache[addressIndex] = Integer.toHexString(register[reg1].data);
                }
                break;
            }
            case "LW": {
                if (addressIndex == 0) {
                    register[reg2].data = Integer.parseInt(cache[register[reg1].data], 16);
                } else {
                    register[reg2].data = Integer.parseInt(cache[addressIndex], 16);
                }
                break;
            }
            case "MOV": {
                register[reg3].data = register[reg1].data;
                break;
            }
            case "ADD": {
                register[reg3].data = register[reg1].data + register[reg2].data;
                break;
            }
            case "SUB": {
                register[reg3].data = register[reg1].data - register[reg2].data;
                break;
            }
            case "MUL": {
                register[reg3].data = register[reg1].data * register[reg2].data;
                break;
            }
            case "DIV": {
                if (register[reg2].data != 0) {
                    register[reg3].data = register[reg1].data / register[reg2].data;
                }
                break;
            }
            case "AND": {
                if (register[reg1].data != 0 && register[reg2].data != 0) {
                    register[reg3].data = 1;
                } else {
                    register[reg3].data = 0;
                }
                break;
            }
            case "OR": {
                if (register[reg1].data == 1 || register[reg2].data == 1) {
                    register[reg3].data = 1;
                } else {
                    register[reg3].data = 0;
                }
                break;
            }
            case "MOVI": {
                register[reg2].data = Integer.parseInt(address, 2);
                break;
            }
            case "ADDI": {
                register[reg2].data++;
                break;
            }
            case "MULI": {
                register[reg2].data = register[reg2].data * addressIndex;
                break;
            }
            case "DIVI": {
                if (addressIndex != 0) {
                    register[reg2].data = register[reg2].data / addressIndex;
                }
                break;
            }
            case "LDI": {
                register[reg2].data = addressIndex;
                break;
            }
            case "SLT": {
                if (register[reg1].data < register[reg2].data) {
                    register[reg3].data = 1;
                } else {
                    register[reg3].data = 0;
                }
                break;
            }
            case "SLTI": {
                if (register[reg1].data < addressIndex) {
                    register[reg2].data = 1;
                } else {
                    register[reg2].data = 0;
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
                if (register[reg1].data == register[reg2].data) {
                    pc = addressIndex;
                }
                break;
            }
            case "BNE": {
                if (register[reg1].data != register[reg2].data) {
                    pc = addressIndex;
                }
                break;
            }
            case "BEZ": {
                if (register[reg2].data == 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BNZ": {
                if (register[reg1].data != 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BGZ": {
                if (register[reg1].data > 0) {
                    pc = addressIndex;
                }
                break;
            }
            case "BLZ": {
                if (register[reg1].data < 0) {
                    pc = addressIndex;
                }
                break;
            }
        }
    }

     // enum that depicts the current state of the CPU.

    public enum CpuState {
        FREE,
        EXECUTING
    }

    @Override
    public String toString() {
        return "ID: " + cpuId + " | State: " + cpuState;
    }
}
