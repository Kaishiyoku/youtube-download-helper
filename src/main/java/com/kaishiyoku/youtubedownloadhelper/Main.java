package com.kaishiyoku.youtubedownloadhelper;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    private static List<Channel> channels = new ArrayList<>();

    public static void main(String[] args) {
        createChannelConfigIfNeeded();

        loadChannelConfig();

        cls();

        int status = 1;

        while (status == 1) {
            int option = showMenuOptions();

            switch (option) {
                case 1:
                    cls();
                    listChannels();
                    break;
                case 2:
                    cls();
                    addChannel();
                    break;
                case 3:
                    cls();
                    removeChannel();
                    break;
                case 0:
                    status = 0;
                    break;
                default:
                    cls();
                    System.out.println("Invalid option.");
            }

            System.out.println("");
            System.out.println("");
        }

        System.exit(0);
    }

    private static int showMenuOptions()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Options:");
        System.out.println(" 1: List channels");
        System.out.println(" 2: Add channel");
        System.out.println(" 3: Remove channel");
        System.out.println(" 0: Exit");
        System.out.println("");

        int option = scanner.nextInt();

        return option;
    }

    private static void loadChannelConfig() {
        File channelConfigFile = new File("config/channels.txt");

        try (Stream<String> lines = Files.lines(channelConfigFile.toPath())) {
            lines.filter(s -> !s.startsWith("#")).forEach(s -> channels.add(new Channel(s)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createChannelConfigIfNeeded() {
        File dir = new File("config");
        dir.mkdirs();

        try {
            File channelConfigFile = new File("config/channels.txt");

            if (!channelConfigFile.exists()) {
                PrintWriter writer = new PrintWriter(channelConfigFile, "UTF-8");
                writer.println("# Enter here the YouTube channels you want to download in the following format, one channel per row:");
                writer.println("# The description field is for visibility only and will not be used by the downloader.");
                writer.println("# <DESCRIPTION>;<URL>;<LOCAL_PATH>");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listChannels() {
        AT_Context ctx = new AT_Context();
        ctx.setGridTheme(TA_GridThemes.FULL);

        AsciiTable at = new AsciiTable(ctx);
        at.addRule();
        at.addRow("#", "DESCRIPTION", "URL", "LOCAL PATH");
        at.addRule();

        if (channels.size() == 0) {
            at.addRow(null, null, null, "no channels yet");
        }

        int i = 1;

        for (Channel channel : channels) {
            at.addRow(i, channel.getDescription(), channel.getUrl(), channel.getLocalPath());

            i++;
        }

        at.addRule();

        System.out.println(at.render());
    }

    private static void addChannel() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Description: ");
        String description = scanner.next();

        System.out.print("URL: ");
        String url = scanner.next();

        System.out.print("Local path: ");
        String localPath = scanner.next();

        Channel channel = new Channel(description, url, localPath);

        channels.add(channel);
        saveChannels(channel);
    }

    private static void removeChannel() {
        listChannels();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Remove channel #: ");

        int channelNumber = scanner.nextInt();

        channels.remove(channelNumber - 1);

        saveChannels();

        System.out.println("Channel removed");
        listChannels();
    }

    private static void saveChannels() {
        saveChannels(null);
    }

    private static void saveChannels(Channel channel) {
        // save to file
        File channelConfigFile = new File("config/channels.txt");
        PrintWriter writer = null;

        List<String> channelLines = new ArrayList<>();

        try (Stream<String> lines = Files.lines(channelConfigFile.toPath())) {
            lines.forEach(s -> channelLines.add(s));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (channel != null) {
            channelLines.add(channel.toConfigLine());
        }

        try {
            writer = new PrintWriter(channelConfigFile);

            for (String line : channelLines) {
                writer.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
