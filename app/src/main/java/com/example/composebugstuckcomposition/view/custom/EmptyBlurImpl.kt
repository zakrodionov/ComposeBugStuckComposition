package com.example.composebugstuckcomposition.view.custom

import android.content.Context
import android.graphics.Bitmap

class EmptyBlurImpl : IBlur {
    override fun prepare(context: Context, buffer: Bitmap, radius: Float): Boolean {
        return false
    }

    override fun release() {
    }

    override fun blur(input: Bitmap, output: Bitmap) {
    }
}
