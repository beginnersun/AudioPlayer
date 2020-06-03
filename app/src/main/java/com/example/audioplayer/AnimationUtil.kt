package com.example.audioplayer

import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation

fun getRotateAnimation(duration: Long,pivotX:Float,pivotY: Float,fromDegrees :Float = 0f,toDegrees:Float = 180f) =
    RotateAnimation(fromDegrees,toDegrees,fromDegrees,toDegrees).apply {
        interpolator = LinearInterpolator()
        setDuration(duration)
    }

fun rotate180(duration:Long,view:View){
    val startRotate = if (view.tag == null) 0f else 180f
    val rotateAnimation = getRotateAnimation(duration,view.pivotX,view.pivotY,startRotate,startRotate+180f)
    view.tag = startRotate
    view.animation = rotateAnimation
}