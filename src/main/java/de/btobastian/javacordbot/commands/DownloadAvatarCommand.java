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
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import java.io.ByteArrayInputStream;

/**
 * The download avatar command.
 */
public class DownloadAvatarCommand implements CommandExecutor {

    @Command(aliases = {"+downloadAvatar"}, description = "Downloads the avatar of the user!", usage = "+downloadAvatar <@user>")
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        if (args.length != 1 || message.getMentions().size() != 1) {
            return "The first argument must be a user!";
        }
        message.getMentions().get(0).getAvatarAsByteArray(new FutureCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    message.replyFile(new ByteArrayInputStream(bytes), "avatar.jpg", "Avatar:", new FutureCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {

                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    message.reply("Error: " + e.getMessage());
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
