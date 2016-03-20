/*
 * Copyright (C) 2016 Bastian Oppermann
 * 
 * This file is part of my Javacord Discord bot.
 * 
 * This bot is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser general Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.btobastian.javacordbot.commands;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.ImplDiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.MessageDecoration;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacordbot.util.commands.Command;
import de.btobastian.javacordbot.util.commands.CommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The command to test uptime
 */
public class UptimeTest implements CommandExecutor {

    private final long startTime = System.currentTimeMillis();
    /**
     * A list with all channel ids.
     */
    private List<String> channelIds = new ArrayList<>();
    private boolean started = false;

    @Override
    @Command(aliases = "testUptime", description = "Posts uptime stats in the current channel", adminOnly = true, showInHelpPage = false)
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        // gets the id of the channel
        String id = message.isPrivateMessage() ?
                message.getUserReceiver().getId() : message.getChannelReceiver().getId();
        synchronized (channelIds) {
            channelIds.add(id);
        }
        if (!started) {
            started = true;
            start(api);
        }
        return "Added channel to uptime test!\n" +
                "Every 12 hours the current uptime will be posted in this channel.\n" +
                "This is to test the stability of the new websocket javacord uses.\n" +
                "It's only for one session!";
    }

    private void start(DiscordAPI api) {
        final ImplDiscordAPI implDiscordAPI = (ImplDiscordAPI) api;
        api.getThreadPool().getExecutorService().submit((Runnable) () -> {
            for (;;) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.append(MessageDecoration.CODE_LONG.getPrefix()).append("xml").appendNewLine();
                appendUptime(messageBuilder);
                messageBuilder.appendNewLine().append(MessageDecoration.CODE_LONG.getSuffix());
                synchronized (channelIds) {
                    for (String id : channelIds) {
                        MessageReceiver receiver = null;
                        receiver = implDiscordAPI.getChannelById(id);
                        if (receiver == null) {
                            receiver = implDiscordAPI.getCachedUserById(id);
                        }
                        if (receiver == null) {
                            continue;
                        }
                        receiver.sendMessage(messageBuilder.toString());
                    }
                }
                try {
                    Thread.sleep(1000 * 60 * 60 * 12);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Appends the uptime of the bot.
     *
     * @param msgBuilder The message builder.
     */
    private void appendUptime(MessageBuilder msgBuilder) {
        long millis = System.currentTimeMillis() - startTime;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        msgBuilder
                .appendNewLine()
                .append("• Uptime: " )
                .append(days + " Days " + hours + " Hours " + minutes + " Minutes " + seconds + " Seconds");
    }

}
