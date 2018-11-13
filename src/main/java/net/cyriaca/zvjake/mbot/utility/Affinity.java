package net.cyriaca.zvjake.mbot.utility;

import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class Affinity {

    public static void setAffinity(long pid, String bFlags) {
        if (SystemUtils.IS_OS_LINUX) {
            try {
                Runtime.getRuntime().exec("taskset -p " + pid + " " + Integer.toHexString(Integer.parseInt(bFlags, 2)));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(30010001);
            }
        }
    }
}
