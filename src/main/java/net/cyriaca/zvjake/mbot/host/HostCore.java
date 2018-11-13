package net.cyriaca.zvjake.mbot.host;

import net.cyriaca.zvjake.mbot.core.MBot;
import net.cyriaca.zvjake.mbot.sysquery.SysQueryCore;
import net.cyriaca.zvjake.mbot.utility.Affinity;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class HostCore {

    private static final int DISCORD_MAX_CHARS = 2000;
    private static final long SEND_DELAY_MS = 1500;

    private Process process;
    private ReadThread readThread;
    private Thread readThreadThread;
    private BufferedWriter bufferedWriter;
    private Thread lifeThreadThread;

    private HostCore(MBot mBot, Process process) {
        this.process = process;
        readThread = new ReadThread(mBot, process.getInputStream());
        readThreadThread = new Thread(readThread);
        LifeThread lifeThread = new LifeThread(mBot, process);
        lifeThreadThread = new Thread(lifeThread);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        readThreadThread.start();
        lifeThreadThread.start();
    }

    public static HostCore startServer(String runCommand, MBot mBot) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(runCommand.split(" +")).redirectErrorStream(false);
        Process process = builder.start();
        Affinity.setAffinity(process.pid(), mBot.getServerAffinity());
        return new HostCore(mBot, process);
    }

    public void write(String str) {
        try {
            bufferedWriter.write(str);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(20440001);
        }
    }

    public void stop() {
        process.destroy();
        if (readThreadThread.isAlive())
            readThread.stop();
    }

    public void forceStop() {
        process.destroyForcibly();
        if (readThreadThread.isAlive())
            readThread.stop();
    }

    public int getPid() {
        return lifeThreadThread.isAlive() ? (int) process.pid() : SysQueryCore.NO_PROCESS;
    }

    private class ReadThread implements Runnable {

        private BufferedReader scanner;
        private WriteTask writeTask;
        private AtomicBoolean stop;
        private Timer timer;

        private ReadThread(MBot mBot, InputStream inputStream) {
            this.scanner = new BufferedReader(new InputStreamReader(inputStream));
            this.writeTask = new WriteTask(mBot);
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(writeTask, SEND_DELAY_MS, SEND_DELAY_MS);
            this.stop = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String str = scanner.readLine();
                    if (str == null) {
                        timer.cancel();
                        writeTask.run();
                        return;
                    } else
                        writeTask.addString(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void stop() {
            stop.set(true);
        }

    }

    private class WriteTask extends TimerTask {

        private final Object lock = new Object();

        private MBot mBot;
        private StringBuilder sb;

        WriteTask(MBot mBot) {
            this.mBot = mBot;
            this.sb = new StringBuilder();

        }

        private void addString(String string) {
            synchronized (lock) {
                int diff = DISCORD_MAX_CHARS - string.length();
                if (diff < 0) {
                    System.err.printf("Single log string exceeds max char limit by %d characters and will not be sent in Discord\nSource string:\n%s", -diff, string);
                    return;
                }
                if (sb.length() <= diff) {
                    if (sb.length() != 0)
                        sb.append('\n');
                    sb.append(string);
                    if (sb.length() == diff)
                        run();
                } else {
                    run();
                    sb.append(string);
                }
            }
        }

        @Override
        public void run() {
            synchronized (lock) {
                if (sb.length() != 0) {
                    mBot.sendLogAsync(sb.toString());
                    sb = new StringBuilder();
                }
            }
        }
    }

    private class LifeThread implements Runnable {

        private MBot mBot;
        private Process process;

        private LifeThread(MBot mBot, Process process) {
            this.mBot = mBot;
            this.process = process;
        }

        @Override
        public void run() {
            int code = -1;
            try {
                code = process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readThread.stop.set(true);
            mBot.reportStop(code);
        }
    }

}
