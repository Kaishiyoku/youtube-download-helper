package com.kaishiyoku.youtubedownloadhelper.models

import com.google.gson.Gson
import java.util.ArrayList

class Config(channels: ArrayList<Channel>) {
    var channels: ArrayList<Channel> = channels
        private set

    companion object {
        fun fromJson(json: String): Config {
            return Gson().fromJson(json, Config::class.java)
        }
    }
}
