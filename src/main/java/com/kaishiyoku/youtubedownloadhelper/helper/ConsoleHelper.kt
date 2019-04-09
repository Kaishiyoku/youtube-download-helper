package com.kaishiyoku.youtubedownloadhelper.helper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.pmw.tinylog.Logger
import java.io.IOException

object ConsoleHelper {
    fun isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows")

    fun println(str: String) = System.out.println(" $str")

    fun print(str: String) = System.out.print(" $str")

    fun cls() {
        print("\u001b[H\u001b[2J")
        System.out.flush()
    }

    fun printPressToContinue() {
        println("")
        println("Press any key to continue")

        try {
            System.`in`.read()
        } catch (e: IOException) {
            Logger.error(e)
        }
    }

    fun defaultGson(): Gson {
        return GsonBuilder().setPrettyPrinting().create()
    }
}
