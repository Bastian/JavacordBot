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
import com.google.common.base.Throwables;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.MessageDecoration;
import de.btobastian.javacordbot.util.commands.Command;
import de.btobastian.javacordbot.util.commands.CommandExecutor;
import javassist.*;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The execute command.
 */
public class ExecuteCommand implements CommandExecutor {

    /*
     * IMPORTANT: This code is fucking ugly. You shouldn't copy it!
     */
    @Override
    @Command(aliases = {"execute", "eval"}, description = "Executes java code", usage = "execute <code>", adminOnly = true)
    public String onCommand(DiscordAPI api, String command, String[] args, Message message) {
        final Thread[] executionThread = new Thread[1];
        Future<String> future = api.getThreadPool().getExecutorService().submit(() -> {
            executionThread[0] = Thread.currentThread();
            try {
                return firstTry(api, command, args, message);
            } catch (Throwable e) {
                return sendStack(e, message);
            }
        });
        Message reply;
        try {
            reply = message.reply("```Executing...```").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return sendStack(e, message);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return sendStack(e, message);
        }
        for (int i = 0; i < 60; i++) {
            try {
                String response = future.get(1, TimeUnit.SECONDS);
                reply.delete();
                return response;
            } catch (InterruptedException | ExecutionException e) {
                return sendStack(e, message);
            } catch (TimeoutException e) {
                if (executionThread[0] != null && i < 59) {
                    MessageBuilder builder = new MessageBuilder();
                    builder.append(MessageDecoration.CODE_LONG.getPrefix())
                            .appendNewLine()
                            .append("Execution details:")
                            .appendNewLine()
                            .append("Time running: ~" + (i + 1) + "s")
                            .appendNewLine()
                            .appendNewLine()
                            .append("Current stack trace:")
                            .appendNewLine();
                    for (StackTraceElement element : executionThread[0].getStackTrace()) {
                        if (element.getMethodName().equals("executeCode")
                                && element.getClassName().startsWith("ExecuteCode")) {
                            break;
                        }
                        builder.append("| ")
                                .append(element.getClassName())
                                .append("#")
                                .append(element.getMethodName())
                                .append("(line: ").append(String.valueOf(element.getLineNumber())).append(")")
                                .appendNewLine();
                    }
                    builder.append(MessageDecoration.CODE_LONG.getSuffix());
                    reply.edit(builder.toString());
                }
            }
        }
        MessageBuilder builder = new MessageBuilder()
                .append(MessageDecoration.CODE_LONG.getPrefix())
                .appendNewLine()
                .append("Execution canceled (took more than 60 seconds)");
        if (executionThread[0] != null) {
            builder.appendNewLine()
                    .appendNewLine()
                    .append("Execution details:")
                    .appendNewLine()
                    .append("Time ran: 60s")
                    .appendNewLine()
                    .appendNewLine()
                    .append("Complete Stack Trace when cancelled:")
                    .appendNewLine();
            for (StackTraceElement element : executionThread[0].getStackTrace()) {
                builder.append("| ")
                        .append(element.getClassName())
                        .append("#")
                        .append(element.getMethodName())
                        .append("(line: ").append(String.valueOf(element.getLineNumber())).append(")")
                        .appendNewLine();
            }
            builder.append(MessageDecoration.CODE_LONG.getSuffix());
        }
        future.cancel(true);
        reply.delete();
        return builder.toString();
    }

    private String firstTry(DiscordAPI api, String cmd, String[] args, Message message) {
        String code = Joiner.on(" ").join(args);
        // wrap the returned value cause javassist has some problems with primitive types
        code = code.replaceAll("return (.+);", "return String.valueOf($1);");
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(api.getClass()));
        pool.importPackage("de.btobastian.javacord.entities");
        pool.importPackage("de.btobastian.javacord.entities.message");
        pool.importPackage("de.btobastian.javacord.entities.permissions");
        pool.importPackage("de.btobastian.javacord.exceptions");
        pool.importPackage("de.btobastian.javacord.utils");
        pool.importPackage("de.btobastian.javacord");
        CtClass executeClass = pool.makeClass("ExecuteCode" + System.currentTimeMillis());
        try {
            executeClass.addMethod(
                    CtNewMethod.make(
                            "public java.lang.Object executeCode (DiscordAPI api, Message msg, Channel c, Server s, User u) { " + code + " }",
                            executeClass));
            Class<?> clazz = executeClass.toClass(api.getClass().getClassLoader(), api.getClass().getProtectionDomain());
            Object obj = clazz.newInstance();
            Method meth = clazz.getDeclaredMethod("executeCode", DiscordAPI.class, Message.class, Channel.class, Server.class, User.class);
            Server server = message.getChannelReceiver() != null ? message.getChannelReceiver().getServer() : null;
            return "```\n" + meth.invoke(
                    obj, api, message, message.getChannelReceiver(), server, message.getAuthor()).toString() + "```";
        } catch (Throwable e) {
            if (e instanceof CannotCompileException) {
                return secondTry(api, cmd, args, message);
            }
            return sendStack(e, message);
        }
    }

    private String secondTry(DiscordAPI api, String cmd, String[] args, Message message) {
        String code = Joiner.on(" ").join(args);
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(api.getClass()));
        CtClass executeClass = pool.makeClass("ExecuteCodeVoid" + System.currentTimeMillis());
        try {
            executeClass.addMethod(
                    CtNewMethod.make(
                            "public void executeCode (DiscordAPI api, Message msg, Channel c, Server s, User u) { " + code + " }",
                            executeClass));
            Class<?> clazz = executeClass.toClass(api.getClass().getClassLoader(), api.getClass().getProtectionDomain());
            Object obj = clazz.newInstance();
            Method meth = clazz.getDeclaredMethod("executeCode", DiscordAPI.class, Message.class, Channel.class, Server.class, User.class);
            Server server = message.getChannelReceiver() != null ? message.getChannelReceiver().getServer() : null;
            meth.invoke(obj, api, message, message.getChannelReceiver(), server, message.getAuthor());
            return "```\n" + "Executed!" + "\n```";
        } catch (Throwable e) {
            return sendStack(e, message);
        }
    }


    private String sendStack(Throwable e, Message message) {
        String stack = Throwables.getStackTraceAsString(e);
        if (stack.length() < 1989) {
            return "```\n" + stack + "\n```";
        }
        String[] split = stack.split("Caused by:");
        int i = 0;
        for (String piece : split) {
            try {
                message.reply("```\n" + (i++ > 0 ? "Caused by:" : "") + piece + "\n```").get();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }


}
