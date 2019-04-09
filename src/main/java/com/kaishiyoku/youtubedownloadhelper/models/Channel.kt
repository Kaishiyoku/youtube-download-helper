package com.kaishiyoku.youtubedownloadhelper.models

import com.google.gson.Gson
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.defaultGson

class Channel(description: String, url: String, localPath: String) {
    var description: String = description
        private set
    var url: String = url
        private set
    var localPath: String = localPath
        private set

    companion object {
        fun fromConfigLine(configLine: String): Channel {
            val (description, url, localPath) = configLine.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()

            return Channel(description, url, localPath)
        }

        fun fromJson(json: String) : Channel {
            return defaultGson().fromJson(json, Channel::class.java)
        }
    }
}
