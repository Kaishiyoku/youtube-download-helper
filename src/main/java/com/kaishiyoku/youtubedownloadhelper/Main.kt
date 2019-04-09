package com.kaishiyoku.youtubedownloadhelper

import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.cls
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.defaultGson
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.isWindows
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.printPressToContinue
import com.kaishiyoku.youtubedownloadhelper.models.*
import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.TA_GridThemes
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import org.pmw.tinylog.Logger
import java.io.*
import java.net.URL
import java.util.*

var status = 1

fun getYoutubeDlFileName(): String {
    return "youtube-dl${if (isWindows()) ".exe" else ""}"
}

fun getYoutubeDlFile(): File {
    return File("third_party/${getYoutubeDlFileName()}")
}

fun exit(config: Config, youtubeDlFile: File) {
    status = 0
}

fun main(args: Array<String>) {
    convertChannelsConfigIfNeeded()

    val maxAgeOfYoutubeDlFileInDays = 30L

    val options = listOf(
            Option(1, "Start download", ::startDownload),
            Option(2, "Download single channel", ::startDownloadSingle),
            OptionSpacer(),
            Option(0, "Exit", ::exit)
    )

    downloadYoutubeDlIfNeeded(maxAgeOfYoutubeDlFileInDays)
    createChannelConfigIfNeeded()

    cls()

    try {
        while (status == 1) {
            val inputValue = showMenuOptions(options)

            cls()

            val foundOption: Option? = options.filterIsInstance<Option>().firstOrNull { it.inputValue == inputValue }

            if (foundOption == null) {
                println("Invalid option.")
            } else {
                foundOption.actionFn(loadChannelConfig(), getYoutubeDlFile())
            }

            println()
            println()
        }

        System.exit(0)

    } catch (e: IOException) {
        Logger.error(e)
    }
}

fun downloadYoutubeDlIfNeeded(maxAge: Long) {
    val dir = File("third_party")
    dir.mkdirs()

    val baseUrl = "https://yt-dl.org/downloads/latest/"

    // check if file already exists
    val youtubeDlFile = getYoutubeDlFile()
    val ageOfFile = Date().time - youtubeDlFile.lastModified()

    println(ageOfFile)
    println(maxAge * 24 * 60 * 60 * 1000)

    if (!youtubeDlFile.exists() || ageOfFile > maxAge * 24 * 60 * 60 * 1000) {
        println("Downloading youtube-dl tool from https://rg3.github.io/youtube-dl/ ...")

        val url: URL

        try {
            url = URL(baseUrl + getYoutubeDlFileName())
            val connection = url.openConnection()
            val `in` = connection.getInputStream()
            val fos = FileOutputStream(File("third_party/${getYoutubeDlFileName()}"))

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

            printPressToContinue()
        } catch (e: IOException) {
            Logger.error(e)
        }

    }
}

fun createChannelConfigIfNeeded() {
    try {
        val configFile = File("config.json")

        if (!configFile.exists()) {
            File("config/default_config.json").copyTo(File("config.json"))
        }
    } catch (e: IOException) {
        Logger.error(e)
    }

}

private fun loadChannelConfig(): Config {
    return Config.fromJson(File("config.json").readText())
}

private fun loadOldChannelConfig(): ArrayList<Channel> {
    val channels = ArrayList<Channel>()

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

    return channels
}

private fun showMenuOptions(options: List<BaseOption>): Int {
    val scanner = Scanner(System.`in`)

    val ctx = AT_Context()
    ctx.setGridTheme(TA_GridThemes.INSIDE_HORIZONTAL)
    ctx.width = 30
    val at = AsciiTable(ctx)
    at.addRule()
    at.addRow("Options:")
    at.addRule()

    options.forEach { it ->
        when (it) {
            is OptionSpacer -> at.addRow("")
            is Option -> at.addRow("${it.inputValue}: ${it.label}")
        }
    }

    at.addRule()

    println(at.render())

    println()

    print("> ")

    val option = scanner.nextInt()

    return option
}

fun startDownload(config: Config, youtubeDlFile: File) {
    for (channel in config.channels) {
        downloadChannel(channel, youtubeDlFile)
    }
}

fun downloadChannel(channel: Channel, youtubeDlFile: File) {
    val p: Process

    try {
        println("Starting downloads for \"" + channel.title + "\".")
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

        if (getCustomParams() != null) {
            newParams.add(getCustomParams()!!)
        }

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

private fun startDownloadSingle(config: Config, youtubeDlFile: File) {
    listChannels(config.channels)

    val scanner = Scanner(System.`in`)

    println("Select the channel # to be downloaded.")

    print("> ")

    val channelNumber = scanner.nextInt()

    try {
        println()

        downloadChannel(config.channels.get(channelNumber - 1), youtubeDlFile)
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
    at.addRow("# | TITLE", "URL", "LOCAL PATH")
    at.addRule()

    if (channels.size == 0) {
        at.addRow(null, null, "no channels yet")
    }

    println(channels[0].title)

    var i = 1

    for (channel in channels) {
        at.addRow(i.toString() + " | " + channel.title, channel.url, channel.localPath)

        if (i < channels.size) {
            at.addRule()
        }

        i++
    }

    at.addRule()

    println(at.render())
}

fun convertChannelsConfigIfNeeded() {
    if (!File("config.json").exists() && File("config/channels.txt").exists()) {
        println("Converting old config")

        val config = Config(loadOldChannelConfig())
        File("config.json").writeText(defaultGson().toJson(config))
    }
}

fun getCustomParams() : String? {
    val fileName = "config/custom_params.txt"

    return if (File(fileName).exists()) File(fileName).readText() else null
}