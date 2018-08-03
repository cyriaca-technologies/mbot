package net.cyriaca.zvjake.mbot.core;

import net.cyriaca.zvjake.mbot.exception.MBotException;
import net.cyriaca.zvjake.mbot.host.HostCore;
import net.cyriaca.zvjake.mbot.sysquery.SysQueryCore;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class MBot {

    public static final String VERSION = "1.0.2.1";

    private JDA jda;
    private SysQueryCore sysQueryCore;
    private HostCore hostCore;
    private Set<Long> adminIds;

    private Guild guild;
    private TextChannel ioChannel;
    private String prefix;
    private String launchCommand;
    private String serverAffinity;

    private HoldAction holdAction;

    MBot(JDA jda, long guildId, long ioChannelId, String prefix, String launchCommand, String serverAffinity, Set<Long> adminIds) throws MBotException {
        if (prefix == null)
            throw new MBotException("Prefix is null!");
        if (launchCommand == null)
            throw new MBotException("Launch command is null!");
        if (serverAffinity == null)
            throw new MBotException("Server affinity is null!");
        this.jda = jda;
        guild = jda.getGuildById(guildId);
        if (guild == null)
            throw new MBotException("Given ID for guild \"" + guildId + "\" does not exist!");
        ioChannel = guild.getTextChannelById(ioChannelId);
        if (ioChannel == null)
            throw new MBotException("Given ID for io channel \"" + ioChannelId + "\" does not exist!");
        this.prefix = prefix;
        this.launchCommand = launchCommand;
        this.serverAffinity = serverAffinity;
        MBotEventListener mbel = new MBotEventListener(this);
        sysQueryCore = new SysQueryCore(60);
        hostCore = null;
        this.adminIds = adminIds == null ? new TreeSet<>() : new TreeSet<>(adminIds);
        holdAction = HoldAction.NONE;
        jda.addEventListener(mbel);
        jda.getPresence().setGame(Game.of(Game.GameType.LISTENING, prefix + "help"));
    }

    public void sendLogAsync(String s) {
        System.out.println(s);
        ioChannel.sendMessage(s).queue();
    }

    public Guild getGuild() {
        return guild;
    }

    public TextChannel getIoChannel() {
        return ioChannel;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isAdmin(long userId) {
        return adminIds.contains(userId);
    }

    public String getServerAffinity() {
        return serverAffinity;
    }

    public void reportStop(int code) {
        sysQueryCore.setPid(SysQueryCore.NO_PROCESS);
        System.gc();
        synchronized (this) {
            MessageAction action = ioChannel.sendMessage("***[Server has stopped with process code " + code + " and is now offline]***");
            hostCore = null;
            switch (holdAction) {
                case BOT_SHUTDOWN:
                    action.complete();
                    completeShutdown();
                    break;
                case SERVER_RESTART:
                    action.queue();
                    serverLaunch();
                    holdAction = HoldAction.NONE;
                    break;
                case SERVER_SHUTDOWN:
                case NONE:
                    action.queue();
                    break;
            }
        }
    }

    public void writeServerCommand(String str, long user) {
        if (holdAction == HoldAction.NONE && isAdmin(user)) {
            if (hostCore != null)
                hostCore.write(str + "\n");
        }
    }

    public void showStatus(TextChannel channel, long user) {
        String status = null;
        switch (holdAction) {
            case BOT_SHUTDOWN:
                status = "shutting down (bot shutdown)";
                break;
            case SERVER_SHUTDOWN:
                status = "shutting down";
                break;
            case SERVER_RESTART:
                status = "rebooting";
                break;
            case NONE:
                status = hostCore != null ? "online" : "offline";
                break;
        }
        channel.sendMessage("Server is " + status).queue();
    }

    public void shutdown(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore == null) {
                    channel.sendMessage("Shutting down bot.").complete();
                    completeShutdown();
                }
                if (holdAction == HoldAction.NONE) {
                    channel.sendMessage("Shutting down server and bot.").complete();
                    holdAction = HoldAction.BOT_SHUTDOWN;
                    hostCore.stop();
                } else
                    channel.sendMessage("Already performing another start/stop action!").queue();
            }
        }
    }

    public void shutdownForce(TextChannel channel, long user) {
        if (isAdmin(user))
            System.exit(0);
    }

    private void completeShutdown() {
        jda.shutdownNow();
        sysQueryCore.shutdown();
        System.exit(0);
    }

    private void serverLaunch() {
        try {
            System.out.println("STARTING");
            hostCore = HostCore.startServer(launchCommand, this);
            sysQueryCore.setPid(hostCore.getPid());
            holdAction = HoldAction.NONE;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(24030001);
        }
    }

    public void restartServer(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore == null)
                    serverLaunch();
                else {
                    if (holdAction == HoldAction.NONE) {
                        channel.sendMessage("Restarting server.").complete();
                        holdAction = HoldAction.SERVER_RESTART;
                        hostCore.stop();
                    } else
                        channel.sendMessage("Already performing another start/stop action!").queue();
                }
            }
        }
    }

    public void restartServerForce(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore == null)
                    serverLaunch();
                else {
                    if (holdAction == HoldAction.NONE || holdAction == HoldAction.SERVER_RESTART) {
                        channel.sendMessage("Force restarting server.").complete();
                        holdAction = HoldAction.SERVER_RESTART;
                        hostCore.forceStop();
                    } else
                        channel.sendMessage("Already performing another start/stop action!").queue();
                }
            }
        }
    }

    public void stopServer(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore == null)
                    channel.sendMessage("Server is already offline!").queue();
                else {
                    if (holdAction == HoldAction.NONE) {
                        channel.sendMessage("Shutting down server.").queue();
                        holdAction = HoldAction.SERVER_SHUTDOWN;
                        hostCore.stop();
                    } else
                        channel.sendMessage("Already performing another start/stop action!").queue();
                }
            }
        }
    }

    public void stopServerNow(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore == null)
                    channel.sendMessage("Server is already offline!").queue();
                else {
                    if (holdAction == HoldAction.NONE || holdAction == HoldAction.SERVER_SHUTDOWN) {
                        channel.sendMessage("Force shutting down server.").queue();
                        holdAction = HoldAction.SERVER_SHUTDOWN;
                        hostCore.forceStop();
                    } else
                        channel.sendMessage("Already performing another start/stop action!").queue();
                }
            }
        }
    }

    public void startServer(TextChannel channel, long user) {
        synchronized (this) {
            if (isAdmin(user)) {
                if (hostCore != null)
                    channel.sendMessage("Server is already online!").queue();
                else
                    serverLaunch();
            }
        }
    }

    public void garbageCollect(TextChannel channel, long user) {
        if (isAdmin(user)) {
            long mem = sysQueryCore.getBotMem();
            System.gc();
            long mem2 = sysQueryCore.getBotMem();
            channel.sendMessage("`GC Result`\nFreed approximately " + String.format("%12.4f", (mem - mem2) / 1048576.0).trim() + " MB\nNew usage: " + String.format("%12.4f", mem2 / 1048576.0).trim() + " MB").queue();
        }
    }

    public void writePerfMetrics(TextChannel channel, long user) {
        if (isAdmin(user))
            channel.sendMessage(sysQueryCore.writePerfMetrics()).queue();
    }

    public void renderPerfMetrics(TextChannel channel, long user) {
        if (isAdmin(user)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(sysQueryCore.renderPerfMetrics(1024, 768, 20), "jpg", baos);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(24030004);
            }
            byte[] bytes = baos.toByteArray();
            channel.sendFile(bytes, "stats.png").queue();
        }
    }

    private enum HoldAction {
        BOT_SHUTDOWN,
        SERVER_SHUTDOWN,
        SERVER_RESTART,
        NONE

    }

}
