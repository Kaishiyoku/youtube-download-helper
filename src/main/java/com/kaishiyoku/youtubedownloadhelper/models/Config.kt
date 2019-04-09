package com.kaishiyoku.youtubedownloadhelper.models

import com.kaishiyoku.youtubedownloadhelper.helper.ConsoleHelper.defaultGson
import java.util.*

class Config(channels: ArrayList<Channel>) {
    var channels: ArrayList<Channel> = channels
        private set

    companion object {
        fun fromJson(json: String): Config {
            return defaultGson().fromJson(json, Config::class.java)
        }
    }
}
