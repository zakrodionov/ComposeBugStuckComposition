package com.example.composebugstuckcomposition.view.custom

import android.content.Context
import android.graphics.Bitmap

interface IBlur {

    fun prepare(context: Context, buffer: Bitmap, radius: Float): Boolean

    fun release()

    fun blur(input: Bitmap, output: Bitmap)
}
