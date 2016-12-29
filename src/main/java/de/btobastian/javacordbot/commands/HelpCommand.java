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

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

import java.awt.*;

/**
 * The help command.
 */
public class HelpCommand implements CommandExecutor {

    private final CommandHandler commandHandler;

    /**
     * Creates a new instance of this class.
     *
     * @param commandHandler The command handler.
     */
    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Command(aliases = {"+help", "+commands"}, description = "Shows this page")
    public void onHelpCommand(Message message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("=== Commands ===");
        embed.setFooter("Powered by Javacord");
        embed.setColor(Color.GREEN);

        for (CommandHandler.SimpleCommand simpleCommand : commandHandler.getCommands()) {
            if (!simpleCommand.getCommandAnnotation().showInHelpPage()) {
                continue; // skip command
            }
            StringBuilder command = new StringBuilder();
            if (!simpleCommand.getCommandAnnotation().requiresMention()) {
                // the default prefix only works if the command does not require a mention
                command.append(commandHandler.getDefaultPrefix());
            }
            String usage = simpleCommand.getCommandAnnotation().usage();
            if (usage.isEmpty()) { // no usage provided, using the first alias
                usage = simpleCommand.getCommandAnnotation().aliases()[0];
            }
            command.append(usage);
            String description = simpleCommand.getCommandAnnotation().description();

            embed.addField(command.toString(), description, false);
        }
        message.reply("", embed);
    }

}
