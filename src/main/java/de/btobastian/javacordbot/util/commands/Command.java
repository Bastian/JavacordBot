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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used for the command executor.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * Gets whether the executor should listen to private messages or not.
     *
     * @return Whether the executor should listen to private messages or not.
     */
    boolean privateMessages() default false;

    /**
     * Gets whether the executor should listen to channel messages or not.
     *
     * @return Whether the executor should listen to channel messages or not.
     */
    boolean channelMessages() default true;

    /**
     * Gets the commands the executor should listen to. The first element is the main command.
     *
     * @return The commands the executor should listen to.
     */
    String[] aliases();

    /**
     * Gets the description of the command.
     *
     * @return The description of the command.
     */
    String description();

    /**
     * Gets the usage of the command.
     *
     * @return The usage of the command.
     */
    String usage() default "NONE";

    /**
     * Gets whether the command is only for admins or not.
     *
     * @return Whether the command is only for admins or not.
     */
    boolean adminOnly() default false;

    /**
     * Gets whether the command should be shown in the help page or not.
     *
     * @return Whether the command should be shown if the help page or not.
     */
    boolean showInHelpPage() default true;

    /**
     * Gets the prefix of the command.
     *
     * @return The prefix of the command.
     */
    String commandPrefix() default "+";
}
