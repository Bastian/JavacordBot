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
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.MessageDecoration;
import de.btobastian.javacord.utils.LoggerUtil;
import de.btobastian.javacordbot.util.commands.Command;
import de.btobastian.javacordbot.util.commands.CommandExecutor;
import de.btobastian.javacordbot.util.commands.CommandHandler;
import org.slf4j.Logger;

/**
 * The help command.
 */
public class HelpCommand implements CommandExecutor {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(HelpCommand.class);

    private final CommandHandler commandHandler;

    /**
     * Creates a new instance of this class.
     *
     * @param commandHandler The command handler.
     */
    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    @Command(aliases = {"help", "commands"}, description = "Shows this page", privateMessages = true)
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(MessageDecoration.CODE_LONG.getPrefix()).append("xml");
        for (CommandHandler.SimpleCommand simpleCommand : commandHandler.getCommands()) {
            if (!simpleCommand.isShowInHelpPage()) {
                continue;
            }
            messageBuilder
                    .appendNewLine()
                    .append(simpleCommand.getCommandPrefix())
                    .append(simpleCommand.getUsage())
                    .append(" | ")
                    .append(simpleCommand.getDescription());
        }
        messageBuilder.appendNewLine().append(MessageDecoration.CODE_LONG.getSuffix());
        return messageBuilder.toString();
    }
}
