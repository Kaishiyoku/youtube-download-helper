package com.kaishiyoku.youtubedownloadhelper;

import com.kaishiyoku.youtubedownloadhelper.models.Channel;
import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.*;

public class Main {
    private final static long MAX_AGE_OF_YOUTUBE_DL_FILE_IN_DAYS = 30;

    private static List<Channel> channels = new ArrayList<>();
    private static Map<Integer, String> options = new LinkedHashMap<Integer, String>() {{
        put(1, "List channels");
        put(2, "Add channel");
        put(3, "Remove channel");
        put(4, "Start download");
        put(5, "Download single channel");
        put(-1, "");
        put(0, "Exit");
    }};

    public static void main(String[] args) {
        downloadYoutubeDlIfNeeded();
        createChannelConfigIfNeeded();

        loadChannelConfig();

        cls();

        int status = 1;

        while (status == 1) {
            int option = showMenuOptions();

            cls();

            try {
                switch (option) {
                    case 1:
                        listChannels();
                        break;
                    case 2:
                        addChannel();
                        break;
                    case 3:
                        removeChannel();
                        break;
                    case 4:
                        startDownload();
                        break;
                    case 5:
                        startDownloadSingle();
                        break;
                    case 0:
                        status = 0;
                        break;
                    default:
                        println("Invalid option.");
                }
            } catch (Exception e) {
                Logger.error(e);
            }

            println();
            println();
        }

        System.exit(0);
    }

    private static int showMenuOptions() {
        Scanner scanner = new Scanner(System.in);

        AT_Context ctx = new AT_Context();
        ctx.setGridTheme(TA_GridThemes.INSIDE_HORIZONTAL);
        ctx.setWidth(30);
        AsciiTable at = new AsciiTable(ctx);
        at.addRule();
        at.addRow("Options:");
        at.addRule();

        for (Map.Entry<Integer, String> option : options.entrySet()) {
            if (option.getKey() == -1) {
                at.addRow(option.getValue());
            } else {
                at.addRow(option.getKey() + ": " + option.getValue());
            }
        }

        at.addRule();

        render(at.render());

        println();

        print("> ");

        int option = scanner.nextInt();

        return option;
    }

    private static void loadChannelConfig() {
        try {
            File channelConfigFile = new File("config/channels.txt");
            BufferedReader reader = new BufferedReader(new FileReader(channelConfigFile));
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    channels.add(new Channel(line));
                }
            }
        } catch (FileNotFoundException e) {
            Logger.error(e);
        } catch (IOException e) {
            Logger.error(e);
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
            Logger.error(e);
        }
    }

    private static void listChannels() {
        AT_Context ctx = new AT_Context();
        ctx.setGridTheme(TA_GridThemes.FULL);
        ctx.setWidth(95);

        AsciiTable at = new AsciiTable(ctx);
        at.setTextAlignment(TextAlignment.LEFT);
        at.addRule();
        at.addRow("# | DESCRIPTION", "URL", "LOCAL PATH");
        at.addRule();

        if (channels.size() == 0) {
            at.addRow(null, null, "no channels yet");
        }

        int i = 1;

        for (Channel channel : channels) {
            at.addRow(i + " | " + channel.getDescription(), channel.getUrl(), channel.getLocalPath());

            if (i < channels.size()) {
                at.addRule();
            }

            i++;
        }

        at.addRule();

        render(at.render());
    }

    private static void addChannel() {
        Scanner scanner = new Scanner(System.in);

        print("Description: ");
        String description = scanner.nextLine();

        print("URL: ");
        String url = scanner.nextLine();

        print("Local path: ");
        String localPath = scanner.nextLine();

        Channel channel = new Channel(description, url, localPath);

        channels.add(channel);
        saveChannels(channel);

        println("Channel added.");
    }

    private static void removeChannel() {
        listChannels();

        Scanner scanner = new Scanner(System.in);

        print("Remove channel #: ");

        int channelNumber = scanner.nextInt();

        channels.remove(channelNumber - 1);

        saveChannels();

        println("Channel removed.");
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
            lines.forEach(channelLines::add);
        } catch (IOException e) {
            Logger.error(e);
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
            Logger.error(e);
        } finally {
            writer.close();
        }
    }

    private static void downloadYoutubeDlIfNeeded() {
        File dir = new File("third_party");
        dir.mkdirs();

        String baseUrl = "https://yt-dl.org/downloads/latest/";

        // check if file already exists
        File youtubeDlFile = new File("third_party/" + getYoutubeDlFileName());

        long ageOfFile = new Date().getTime() - youtubeDlFile.lastModified();

        boolean fileNotExistsOrIsVeryOld = !youtubeDlFile.exists();

        if (!fileNotExistsOrIsVeryOld && ageOfFile > MAX_AGE_OF_YOUTUBE_DL_FILE_IN_DAYS * 24 * 60 * 60 * 1000) {
            fileNotExistsOrIsVeryOld = true;
        }

        if (fileNotExistsOrIsVeryOld) {
            println("Downloading youtube-dl tool from https://rg3.github.io/youtube-dl/...");

            URL url;

            try {
                url = new URL(baseUrl + getYoutubeDlFileName());
                URLConnection connection = url.openConnection();
                InputStream in = connection.getInputStream();
                FileOutputStream fos = new FileOutputStream(new File("third_party/" + getYoutubeDlFileName()));

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

                println("...done.");

                pressToContinue();
            } catch (MalformedURLException e) {
                Logger.error(e);
            } catch (FileNotFoundException e) {
                Logger.error(e);
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    private static void startDownloadSingle() {
        listChannels();

        Scanner scanner = new Scanner(System.in);

        println("Select the channel # to be downloaded.");

        print("> ");

        int channelNumber = scanner.nextInt();

        try {
            println();

            downloadChannel(channels.get(channelNumber - 1), getYoutubeDlFile());
        } catch (IndexOutOfBoundsException e) {
            println("Invalid channel #.");
        }
    }

    private static File getYoutubeDlFile() {
        return new File("third_party/" + getYoutubeDlFileName());
    }

    private static void startDownload() {
        File youtubeDlFile = getYoutubeDlFile();

        for (Channel channel : channels) {
            downloadChannel(channel, youtubeDlFile);
        }
    }

    private static void downloadChannel(Channel channel, File youtubeDlFile) {
        Process p;

        try {
            println("Starting downloads for \"" + channel.getDescription() + "\".");
            println();

            List<String> newParams = new ArrayList<>();

            if (isWindows()) {
                newParams.add("cmd");
                newParams.add("/c");
            }

            newParams.add(youtubeDlFile.getAbsolutePath());
            newParams.add("--yes-playlist");
            newParams.add("--output");
            newParams.add(channel.getLocalPath() + "/%(title)s.%(ext)s");
            newParams.add("--ignore-errors");
            newParams.add("--no-overwrites");
            newParams.add(channel.getUrl());

            ProcessBuilder builder = new ProcessBuilder(newParams);
            builder.redirectErrorStream(true);
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while (true) {
                line = r.readLine();

                if (line == null) {
                    break;
                }

                Logger.info(line);
            }

            println();
            println();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private static String getYoutubeDlFileName() {
        String fileName = "youtube-dl";

        if (isWindows()) {
            fileName += ".exe";
        }

        return fileName;
    }
}
