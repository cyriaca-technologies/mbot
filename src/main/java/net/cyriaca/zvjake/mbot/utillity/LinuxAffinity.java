package net.cyriaca.zvjake.mbot.utillity;

import java.io.IOException;

public class LinuxAffinity {

    public static void setAffinity(long pid, String bFlags) {
        try {
            Runtime.getRuntime().exec("taskset -p " + pid + " " + Integer.toHexString(Integer.parseInt(bFlags, 2)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(30010001);
        }
    }
}
