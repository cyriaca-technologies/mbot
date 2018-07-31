package net.cyriaca.zvjake.mbot.core;

import net.cyriaca.zvjake.mbot.exception.BuildException;
import net.cyriaca.zvjake.mbot.exception.MBotException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;
import java.util.Set;

public class MBotBuilder {

    private JDABuilder builder;
    private long guildId;
    private long ioChannelId;
    private String prefix;
    private String launchCommand;
    private String serverAffinity;
    private Set<Long> adminIds;

    public MBotBuilder() {
        this.builder = new JDABuilder(AccountType.BOT);
        this.guildId = -1L;
        this.ioChannelId = -1L;
        this.prefix = null;
        this.launchCommand = null;
        this.adminIds = null;
    }

    public MBotBuilder setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    public MBotBuilder setIoChannelId(long ioChannelId) {
        this.ioChannelId = ioChannelId;
        return this;
    }

    public MBotBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MBotBuilder setLaunchCommand(String launchCommand) {
        this.launchCommand = launchCommand;
        return this;
    }

    public MBotBuilder setServerAffinity(String serverAffinity) {
        this.serverAffinity = serverAffinity;
        return this;
    }

    public MBotBuilder setAdminIds(Set<Long> adminIds) {
        this.adminIds = adminIds;
        return this;
    }

    public MBotBuilder setToken(String token) {
        builder.setToken(token);
        return this;
    }

    public MBot build() throws BuildException, LoginException, InterruptedException, MBotException {
        if (guildId == -1L)
            throw new BuildException("Guild ID not specified!");
        if (ioChannelId == -1L)
            throw new BuildException("IO channel ID not specified!");
        if (prefix == null)
            throw new BuildException("Prefix is null!");
        if (launchCommand == null)
            throw new BuildException("Launch command is null!");
        if (serverAffinity == null)
            throw new BuildException("Server affinity is null!");
        JDA jda = builder.buildBlocking();
        return new MBot(jda, guildId, ioChannelId, prefix, launchCommand, serverAffinity, adminIds);
    }

}