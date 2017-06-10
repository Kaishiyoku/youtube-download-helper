package com.kaishiyoku.youtubedownloadhelper;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        createChannelConfigIfNeeded();
    }

    private static void createChannelConfigIfNeeded() {
        File dir = new File("config");
        dir.mkdirs();

        try {
            File channelConfigFile = new File("config/channels.txt");

            if (!channelConfigFile.exists()) {
                PrintWriter writer = new PrintWriter(channelConfigFile, "UTF-8");
                writer.println("# Enter here the YouTube channels you want to download in the following format, one channel per row:");
                writer.println("# <URL>;<LOCAL_PATH>");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
