package com.kaishiyoku.youtubedownloadhelper;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    private final static long MAX_AGE_OF_YOUTUBE_DL_FILE_IN_DAYS = 30;

    private static List<Channel> channels = new ArrayList<>();

    public static void main(String[] args) {
        downloadYoutubeDlIfNeeded();
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
                case 4:
                    cls();
                    startDownload();
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

    private static int showMenuOptions() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Options:");
        System.out.println(" 1: List channels");
        System.out.println(" 2: Add channel");
        System.out.println(" 3: Remove channel");
        System.out.println(" 4: Start download");
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

    private static void downloadYoutubeDlIfNeeded() {
        File dir = new File("third_party");
        dir.mkdirs();

        String baseUrl = "https://yt-dl.org/downloads/latest/";
        String fileName = "youtube-dl";

        if (System.getProperty("os.name").startsWith("Windows")) {
            fileName += ".exe";
        }

        // check if file already exists
        File youtubeDlFile = new File("third_party/" + fileName);

        long ageOfFile = new Date().getTime() - youtubeDlFile.lastModified();

        boolean fileNotExistsOrIsVeryOld = !youtubeDlFile.exists();

        if (!fileNotExistsOrIsVeryOld && ageOfFile > MAX_AGE_OF_YOUTUBE_DL_FILE_IN_DAYS * 24 * 60 * 60 * 1000) {
            fileNotExistsOrIsVeryOld = true;
        }

        if (fileNotExistsOrIsVeryOld) {
            System.out.println("Downloading youtube-dl tool from https://rg3.github.io/youtube-dl/...");

            URL url;

            try {
                url = new URL(baseUrl + fileName);
                URLConnection connection = url.openConnection();
                InputStream in = connection.getInputStream();
                FileOutputStream fos = new FileOutputStream(new File("third_party/" + fileName));

                byte[] buf = new byte[512];

                while (true) {
                    int len = in.read(buf);

                    if (len == -1) {
                        break;
                    }

                    fos.write(buf, 0, len);
                }

                in.close();
                fos.flush();
                fos.close();

                System.out.println("...done.");

                pressToContinue();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void startDownload() {

    }

    private static void pressToContinue() {
        System.out.println("");
        System.out.println("Press any key to continue");

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
