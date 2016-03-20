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

import de.btobastian.javacord.utils.LoggerUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * The plugin as bukkit plugin.
 */
public class JavacordBot extends JavaPlugin {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(JavacordBot.class);

    @Override
    public void onEnable() {
        if (!getConfig().contains("token")) {
            getConfig().addDefault("token", "your bot`s token");
            getConfig().addDefault("adminId", "your id");
            saveConfig();
            logger.info("Created config.yml file. Please enter a valid token and restart the server!");
            getPluginLoader().disablePlugin(this);
            return;
        }
        new Main().login(getConfig().getString("token"), getConfig().getString("adminId"));
    }

}
