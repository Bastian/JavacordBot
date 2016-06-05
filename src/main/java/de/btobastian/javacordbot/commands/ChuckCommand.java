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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.MessageDecoration;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * The chuck command.
 */
public class ChuckCommand implements CommandExecutor {

    @Command(aliases = {"+chuck", "+joke"}, description = "Tells a Chuck Norris joke")
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        JSONObject jsonResponse;
        try {
            jsonResponse = Unirest.get("http://api.icndb.com/jokes/random").asJson().getBody().getObject();
        } catch (UnirestException e) {
            return "Error:" + e.getMessage();
        }
        String joke = jsonResponse.getJSONObject("value").getString("joke").replace("&quot;", "\"");
        Jsoup.parse(joke).text();
        return new MessageBuilder().appendDecoration(MessageDecoration.CODE_LONG, joke).toString();
    }
}
