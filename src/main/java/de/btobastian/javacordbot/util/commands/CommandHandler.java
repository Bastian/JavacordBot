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
package de.btobastian.javacordbot.util.commands;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.javacord.utils.LoggerUtil;
import org.slf4j.Logger;

import java.util.*;

public class CommandHandler implements MessageCreateListener {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(CommandHandler.class);
    /**
     * A list with all commands.
     */
    public final List<SimpleCommand> commandsList = new ArrayList<>();
    /**
     * A map with all commands and their aliases.
     */
    private final HashMap<String, SimpleCommand> commands = new HashMap<>();
    /**
     * The id of the bot's admin.
     */
    private final String adminId;

    /**
     * Creates a new instance of this class.
     *
     * @param adminId The id of the bot's admin.
     */
    public CommandHandler(String adminId) {
        this.adminId = adminId;
    }

    @Override
    public void onMessageCreate(DiscordAPI api, Message message) {
        if (message.getAuthor().isYourself()) {
            return;
        }
        String[] splitMessage = message.getContent().split(" ");
        String commandString = splitMessage[0];
        SimpleCommand command = commands.get(commandString.toLowerCase());
        if (command == null) {
            return;
        }
        if (message.isPrivateMessage() && !command.isPrivateMessages()) {
            return;
        }
        if (!message.isPrivateMessage() && !command.isChannelMessages()) {
            return;
        }
        if (command.isAdminOnly() && !message.getAuthor().getId().equals(adminId)) {
            return;
        }
        String[] args = Arrays.copyOfRange(splitMessage, 1, splitMessage.length);
        String reply = command.getExecutor().onCommand(api, commandString, args, message);
        if (reply != null) {
            message.reply(reply);
        }
    }

    /**
     * Gets a list with all commands.
     *
     * @return A list with all command.
     */
    public Collection<SimpleCommand> getCommands() {
        return Collections.unmodifiableCollection(commandsList);
    }

    /**
     * Registers a command.
     *
     * @param executor The executor of the command.
     */
    public void registerCommand(CommandExecutor executor) {
        Command commandAnnotation;
        try {
            commandAnnotation = executor.getClass()
                            .getMethod("onCommand", DiscordAPI.class, String.class, String[].class, Message.class)
                            .getAnnotation(Command.class);
        } catch (NoSuchMethodException e) {
            logger.warn("Not found onCommand method!", e);
            return;
        }
        if (commandAnnotation == null) {
            throw new IllegalArgumentException("The onCommand method does not have the @Command annotation!");
        }
        SimpleCommand command = new SimpleCommand(commandAnnotation, executor);
        commandsList.add(command);
        for (String alias : command.getAliases()) {
            commands.put(command.getCommandPrefix() + alias.toLowerCase(), command);
        }
    }

    public class SimpleCommand {

        private final CommandExecutor executor;
        private final boolean privateMessages;
        private final boolean channelMessages;
        private final String[] aliases;
        private final String description;
        private final String usage;
        private final boolean adminOnly;
        private final boolean showInHelpPage;
        private final String commandPrefix;

        public SimpleCommand(Command annotation, CommandExecutor executor) {
            this.executor = executor;
            privateMessages = annotation.privateMessages();
            channelMessages = annotation.channelMessages();
            aliases = annotation.aliases();
            if (aliases.length == 0) {
                throw new IllegalArgumentException("Aliases cannot be empty!");
            }
            description = annotation.description();
            adminOnly = annotation.adminOnly();
            showInHelpPage = annotation.showInHelpPage();
            commandPrefix = annotation.commandPrefix();
            usage = annotation.usage().equals("NONE") ? aliases[0] : annotation.usage();
        }

        public CommandExecutor getExecutor() {
            return executor;
        }

        public boolean isPrivateMessages() {
            return privateMessages;
        }

        public boolean isChannelMessages() {
            return channelMessages;
        }

        public String getDescription() {
            return description;
        }

        public String getUsage() {
            return usage;
        }

        public String[] getAliases() {
            return aliases;
        }

        public boolean isAdminOnly() {
            return adminOnly;
        }

        public boolean isShowInHelpPage() {
            return showInHelpPage;
        }

        public String getCommandPrefix() {
            return commandPrefix;
        }
    }

}
