import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 Loads information form the data into disk array.
 */

class Loader {
    static void loadFile() {
        //set up the variable for the index, string line, and a null PCB
        int index = 0;
        String strline;
        PCB pcb = null;
        // try to look for file, if not exception is thrown
        try {
            // creates file and scanner
            File file = new File("src/Program-File-Wordversion-30-JOBS.txt");
            Scanner sc = new Scanner(file);

            while(sc.hasNext()) {
                strline = sc.nextLine();
                if (strline.startsWith("//")) {
                    String[] instructionLines = strline.substring(3).split(" ");
                    switch (instructionLines[0]) {
                        case "JOB":{
                            pcb = new PCB(instructionLines[1], instructionLines[2], instructionLines[3], index);
                            break;
                        }
                        case "Data": {
                            if(pcb != null) {
                                pcb.setInputBufferSize(Integer.parseInt(instructionLines[1], 16));
                                pcb.setOutputBufferSize(Integer.parseInt(instructionLines[2], 16));
                                pcb.setTempBufferSize(Integer.parseInt(instructionLines[3], 16));
                            }
                            break;
                        }
                        case "END": {
                            if(pcb != null) {
                                pcb.setJobState(PCB.JobState.READY);
                                Scheduler.addJob(pcb);
                            }
                            break;
                        }
                    }
                }
                else {
                    String code = strline.substring(2, 10);
                    MMU.store_disk(index, code);
                    index++;
                }
            }

            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}