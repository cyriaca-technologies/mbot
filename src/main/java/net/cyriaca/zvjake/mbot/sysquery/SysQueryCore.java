package net.cyriaca.zvjake.mbot.sysquery;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.MemoryStats;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SysQueryCore {

    private final Repeater repeater;
    private JavaSysMon jsm;
    private Thread repeaterThread;

    public SysQueryCore(int stepLength) {
        jsm = new JavaSysMon();
        repeater = new Repeater(stepLength);
        repeaterThread = new Thread(repeater);
        repeaterThread.start();
    }

    public BufferedImage renderPerfMetrics(int x, int y) {
        BufferedImage i = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        Graphics g = i.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, x, y);

        Element e;
        synchronized (repeater) {
            e = repeater.cur.next;
        }
        float cpuPerc = e.cpuPerc;
        float memPerc = e.memPerc;
        int len = repeater.len;
        for (int n = 1; n < len; n++) {
            float x1 = (n - 1) * (((float) x) / (len - 1));
            float x2 = (n) * (((float) x) / (len - 1));
            e = e.next;
            float cpuPercN = e.cpuPerc;
            g.setColor(Color.BLUE);
            g.drawLine((int) x1, (int) ((y / 2) - ((cpuPerc / 2.0f) * y)), (int) x2, (int) ((y / 2) - ((cpuPercN / 2.0f) * y)));
            float memPercN = e.memPerc;
            g.setColor(Color.GREEN);
            g.drawLine((int) x1, y - (int) ((memPerc / 2.0f) * y), (int) x2, y - (int) ((memPercN / 2.0f) * y));
            cpuPerc = cpuPercN;
            memPerc = memPercN;
        }
        int th = g.getFontMetrics().getHeight();
        g.setColor(Color.BLACK);
        g.drawString(Integer.toString((int) (cpuPerc * 100.0f)) + "%", 0, y / 2);
        g.drawString("CPU", 0, th);
        g.drawString(Integer.toString((int) (memPerc * 100.0f)) + "%", 0, y);
        g.drawString("MEM", 0, (y / 2) + th);
        return i;
    }

    public String writePerfMetrics() {
        Element e;
        synchronized (repeater) {
            e = repeater.cur;
        }
        return "***USAGE***\nCPU: " + e.cpuPerc + "\nMEM: " + e.memPerc;
    }

    public void shutdown() {
        repeater.shutdown = true;
        repeaterThread.interrupt();
    }

    private class Repeater implements Runnable {

        Element cur;
        boolean shutdown;
        int len;

        Repeater(int len) {
            this.len = len;
            shutdown = false;
            Element first = new Element();
            cur = first;
            cur.times = jsm.cpuTimes();
            cur.cpuPerc = 0.0f;
            MemoryStats ms = jsm.physicalWithBuffersAndCached();
            cur.memPerc = 1.0f - (float) ((1 - (double) ms.getFreeBytes()) / ms.getTotalBytes());
            for (int i = 1; i < len; i++) {
                Element x = new Element();
                x.cpuPerc = 0.0f;
                x.memPerc = 0.0f;
                x.prev = cur;
                cur.next = x;
                cur = x;
            }
            first.prev = cur;
            cur.next = first;
            cur = first;
        }

        @Override
        public void run() {
            while (!shutdown) {
                synchronized (this) {
                    cur = cur.next;
                    cur.times = jsm.cpuTimes();
                    cur.cpuPerc = cur.times.getCpuUsage(cur.prev.times);
                    MemoryStats ms = jsm.physicalWithBuffersAndCached();
                    cur.memPerc = 1.0f - (float) (((double) ms.getFreeBytes()) / ms.getTotalBytes());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (shutdown)
                        return;
                }
            }
        }

    }

    private class Element {
        Element next;
        Element prev;
        float cpuPerc;
        CpuTimes times;
        float memPerc;
    }
}
