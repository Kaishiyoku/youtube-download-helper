package com.kaishiyoku.youtubedownloadhelper.helper

import org.pmw.tinylog.Logger

import java.io.IOException

object ConsoleHelper {

    val isWindows: Boolean
        get() = System.getProperty("os.name").startsWith("Windows")

    fun cls() {
        print("\u001b[H\u001b[2J")
        System.out.flush()
    }

    fun render(str: String) {
        println(str)
    }

    fun println() {
        println("")
    }

    fun println(str: String) {
        System.out.println(" $str")
    }

    fun print(str: String) {
        System.out.print(" $str")
    }

    fun pressToContinue() {
        println("")
        println("Press any key to continue")

        try {
            System.`in`.read()
        } catch (e: IOException) {
            Logger.error(e)
        }

    }
}
