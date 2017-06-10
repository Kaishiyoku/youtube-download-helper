package com.kaishiyoku.youtubedownloadhelper;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;

import java.io.File;
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

        int status = 1;

        while (status == 1) {
            int option = showMenuOptions();

            switch (option) {
                case 1:
                    listChannels();
                    break;
                case 0:
                    status = 0;
                    break;
                default:
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
        System.out.println(" 0: Exit");
        System.out.println("");

        int option = scanner.nextInt();

        return option;
    }

    private static void loadChannelConfig() {
        File channelConfigFile = new File("config/channels.txt");

        try (Stream<String> lines = Files.lines(channelConfigFile.toPath())) {
            lines.filter(s -> !s.startsWith("#")).forEach(s -> channels.add(new Channel(s)));
        } catch (IOException ex) {

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
        //ctx.setGrid(A8_Grids.lineDoubleBlocks());
        ctx.setGridTheme(TA_GridThemes.FULL);

        AsciiTable at = new AsciiTable(ctx);
        at.addRule();
        at.addRow("DESCRIPTION", "URL", "LOCAL PATH");
        at.addRule();

        if (channels.size() == 0) {
            at.addRow(null, null, "no channels yet");
        }

        for (Channel channel : channels) {
            at.addRow(channel.getDescription(), channel.getUrl(), channel.getLocalPath());
        }

        at.addRule();

        System.out.println(at.render());
    }
}
