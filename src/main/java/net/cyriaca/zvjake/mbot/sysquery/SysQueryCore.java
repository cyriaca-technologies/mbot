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

    public BufferedImage renderPerfMetrics(int x, int y, int fSize) {
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
        int[] xV = new int[4];
        int[] yV = new int[4];
        for (int n = 1; n < len; n++) {
            int x1 = (int) ((n - 1) * (((float) x) / (len - 1)));
            int x2 = (int) ((n) * (((float) x) / (len - 1)));
            xV[0] = x1;
            xV[1] = x1;
            xV[2] = x2;
            xV[3] = x2;
            e = e.next;
            float cpuPercN = e.cpuPerc;
            yV[0] = y / 2;
            yV[1] = (int) ((y / 2) - ((cpuPerc / 2.0f) * y));
            yV[2] = (int) ((y / 2) - ((cpuPercN / 2.0f) * y));
            yV[3] = y / 2;
            g.setColor(Color.CYAN);
            g.fillPolygon(xV, yV, 4);
            g.setColor(Color.BLUE);
            g.drawLine(x1, yV[1], x2, yV[2]);
            float memPercN = e.memPerc;
            yV[0] = y;
            yV[1] = y - (int) ((memPerc / 2.0f) * y);
            yV[2] = y - (int) ((memPercN / 2.0f) * y);
            yV[3] = y;
            g.setColor(Color.YELLOW);
            g.fillPolygon(xV, yV, 4);
            g.setColor(Color.ORANGE);
            g.drawLine(x1, yV[1], x2, yV[2]);
            cpuPerc = cpuPercN;
            memPerc = memPercN;
        }
        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, y / 2, x, y / 2);
        g.setColor(Color.GRAY);
        for (int n = 1; n < 4; n++)
            g.drawLine(0, y / 8 * n, x, y / 8 * n);
        for (int n = 5; n < 8; n++)
            g.drawLine(0, y / 8 * n, x, y / 8 * n);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, fSize));
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
        String cpu = String.format("[%010." + (int) (e.cpuPerc * 10.0f) + "s]", "==========").replaceAll(" ", "-");
        String mem = String.format("[%010." + (int) (e.memPerc * 10.0f) + "s]", "==========").replaceAll(" ", "-");
        return String.format("***USAGE***\nCPU\n%s %-6.2f\nMEM\n%s %-6.2f", cpu, e.cpuPerc, mem, e.memPerc);
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
            cur.memPerc = 1.0f - (float) (((double) ms.getFreeBytes()) / ms.getTotalBytes());
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
