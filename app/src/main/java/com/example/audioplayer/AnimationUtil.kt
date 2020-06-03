package com.example.audioplayer

import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation

fun getRotateAnimation(duration: Long,pivotX:Float,pivotY: Float,fromDegrees :Float = 0f,toDegrees:Float = 180f) =
    RotateAnimation(fromDegrees,toDegrees,pivotX,pivotY).apply {
        interpolator = LinearInterpolator()
        setDuration(duration)
        fillAfter = true
    }

fun rotate180(duration:Long,view:View){
    val startRotate = if (view.tag == null || (view.tag as Float) == 180f) 0f else 180f
    val rotateAnimation = getRotateAnimation(duration,view.pivotX,view.pivotY,startRotate,startRotate+180f)
    view.tag = startRotate
    view.startAnimation(rotateAnimation)

}
