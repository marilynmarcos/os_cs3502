import java.util.concurrent.Semaphore;
import java.io.*;
import java.util.Scanner;


//Memory Management Unit
class MMU
{
    static String[] disk = new String[Driver.disk_size];

    static String[] ram = new String[Driver.ram_size];
    
    static void init() {
        for (int i = 0; i < ram.length; i++) {
            store_ram(i, "");
        }
    }

    static int left(int size) {
        int ammount_left = 0;
        for (int i = 0; i < ram.length; i++) {
            if (ammount_left == size) {
                return i - size;
            }

            if(ram[i].isEmpty()) {
                ammount_left++;
            } else {
                ammount_left = 0;
            }
        }

        return -1;
    }
    
    //We need to clear a section of memory
    static void clear_all(int inclusiveStart, int exclusiveEnd) {
        for (int i = inclusiveStart; i < exclusiveEnd; i++) {
            store_ram(i, "");
        }
    }

    //Load values into ram and disk
    static String load_ram(int address) {
        if (ram[address].isEmpty()) {
            return "";
        } else {
            return ram[address].substring(0, 8);
        }
    }
    static String load_disk(int address) {
        return disk[address].substring(0, 8);
    }

    //store values we just loaded
    static void store_ram(int address, String data) {
        ram[address] = data;
    }
    static void store_disk(int address, String data) {
        disk[address] = data;
    }
    
    //Ram usage will be the ammount of words loaded into memory
    static int ram_usage() {
        int usage = 0;
        for (int i = 0; i < ram.length; i++) {
            if(!ram[i].equals("")) {
                usage++;
            }
        }
        return usage;
    }
}