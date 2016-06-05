/*
 * Copyright (C) 2016 Bastian Oppermann
 * 
 * This file is part of Javacord.
 * 
 * Javacord is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser general Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Javacord is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.btobastian.javacordbot.commands;

import com.google.common.base.Joiner;
import de.btobastian.javacord.utils.LoggerUtil;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

public class TodoCommand implements CommandExecutor {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(TodoCommand.class);
    Object fileLock = new Object();

    @Command(aliases = "+todo", requiredPermissions = "todo", description = "A simple todo list", usage = "+todo [key] [value]")
    public String onTodoCommand(String[] args) {
        synchronized (fileLock) {
            // no arguments = show keys
            if (args.length == 0) {
                Iterator<String> keys = null;
                try {
                    keys = getKeys();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!keys.hasNext()) {
                    return "No entries \"" + args[0] + "\" found!";
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("```xml");
                while (keys.hasNext()) {
                    stringBuilder.append("\n ● ").append(keys.next());
                }
                stringBuilder.append("\n```");
                return stringBuilder.toString();
            }

            JSONArray data;
            try {
                data = loadData(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }

            // only one argument = get values
            if (args.length == 1) {
                if (data == null) {
                    return "No entry with key \"" + args[0] + "\" found!";
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("```xml");
                for (int i = 0; i < data.length(); i++) {
                    stringBuilder.append("\n ● ").append(data.get(i));
                }
                stringBuilder.append("\n```");
                return stringBuilder.toString();
            }

            // more than one argument = add value

            if (data == null) {
                data = new JSONArray();
            }
            data.put(Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length)));
            try {
                saveData(args[0], data);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
            return "Entry added!";
        }
    }

    public JSONArray loadData(String key) throws IOException {
        File file = new File("todo.json");
        if (!file.exists()) {
            return null;
        }
        String jsonText = Joiner.on("\n").join(Files.readAllLines(file.toPath()));
        if (jsonText == null || jsonText.isEmpty()) {
            jsonText = "{}";
        }
        JSONObject jsonObject = new JSONObject(jsonText);
        if (!jsonObject.has(key)) {
            return null;
        }
        return jsonObject.getJSONArray(key);
    }

    public void saveData(String key, JSONArray object) throws IOException {
        File file = new File("todo.json");
        if (!file.exists()) {
            file.createNewFile();
        }
        String jsonText = Joiner.on("\n").join(Files.readAllLines(file.toPath()));
        if (jsonText == null || jsonText.isEmpty()) {
            jsonText = "{}";
        }
        JSONObject jsonObject = new JSONObject(jsonText);
        jsonObject.put(key, object);
        Files.write(file.toPath(), jsonObject.toString(2).getBytes());
    }

    public Iterator<String> getKeys() throws IOException {
        File file = new File("todo.json");
        if (!file.exists()) {
            return null;
        }
        String jsonText = Joiner.on("\n").join(Files.readAllLines(file.toPath()));
        if (jsonText == null || jsonText.isEmpty()) {
            jsonText = "{}";
        }
        JSONObject jsonObject = new JSONObject(jsonText);
        return jsonObject.keys();
    }

}
