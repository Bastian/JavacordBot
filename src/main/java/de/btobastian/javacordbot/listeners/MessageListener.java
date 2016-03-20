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
package de.btobastian.javacordbot.listeners;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import org.jsoup.Jsoup;

import java.util.HashMap;

/**
 * A message listener of this bot.
 */
public class MessageListener implements MessageCreateListener {

    private final HashMap<String, ChatterBotSession> sessions = new HashMap<>();

    @Override
    public void onMessageCreate(DiscordAPI api, Message message) {
        // the bot is sometimes pretty slow. We don't want it to block our listener thread
        api.getThreadPool().getExecutorService().submit((Runnable) () -> handleCleverbot(api, message));
    }

    private void handleCleverbot(DiscordAPI api, Message message) {
        if (!message.isPrivateMessage()
                && !message.getChannelReceiver().getName().equals("cleverbot")
                && !message.getChannelReceiver().getName().equals("german_cleverbot")
                && !message.getContent().startsWith("+cleverbot ")) {
            return; // no valid cleverbot channel
        }
        // the question
        String question = message.getContent();
        if (question.startsWith("+cleverbot ")) {
            // remove the +cleverbot at the beginning of the message
            question = question.replaceFirst("\\+cleverbot ", "");
        }
        // gets the id of the channel
        String id = message.isPrivateMessage() ?
                message.getUserReceiver().getId() : message.getChannelReceiver().getId();
        // is there already a session?
        ChatterBotSession session = sessions.get(id);
        // create a new session if no exists or someone wants to start a new one
        if (session == null || question.equalsIgnoreCase("+newsession")) {
            try {
                ChatterBot bot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
                session = bot.createSession();
                sessions.put(id, session);
            } catch (Exception e) {
                e.printStackTrace();
                message.reply("Error: " + e.getMessage());
                return;
            }
        }
        if (question.startsWith("+")) { // do not react to commands
            if (question.equalsIgnoreCase("+newsession")) { // Exception: new session command
                message.reply("Started new session!");
            }
            return;
        }
        // send the typing message cause it looks more human :)
        if (message.isPrivateMessage()) {
            message.getUserReceiver().type();
        } else {
            message.getChannelReceiver().type();
        }
        // translation stuff:
        if (!message.isPrivateMessage() && message.getChannelReceiver().getName().equals("german_cleverbot")) {
            question = translate("de", "en", question);
        }

        try {
            String answer = session.think(question);
            answer = Jsoup.parse(answer).text(); // remove all html stuff, e.g. links
            if (answer.isEmpty()) { // sometimes the bot does not answer, e.g. if you say "shut up"
                return;
            }
            // translate answer if we are in a german cleverbot channel
            if (!message.isPrivateMessage() && message.getChannelReceiver().getName().equals("german_cleverbot")) {
                answer = translate("en", "de", answer);
            }
            message.reply(answer);
        } catch (Exception e) {
            e.printStackTrace();
            message.reply("Error: " + e.getMessage());
        }
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
