package com.kaishiyoku.youtubedownloadhelper.models

class Channel {
    var description: String
        private set
    var url: String
        private set
    var localPath: String
        private set

    constructor(description: String, url: String, localPath: String) {
        this.description = description
        this.url = url
        this.localPath = localPath
    }

    constructor(configLine: String) {
        val values = configLine.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        description = values[0]
        url = values[1]
        localPath = values[2]
    }

    fun toConfigLine(): String {
        return arrayOf(description, url, localPath).joinToString(";")
    }
}
