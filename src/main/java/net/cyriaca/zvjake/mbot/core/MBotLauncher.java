package net.cyriaca.zvjake.mbot.core;

import net.cyriaca.zvjake.mbot.exception.BuildException;
import net.cyriaca.zvjake.mbot.exception.MBotException;
import net.cyriaca.zvjake.mbot.utility.Affinity;

import javax.json.*;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;

public class MBotLauncher {

    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            System.exit(24020001);
        }
        File configFile = new File(args[0]);
        JsonReader reader = null;
        try {
            reader = Json.createReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR]<Launcher> Config file not found!\nPath: " + configFile.getAbsolutePath());
            System.exit(24020002);
        }
        JsonObject configData = reader.readObject();
        try {
            reader.close();
        } catch (JsonException e) {
            System.err.println("[ERROR]<Launcher> Failed to close IO stream! Details:\n" + e.getMessage());
            System.exit(24020003);
        }
        JsonNumber guildIdObj = null;
        try {
            guildIdObj = configData.getJsonNumber("guild");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"guild\" was wrong type!\nExpected: " + JsonValue.ValueType.NUMBER.name() + "\nActual: " + configData.getValue("guild").getValueType().name());
            System.exit(24020004);
        }
        if (guildIdObj == null) {
            System.err.println("[ERROR]<Launcher> Config element \"guild\" missing!");
            System.exit(24020005);
        }
        JsonNumber ioChannelIdObj = null;
        try {
            ioChannelIdObj = configData.getJsonNumber("io_channel");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"io_channel\" was wrong type!\nExpected: " + JsonValue.ValueType.NUMBER.name() + "\nActual: " + configData.getValue("io_channel").getValueType().name());
            System.exit(24020006);
        }
        if (ioChannelIdObj == null) {
            System.err.println("[ERROR]<Launcher> Config element \"io_channel\" missing!");
            System.exit(24020007);
        }
        String token = null;
        try {
            token = configData.getString("token");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"token\" was wrong type!\nExpected: " + JsonValue.ValueType.STRING.name() + "\nActual: " + configData.getValue("token").getValueType().name());
            System.exit(24020008);
        }
        if (token == null) {
            System.err.println("[ERROR]<Launcher> Config element \"token\" missing!");
            System.exit(24020009);
        }
        String prefix = null;
        try {
            prefix = configData.getString("prefix");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"prefix\" was wrong type!\nExpected: " + JsonValue.ValueType.STRING.name() + "\nActual: " + configData.getValue("prefix").getValueType().name());
            System.exit(24020010);
        }
        if (prefix == null) {
            System.err.println("[ERROR]<Launcher> Config element \"prefix\" missing!");
            System.exit(24020011);
        }
        String launchCommand = null;
        try {
            launchCommand = configData.getString("launch_command");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"launch_command\" was wrong type!\nExpected: " + JsonValue.ValueType.STRING.name() + "\nActual: " + configData.getValue("launch_command").getValueType().name());
            System.exit(24020012);
        }
        if (launchCommand == null) {
            System.err.println("[ERROR]<Launcher> Config element \"launch_command\" missing!");
            System.exit(24020013);
        }
        String botAffinity = null;
        try {
            botAffinity = configData.getString("bot_affinity");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"bot_affinity\" was wrong type!\nExpected: " + JsonValue.ValueType.STRING.name() + "\nActual: " + configData.getValue("bot_affinity").getValueType().name());
            System.exit(24020012);
        }
        if (botAffinity == null) {
            System.err.println("[ERROR]<Launcher> Config element \"bot_affinity\" missing!");
            System.exit(24020013);
        }
        Affinity.setAffinity(ProcessHandle.current().pid(), botAffinity);
        String serverAffinity = null;
        try {
            serverAffinity = configData.getString("server_affinity");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"server_affinity\" was wrong type!\nExpected: " + JsonValue.ValueType.STRING.name() + "\nActual: " + configData.getValue("server_affinity").getValueType().name());
            System.exit(24020012);
        }
        if (serverAffinity == null) {
            System.err.println("[ERROR]<Launcher> Config element \"server_affinity\" missing!");
            System.exit(24020013);
        }
        JsonArray adminIdArray = null;
        try {
            adminIdArray = configData.getJsonArray("admin_ids");
        } catch (ClassCastException e) {
            System.err.println("[ERROR]<Launcher> Config element \"admin_ids\" was wrong type!\nExpected: " + JsonValue.ValueType.ARRAY.name() + "\nActual: " + configData.getValue("admin_ids").getValueType().name());
            System.exit(24020014);
        }
        if (adminIdArray == null) {
            System.err.println("[ERROR]<Launcher> Config element \"admin_ids\" missing!");
            System.exit(24020015);
        }
        Set<Long> adminIds = new TreeSet<>();
        JsonNumber adminIdObj = null;
        for (int i = 0; i < adminIdArray.size(); i++) {
            try {
                adminIdObj = adminIdArray.getJsonNumber(i);
            } catch (ClassCastException e) {
                System.err.println("[ERROR]<Launcher> Config element \"admin_ids\"[" + i + "] was wrong type!\nExpected: " + JsonValue.ValueType.NUMBER.name() + "\nActual: " + adminIdArray.get(i).getValueType().name());
                System.exit(24020016);
            }
            adminIds.add(adminIdObj.longValueExact());
        }
        MBotBuilder builder = new MBotBuilder();
        builder.setGuildId(guildIdObj.longValueExact());
        builder.setIoChannelId(ioChannelIdObj.longValueExact());
        builder.setToken(token);
        builder.setPrefix(prefix);
        builder.setLaunchCommand(launchCommand);
        builder.setServerAffinity(serverAffinity);
        builder.setAdminIds(adminIds);
        try {
            builder.build();
        } catch (BuildException e) {
            System.err.println("[ERROR]<Launcher> BuildException while building MBot! Details:\n" + e.getMessage());
            System.exit(24020017);
        } catch (LoginException e) {
            System.err.println("[ERROR]<Launcher> LoginException while building MBot! Details:\n" + e.getMessage());
            System.exit(24020018);
        } catch (InterruptedException e) {
            System.err.println("[ERROR]<Launcher> InterruptedException while building MBot! Details:\n" + e.getMessage());
            System.exit(24020019);
        } catch (MBotException e) {
            System.err.println("[ERROR]<Launcher> MBotException while building MBot! Details:\n" + e.getMessage());
            System.exit(24020020);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:\n\tjava -jar MBot.jar <configfile>");
    }

}
