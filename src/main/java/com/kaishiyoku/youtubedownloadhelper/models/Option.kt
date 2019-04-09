package com.kaishiyoku.youtubedownloadhelper.models

import java.io.File

class Option(
        val inputValue: Int,
        val label: String,
        val actionFn: (config: Config, youtubeDlFile: File) -> Unit
) : BaseOption() {

}