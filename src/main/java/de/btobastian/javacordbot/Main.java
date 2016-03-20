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
package de.btobastian.javacordbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.utils.LoggerUtil;
import de.btobastian.javacordbot.commands.*;
import de.btobastian.javacordbot.util.commands.CommandHandler;
import org.slf4j.Logger;

/**
 * The main class of the plugin.
 */
public class Main implements FutureCallback<DiscordAPI> {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(Main.class);

    private String adminId = null;

    /**
     * The main method.
     *
     * @param args The arguments. Not used.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Invalid amount of arguments! First argument must be valid token and second the admin id!");
            System.exit(-1);
            return;
        }
        new Main().login(args[0], args[1]);
    }

    /**
     * Successfully connected to discord.
     *
     * @param api The discord api.
     */
    @Override
    public void onSuccess(DiscordAPI api) {
        logger.info("Connected to discord account {}", api.getYourself());
        CommandHandler commandHandler = new CommandHandler(adminId);
        api.registerListener(commandHandler);

        // register commands
        commandHandler.registerCommand(new HelpCommand(commandHandler));
        commandHandler.registerCommand(new PingCommand());
        commandHandler.registerCommand(new DownloadAvatarCommand());
        commandHandler.registerCommand(new ChuckCommand());
        commandHandler.registerCommand(new ReconnectCommand());
    }

    /**
     * Connecting failed!
     *
     * @param throwable The reason why connection failed.
     */
    @Override
    public void onFailure(Throwable throwable) {
        logger.error("Could not connect to discord!", throwable);
    }

    /**
     * Attempts to login.
     *
     * @param token A valid token.
     */
    public void login(String token, String adminId) {
        DiscordAPI api = Javacord.getApi(token, false);
        this.adminId = adminId;
        api.connect(this);
    }

}
