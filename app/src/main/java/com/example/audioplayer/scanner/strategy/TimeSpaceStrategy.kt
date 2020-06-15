package com.example.audioplayer.scanner.strategy

import com.example.audioplayer.scanner.WeChatScanner
import java.io.File

open class TimeSpaceStrategy(
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long = startTime - WeChatScanner.defaultSpaceTime
) : WeChatScanner.FilterStrategy {
    override fun predicate(file: File): Boolean = inSpaceTime(file)

    /**
     * 处于间隔时间内  (默认为间隔时间为一个月)
     */
    protected fun inSpaceTime(file: File) :Boolean =
        file.lastModified() in (startTime + 1) until endTime
}