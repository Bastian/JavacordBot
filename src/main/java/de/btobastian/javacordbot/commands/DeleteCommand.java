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

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

/**
 * The delete command.
 */
public class DeleteCommand implements CommandExecutor {

    @Command(aliases = {"+delete", "+del"}, description = "Deletes the last <amount> messages of [@user]",
            usage = "delete <amount> [@user]", requiredPermissions = "delete")
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        if (args.length != 1 && message.getMentions().size() == 0
                || args.length != 2 && message.getMentions().size() == 1) {
            return "Invalid amount of arguments!";
        }
        int amount = -1;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) { }

        if (amount < 1) {
            return "The amount must be a number greater than 0!";
        }

        final User fromAuthor = args.length == 1 ? null : message.getMentions().get(0);

        message.getReceiver().getMessageHistory(amount, new FutureCallback<MessageHistory>() {
            @Override
            public void onSuccess(MessageHistory history) {
                for (Message message : history.getMessages()) {
                    if (fromAuthor != null && message.getAuthor() == fromAuthor) {
                        message.delete();
                    } else if (fromAuthor == null) {
                        message.delete();
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                message.reply("Error: " + throwable.getMessage());
            }
        });
        return null;
    }
}
