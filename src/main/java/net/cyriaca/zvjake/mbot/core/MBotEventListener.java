package net.cyriaca.zvjake.mbot.core;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

class MBotEventListener extends ListenerAdapter {

    private MBot mBot;

    MBotEventListener(MBot mBot) {
        this.mBot = mBot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getGuild().getIdLong() == mBot.getGuild().getIdLong()) {
            User author = event.getAuthor();
            Message message = event.getMessage();
            if (event.getChannel().getIdLong() == mBot.getIoChannel().getIdLong()) {
                mBot.writeServerCommand(message.getContentRaw(), author.getIdLong());
            } else {
                String[] tk = message.getContentDisplay().split(" +");
                String pref = mBot.getPrefix();
                if (tk.length != 0 && tk[0].startsWith(pref)) {
                    TextChannel textChannel = event.getTextChannel();
                    switch (tk[0].substring(pref.length())) {
                        case "shutdownforce":
                            mBot.shutdownForce(textChannel, author.getIdLong());
                            break;
                        case "shutdown":
                            mBot.shutdown(textChannel, author.getIdLong());
                            break;
                        case "stop":
                            mBot.stopServer(textChannel, author.getIdLong());
                            break;
                        case "stopforce":
                            mBot.stopServerNow(textChannel, author.getIdLong());
                            break;
                        case "start":
                            mBot.startServer(textChannel, author.getIdLong());
                            break;
                        case "restart":
                            mBot.restartServer(textChannel, author.getIdLong());
                            break;
                        case "restartforce":
                            mBot.restartServerForce(textChannel, author.getIdLong());
                            break;
                        case "status":
                            mBot.showStatus(textChannel, author.getIdLong());
                        case "metrics":
                            mBot.writePerfMetrics(textChannel, author.getIdLong());
                            break;
                        case "vismetrics":
                            mBot.renderPerfMetrics(textChannel, author.getIdLong());
                            break;
                        case "gc":
                            mBot.garbageCollect(textChannel, author.getIdLong());
                            break;
                        case "about":
                            String aboutStr = "***MBot info***"
                                    + "\nVersion: `" + MBot.VERSION
                                    + "`\nSource: https://github.com/cyriaca-technologies/mbot";
                            textChannel.sendMessage(aboutStr).queue();
                            break;
                        case "help":
                            String helpStr = "***MBot help***"
                                    + "\n```\nServer\n```"
                                    + "\n`" + pref + "status` - show whether server is online/offline"
                                    + "\n`" + pref + "start` - start server"
                                    + "\n`" + pref + "stop` - shutdown server"
                                    + "\n`" + pref + "stopforce` - force shutdown server"
                                    + "\n`" + pref + "restart` - restart server"
                                    + "\n`" + pref + "restartforce` - force restart server"
                                    + "\n```\nBot\n```"
                                    + "\n`" + pref + "shutdown` - force shutdown server and stop bot"
                                    + "\n`" + pref + "shutdownforce` - force shutdown bot"
                                    + "\n`" + pref + "about` - print info about bot"
                                    + "\n`" + pref + "help` - print this help message"
                                    + "\n```\nPerformance\n```"
                                    + "\n`" + pref + "gc` - run jvm garbage collector"
                                    + "\n`" + pref + "metrics` - show system usage metrics"
                                    + "\n`" + pref + "vismetrics` - show system usage metrics in charts";
                            textChannel.sendMessage(helpStr).queue();
                            break;
                    }
                }
            }
        }
    }
}
