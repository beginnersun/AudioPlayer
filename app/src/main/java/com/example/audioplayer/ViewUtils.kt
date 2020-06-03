package com.example.audioplayer

import android.content.Context
import android.util.TypedValue

fun dp2px(context: Context,dpValue:Float):Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,context.resources.displayMetrics).toInt()



fun px2dp(context: Context,pxValue:Float):Float =
    (pxValue / context.resources.displayMetrics.scaledDensity + 0.5f)