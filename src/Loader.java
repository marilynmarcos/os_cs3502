import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class Loader
{
    /**
     * Loads instructions from data file into disk array.
     * Creates new PCB objects for each job control card, storing important job information.
     */
    static void load() {
        int index = 0;
        try {
            File programfile = new File("src/Program-File-Wordversion-30-JOBS.txt");
            Scanner scanner = new Scanner(programfile);
            PCB current_pcb = null;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("//")) {
                    String[] instructions = line.substring(3).split(" ");
                    switch (instructions[0]) {
                        case "JOB": {
                            current_pcb = new PCB(instructions[1], instructions[2], instructions[3], index);
                            break;
                        }
                        case "Data": {
                            if (current_pcb != null) {
                                current_pcb.setInputBufferSize(Integer.parseInt(instructions[1], 16));
                                current_pcb.setOutputBufferSize(Integer.parseInt(instructions[2], 16));
                                current_pcb.setTempBufferSize(Integer.parseInt(instructions[3], 16));
                            }
                            break;
                        }
                        case "END": {
                            if (current_pcb != null) {
                                current_pcb.setJobState(PCB.JobState.READY);
                                Scheduler.addJob(current_pcb);
                            }
                            break;
                        }
                    }
                } else {
                    // Store instruction on disk
                    MMU.store_disk(index, line.substring(2, 10));
                    index++;
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}