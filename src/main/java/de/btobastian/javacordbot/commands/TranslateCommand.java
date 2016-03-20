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

import com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacordbot.util.commands.Command;
import de.btobastian.javacordbot.util.commands.CommandExecutor;

import java.util.Arrays;

/**
 * The translate command.
 */
public class TranslateCommand implements CommandExecutor {

    @Override
    @Command(aliases = {"translate"}, description = "Translates the given text", usage = "translate <from> <to> <text>")
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        if (args.length < 3) {
            return "Too less arguments!";
        }
        String text = Joiner.on(" ").join(Arrays.copyOfRange(args, 2, args.length));
        String translation = translate(args[0], args[1], text);
        if (translation.matches("'(.+)' IS AN INVALID (TARGET|SOURCE) LANGUAGE \\. EXAMPLE: LANGPAIR=EN\\|IT USING 2 LETTER ISO OR RFC3066 LIKE ZH-CN\\. ALMOST ALL LANGUAGES SUPPORTED BUT SOME MAY HAVE NO CONTENT")) {
            return "Unknown language tag. Here's a list with lots of tags: http://www.w3schools.com/tags/ref_language_codes.asp";
        }
        return translation;
    }

    /**
     * Translates the given text.
     *
     * @param from The language of the input.
     * @param to The language the text should be translated to.
     * @param text The text which should be translated.
     * @return The translated text or an error message.
     */
    private String translate(String from, String to, String text) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get("http://mymemory.translated.net/api/get")
                    .queryString("q", text)
                    .queryString("langpair", from + "|" + to)
                    .queryString("ie", "UTF-32")
                    .asJson();
        } catch (UnirestException e) {
            return "Error: " + e.getMessage();
        }
        return response.getBody().getObject().getJSONObject("responseData").getString("translatedText");
    }

}
