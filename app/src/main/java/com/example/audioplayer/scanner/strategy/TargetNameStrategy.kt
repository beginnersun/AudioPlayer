package com.example.audioplayer.scanner.strategy

import com.example.audioplayer.scanner.WeChatScanner
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.sqlite.Voice
import java.io.File

class TargetNameStrategy(
    private val targetCode: String,
    startTime:Long = System.currentTimeMillis(),
    endTime: Long = startTime - defaultSpaceTime
) : TimeSpaceStrategy(startTime,endTime) {

    override fun predicate(file: File): Boolean =
        Voice.convertPathToUserCode(file.name) == targetCode && inSpaceTime(file)


}