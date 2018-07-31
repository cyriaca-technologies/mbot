# mbot

### A Discord bot for keeping an eye on your game server process

##### This code requires [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 9 features.

##### The latest releases are [here](https://github.com/cyriaca-technologies/mbot/releases).

## Contents

* [Functions](#functions)

* [Execution](#execution)

* [Config format](#config-format)

* [Additional info](#additional-info)

## Functions

* `start` / `stop` / `stopforce` / `restart` server process (with a command specified in the configuration file)
* See target process output lines (merged stdout and stderr) as messages from the bot in designated IO channel (mind the Discord ratelimiting)
* See target process exit code as a message from the bot in the IO channel on termination
* Write to stdin by sending messages in the IO channel
* Check basic status (online/offline, shutting down/etc.) with the `status` command
* Check CPU utilization / memory usage with the `metrics` and `status` commands
* See graph of CPU utilization / memory usage with the `vismetrics` command
* Shutdown bot with the `shutdown` / `shutdownforce` commands
* Display help with the `help` command

## Execution

`java -jar mbot.jar <configfile>`

## Config format

```
{
  "token":"{token-here}",
  "guild":{guild-id-here},
  "io_channel":{io-channel-here},
  "prefix":"{prefix-here}",
  "launch_command":"{launch-command-here}",
  "bot_affinity":"{affinity-flags-here}",
  "server_affinity":"{affinity-flags-here}",
  "admin_ids":
  [
    {admin-ids-here}
  ]
}

```

#### token

The token used by the bot to login to Discord.

#### guild

The unique ID of the guild for the bot to work in.

#### io_channel

The unique ID of the channel used for the target process's log output.

#### prefix

Command prefix, e.g. "."

#### bot_affinity / server_affinity

Processor affinity flags for the bot process / server process.

Increasing order bits represent increasing logical CPUs.

e.g. a value of `11` would set it to operate on the lowest 2 logical CPUs

e.g. a value of `1000` would set it to operate on the 4th lowest logical CPU

This works using system-dependent commands (only code for Linux support is implemented).

#### admin_ids

The unique IDs of the bot's admins (able to access all functionality).

## Additional info

Currently, normal users are able  to call up the status of the target process (online/offline, shutting down/etc.) while simultaneously reporting CPU / memory usage. The usage statistics are pulled from saved snapshots.

Process affinity setting is only implemented for Linux.