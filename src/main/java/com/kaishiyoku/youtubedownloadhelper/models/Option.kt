package com.kaishiyoku.youtubedownloadhelper.models

import java.io.File
import java.util.ArrayList

class Option(
        val inputValue: Int,
        val label: String,
        val actionFn: (channels: ArrayList<Channel>, youtubeDlFile: File) -> Unit
) : BaseOption() {

}