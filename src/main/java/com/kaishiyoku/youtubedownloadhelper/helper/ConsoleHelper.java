package com.kaishiyoku.youtubedownloadhelper.helper;

import org.pmw.tinylog.Logger;

import java.io.IOException;

public class ConsoleHelper {
    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void render(String str) {
        System.out.println(str);
    }

    public static void println() {
        System.out.println("");
    }

    public static void println(String str) {
        System.out.println(" " + str);
    }

    public static void print(String str) {
        System.out.print(" " + str);
    }

    public static void pressToContinue() {
        println("");
        println("Press any key to continue");

        try {
            System.in.read();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
