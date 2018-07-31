package net.cyriaca.zvjake.mbot.host;

import net.cyriaca.zvjake.mbot.core.MBot;
import net.cyriaca.zvjake.mbot.utillity.Affinity;

import java.io.*;

public class HostCore {

    private Process process;
    private ReadThread readThread;
    private Thread readThreadThread;
    private BufferedWriter bufferedWriter;

    private HostCore(MBot mBot, Process process) {
        this.process = process;
        readThread = new ReadThread(mBot, process.getInputStream());
        readThreadThread = new Thread(readThread);
        LifeThread lifeThread = new LifeThread(mBot, process);
        Thread lifeThreadThread = new Thread(lifeThread);
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

    private class ReadThread implements Runnable {

        private MBot mBot;
        private BufferedReader scanner;
        private volatile boolean stop;

        private ReadThread(MBot mBot, InputStream inputStream) {
            this.mBot = mBot;
            this.scanner = new BufferedReader(new InputStreamReader(inputStream));
            this.stop = false;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    if (scanner.ready()) {
                        try {
                            mBot.sendLogAsync(scanner.readLine());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!stop)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
            }
        }

        private void stop() {
            stop = true;
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
            readThread.stop = true;
            mBot.reportStop(code);
        }
    }

}
