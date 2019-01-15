package com.kaishiyoku.youtubedownloadhelper

import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.cls
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.isWindows
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.pressToContinue
import com.kaishiyoku.youtubedownloadhelper.models.Channel
import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.TA_GridThemes
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import org.pmw.tinylog.Logger
import java.io.*
import java.net.URL
import java.util.*

fun main(args: Array<String>) {
    val maxAgeOfYoutubeDlFileInDays = 30
    val channels = ArrayList<Channel>()
    val options = object : LinkedHashMap<Int, String>() {
        init {
            put(1, "Start download")
            put(2, "Download single chanel")
            put(-1, "")
            put(0, "Exit")
        }
    }
    val youtubeDlFileName = "youtube-dl${if (isWindows()) ".exe" else ""}"
    val youtubeDlFile = File("third_party/$youtubeDlFileName")

    downloadYoutubeDlIfNeeded(youtubeDlFileName, maxAgeOfYoutubeDlFileInDays)
    createChannelConfigIfNeeded()

    loadChannelConfig(channels)

    cls()

    var status = 1

    try {
        while (status == 1) {
            val option = showMenuOptions(options)

            cls()

            when (option) {
                1 -> startDownload(channels, youtubeDlFile)
                2 -> startDownloadSingle(channels, youtubeDlFile)
                0 -> status = 0
                else -> println("Invalid option.")
            }

            println()
            println()

            System.exit(0)
        }

    } catch (e: IOException) {
        Logger.error(e)
    }
}

fun downloadYoutubeDlIfNeeded(youtubeDlFileName: String, maxAge: Int) {
    val dir = File("third_party")
    dir.mkdirs()

    val baseUrl = "https://yt-dl.org/downloads/latest/"

    // check if file already exists
    val youtubeDlFile = File("third_party/$youtubeDlFileName")

    val ageOfFile = Date().time - youtubeDlFile.lastModified()

    var fileNotExistsOrIsVeryOld = !youtubeDlFile.exists()

    if (!fileNotExistsOrIsVeryOld && ageOfFile > maxAge * 24 * 60 * 60 * 1000) {
        fileNotExistsOrIsVeryOld = true
    }

    if (fileNotExistsOrIsVeryOld) {
        println("Downloading youtube-dl tool from https://rg3.github.io/youtube-dl/...")

        val url: URL

        try {
            url = URL(baseUrl + youtubeDlFileName)
            val connection = url.openConnection()
            val `in` = connection.getInputStream()
            val fos = FileOutputStream(File("third_party/$youtubeDlFileName"))

            val buf = ByteArray(512)

            while (true) {
                val len = `in`.read(buf)

                if (len == -1) {
                    break
                }

                fos.write(buf, 0, len)
            }

            `in`.close()
            fos.flush()
            fos.close()

            println("...done.")

            pressToContinue()
        } catch (e: IOException) {
            Logger.error(e)
        }

    }
}

fun createChannelConfigIfNeeded() {
    val dir = File("config")
    dir.mkdirs()

    try {
        val channelConfigFile = File("config/channels.txt")

        if (!channelConfigFile.exists()) {
            val writer = PrintWriter(channelConfigFile, "UTF-8")
            writer.println("# Enter here the YouTube channels you want to download in the following format, one channel per row:")
            writer.println("# The description field is for visibility only and will not be used by the downloader.")
            writer.println("# <DESCRIPTION>;<URL>;<LOCAL_PATH>")
            writer.close()
        }
    } catch (e: IOException) {
        Logger.error(e)
    }

}

private fun loadChannelConfig(channels: ArrayList<Channel>) {
    try {
        val channelConfigFile = File("config/channels.txt")
        val reader = BufferedReader(FileReader(channelConfigFile))
        val iterator = reader.lineSequence().iterator()

        while (iterator.hasNext()) {
            val line = iterator.next();

            if (!line.startsWith("#")) {
                channels.add(Channel.fromConfigLine(line))
            }
        }
    } catch (e: IOException) {
        Logger.error(e)
    }

}

private fun showMenuOptions(options: LinkedHashMap<Int, String>): Int {
    val scanner = Scanner(System.`in`)

    val ctx = AT_Context()
    ctx.setGridTheme(TA_GridThemes.INSIDE_HORIZONTAL)
    ctx.width = 30
    val at = AsciiTable(ctx)
    at.addRule()
    at.addRow("Options:")
    at.addRule()

    for ((key, value) in options) {
        if (key == -1) {
            at.addRow(value)
        } else {
            at.addRow(key.toString() + ": " + value)
        }
    }

    at.addRule()

    println(at.render())

    println()

    print("> ")

    val option = scanner.nextInt()

    return option
}

fun startDownload(channels: ArrayList<Channel>, youtubeDlFile: File) {
    for (channel in channels) {
        downloadChannel(channel, youtubeDlFile)
    }
}

fun downloadChannel(channel: Channel, youtubeDlFile: File) {
    val p: Process

    try {
        println("Starting downloads for \"" + channel.description + "\".")
        println()

        val newParams = ArrayList<String>()

        if (isWindows()) {
            newParams.add("cmd")
            newParams.add("/c")
        }

        newParams.add(youtubeDlFile.absolutePath)
        newParams.add("--yes-playlist")
        newParams.add("--output")
        newParams.add(channel.localPath + "/%(title)s.%(ext)s")
        newParams.add("--ignore-errors")
        newParams.add("--no-overwrites")
        newParams.add(channel.url)

        val builder = ProcessBuilder(newParams)
        builder.redirectErrorStream(true)
        p = builder.start()
        val r = BufferedReader(InputStreamReader(p.inputStream))
        var line: String?

        while (true) {
            line = r.readLine()

            if (line == null) {
                break
            }

            Logger.info(line)
        }

        println()
        println()
    } catch (e: IOException) {
        Logger.error(e)
    }

}

private fun startDownloadSingle(channels: ArrayList<Channel>, youtubeDlFile: File) {
    listChannels(channels)

    val scanner = Scanner(System.`in`)

    println("Select the channel # to be downloaded.")

    print("> ")

    val channelNumber = scanner.nextInt()

    try {
        println()

        downloadChannel(channels[channelNumber - 1], youtubeDlFile)
    } catch (e: IndexOutOfBoundsException) {
        println("Invalid channel #.")
    }
}

private fun listChannels(channels: ArrayList<Channel>) {
    val ctx = AT_Context()
    ctx.setGridTheme(TA_GridThemes.FULL)
    ctx.width = 95

    val at = AsciiTable(ctx)
    at.setTextAlignment(TextAlignment.LEFT)
    at.addRule()
    at.addRow("# | DESCRIPTION", "URL", "LOCAL PATH")
    at.addRule()

    if (channels.size == 0) {
        at.addRow(null, null, "no channels yet")
    }

    var i = 1

    for (channel in channels) {
        at.addRow(i.toString() + " | " + channel.description, channel.url, channel.localPath)

        if (i < channels.size) {
            at.addRule()
        }

        i++
    }

    at.addRule()

    println(at.render())
}
