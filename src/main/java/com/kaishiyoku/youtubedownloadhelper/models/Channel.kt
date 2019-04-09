package com.kaishiyoku.youtubedownloadhelper.models

import com.google.gson.annotations.SerializedName
import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.defaultGson

class Channel(title: String, url: String, localPath: String) {
    var title: String = title
        private set
    var url: String = url
        private set
    @SerializedName("local_path")
    var localPath: String = localPath
        private set

    companion object {
        fun fromConfigLine(configLine: String): Channel {
            val (title, url, localPath) = configLine.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()

            return Channel(title, url, localPath)
        }

        fun fromJson(json: String) : Channel {
            return defaultGson().fromJson(json, Channel::class.java)
        }
    }
}
