package net.cyriaca.zvjake.mbot.sysquery;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.MemoryStats;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SysQueryCore {

    public static final int NO_PROCESS = -1;
    public static final Color INDIGO = new Color(75, 0, 130);

    private final Repeater repeater;
    private JavaSysMon jsm;
    private Thread repeaterThread;
    private long mem;
    private int selfPid;

    private long _mV = 0L;


    public SysQueryCore(int stepLength) {
        selfPid = (int) ProcessHandle.current().pid();
        jsm = new JavaSysMon();
        mem = jsm.physical().getTotalBytes();
        repeater = new Repeater(stepLength, selfPid);
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
        float processPerc = e.processMem / (float) mem;
        float botPerc = e.botMem / (float) mem;
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
            g.setColor(Color.CYAN);
            g.fillPolygon(xV, yV, 4);
            g.setColor(Color.BLACK);
            g.drawLine(x1, yV[1], x2, yV[2]);
            float processPercN = e.processMem / (float) mem;
            yV[1] = y - (int) ((processPerc / 2.0f) * y);
            yV[2] = y - (int) ((processPercN / 2.0f) * y);
            g.setColor(Color.BLUE);
            g.fillPolygon(xV, yV, 4);
            g.setColor(Color.BLACK);
            g.drawLine(x1, yV[1], x2, yV[2]);
            float botPercN = e.botMem / (float) mem;
            yV[1] = y - (int) ((botPerc / 2.0f) * y);
            yV[2] = y - (int) ((botPercN / 2.0f) * y);
            g.setColor(INDIGO);
            g.fillPolygon(xV, yV, 4);
            g.setColor(Color.BLACK);
            g.drawLine(x1, yV[1], x2, yV[2]);
            cpuPerc = cpuPercN;
            memPerc = memPercN;
            processPerc = processPercN;
            botPerc = botPercN;
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
        g.drawString("CPU", 0, th);
        g.drawString("MEM", 0, (y / 2) + th);
        g.setColor(Color.BLUE);
        g.drawString(Integer.toString((int) (cpuPerc * 100.0f)) + "% (" + String.format("%4.2f", e.cpuFreq / 1000000000.0).trim() + " GHz)", 0, y / 2);
        g.setColor(Color.GREEN);
        g.drawString(Integer.toString((int) (memPerc * 100.0f)) + "% (" + String.format("%8.3f", e.botMem / 1073741824.0).trim() + " GB bot / " + String.format("%8.3f", e.processMem / 1073741824.0).trim() + " GB proc / " + String.format("%8.3f", e.memUsed / 1073741824.0).trim() + " GB all / " + String.format("%8.3f", mem / 1073741824.0).trim() + " GB tot)", 0, y);
        return i;
    }

    public String writePerfMetrics() {
        Element e;
        synchronized (repeater) {
            e = repeater.cur;
        }
        int cpuV = (int) (e.cpuPerc * 10.0f);
        int memV = (int) (e.memPerc * 10.0f);
        String cpuUsg = cpuV == 0 ? "[----------]" : String.format("[%-10." + cpuV + "s]", "==========").replaceAll(" ", "-");
        String memUsg = memV == 0 ? "[----------]" : String.format("[%-10." + memV + "s]", "==========").replaceAll(" ", "-");
        return "***USAGE***\n\tCPU\n\t\t"
                + cpuUsg + " " + Integer.toString((int) (e.cpuPerc * 100.0f))
                + "% ("
                + String.format("%4.2f", e.cpuFreq / 1000000000.0).trim() + " GHz)\n\tMEM\n\t\t"
                + memUsg + " " + Integer.toString((int) (e.memPerc * 100.0f))
                + "% ("
                + String.format("%8.3f", e.botMem / 1073741824.0).trim() + " GB bot / "
                + String.format("%8.3f", e.processMem / 1073741824.0).trim() + " GB proc / "
                + String.format("%8.3f", e.memUsed / 1073741824.0).trim() + " GB all / "
                + String.format("%8.3f", mem / 1073741824.0).trim() + " GB tot)";
    }

    public void shutdown() {
        repeater.shutdown = true;
        repeaterThread.interrupt();
    }

    public long getBotMem() {
        jsm.visitProcessTree(selfPid, (process, level) -> {
            if (level == 0)
                _mV = process.processInfo().getResidentBytes();
            return false;
        });
        return _mV;
    }

    public void setPid(int pid) {
        repeater.setPid(pid);
    }

    private class Repeater implements Runnable {

        Element cur;
        boolean shutdown;
        int len;
        int pid;
        int selfPid;

        Repeater(int len, int selfPid) {
            this.selfPid = selfPid;
            pid = -1;
            this.len = len;
            shutdown = false;
            Element first = new Element();
            cur = first;
            cur.times = jsm.cpuTimes();
            cur.cpuPerc = 0.0f;
            MemoryStats ms = jsm.physical();
            cur.memUsed = ms.getTotalBytes() - ms.getFreeBytes();
            cur.memPerc = (float) (((double) cur.memUsed) / ms.getTotalBytes());
            cur.cpuFreq = jsm.cpuFrequencyInHz();
            cur.processMem = 0L;
            jsm.visitProcessTree(selfPid, (process, level) -> {
                if (level == 0)
                    cur.botMem = process.processInfo().getResidentBytes();
                return false;
            });
            for (int i = 1; i < len; i++) {
                Element x = new Element();
                x.cpuPerc = 0.0f;
                x.memPerc = 0.0f;
                x.memUsed = 0L;
                x.cpuFreq = 0L;
                x.processMem = 0L;
                x.botMem = 0L;
                x.prev = cur;
                cur.next = x;
                cur = x;
            }
            first.prev = cur;
            cur.next = first;
            cur = first;
        }

        void setPid(int pid) {
            this.pid = pid;
        }

        @Override
        public void run() {
            while (!shutdown) {
                synchronized (this) {
                    cur = cur.next;
                    cur.times = jsm.cpuTimes();
                    cur.cpuPerc = cur.times.getCpuUsage(cur.prev.times);
                    MemoryStats ms = jsm.physical();
                    cur.memUsed = ms.getTotalBytes() - ms.getFreeBytes();
                    cur.memPerc = (float) (((double) cur.memUsed) / ms.getTotalBytes());
                    cur.cpuFreq = jsm.cpuFrequencyInHz();
                    if (pid != NO_PROCESS) {
                        cur.processMem = 0;
                        jsm.visitProcessTree(pid, (process, level) -> {
                            cur.processMem += process.processInfo().getResidentBytes();
                            return false;
                        });
                    }
                    jsm.visitProcessTree(selfPid, (process, level) -> {
                        if (level == 0)
                            cur.botMem = process.processInfo().getResidentBytes();
                        return false;
                    });
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
        long memUsed;
        long cpuFreq;
        long processMem;
        long botMem;
    }
}
