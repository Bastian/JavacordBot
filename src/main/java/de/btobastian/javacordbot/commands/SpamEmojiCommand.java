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

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * The spam emoji command.
 */
public class SpamEmojiCommand implements CommandExecutor {

    @Command(aliases = {"+spamEmoji"}, description = "Spams an emoji!", usage = "+spamEmoji <messageId>", requiredPermissions = "spam", async = true)
    public String onCommand(DiscordAPI api, Server server, String[] args, Message message) {
        if (args.length != 1) {
            return "Invalid amount of parameters!";
        }

        Message targetMessage = api.getMessageById(args[0]);

        if (targetMessage == null) {
            return "Unknown message";
        }

        Collection<Emoji> emojis = EmojiManager.getAll();

        for (int i = 0; i < 18; i++) {
            try {
                targetMessage.addUnicodeReaction(random(emojis).getUnicode()).get();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return "Spammed!";
    }

    private <T> T random(Collection<T> coll) {
        int num = (int) (Math.random() * coll.size());
        for (T t: coll) if (--num < 0) return t;
        throw new AssertionError();
    }

}
