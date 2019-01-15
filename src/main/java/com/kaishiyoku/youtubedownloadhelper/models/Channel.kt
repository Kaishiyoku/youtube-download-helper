package com.kaishiyoku.youtubedownloadhelper.models

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
    }
}
