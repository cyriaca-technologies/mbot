package net.cyriaca.zvjake.mbot.core;

import net.cyriaca.zvjake.mbot.exception.BuildException;
import net.cyriaca.zvjake.mbot.exception.MBotException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;
import java.util.Set;

class MBotBuilder {

    private JDABuilder builder;
    private long guildId;
    private long ioChannelId;
    private String prefix;
    private String launchCommand;
    private String serverAffinity;
    private Set<Long> adminIds;

    MBotBuilder() {
        this.builder = new JDABuilder(AccountType.BOT);
        this.guildId = -1L;
        this.ioChannelId = -1L;
        this.prefix = null;
        this.launchCommand = null;
        this.adminIds = null;
    }

    void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    void setIoChannelId(long ioChannelId) {
        this.ioChannelId = ioChannelId;
    }

    void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    void setLaunchCommand(String launchCommand) {
        this.launchCommand = launchCommand;
    }

    void setServerAffinity(String serverAffinity) {
        this.serverAffinity = serverAffinity;
    }

    void setAdminIds(Set<Long> adminIds) {
        this.adminIds = adminIds;
    }

    void setToken(String token) {
        builder.setToken(token);
    }

    void build() throws BuildException, LoginException, InterruptedException, MBotException {
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
        if (adminIds == null)
            throw new BuildException("Admin IDs are unspecified / null!");
        JDA jda = builder.build().awaitReady();
        new MBot(jda, guildId, ioChannelId, prefix, launchCommand, serverAffinity, adminIds);
    }

}