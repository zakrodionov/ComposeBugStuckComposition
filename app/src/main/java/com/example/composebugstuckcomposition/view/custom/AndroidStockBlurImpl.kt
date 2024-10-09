package com.example.composebugstuckcomposition.view.custom

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.renderscript.*

@Suppress("DEPRECATION")
internal class AndroidStockBlurImpl : IBlur {
    private var renderScript: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null
    private var blurInput: Allocation? = null
    private var blurOutput: Allocation? = null

    override fun prepare(context: Context, buffer: Bitmap, radius: Float): Boolean {
        if (renderScript == null) {
            try {
                renderScript = RenderScript.create(context)
                blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
            } catch (e: RSRuntimeException) {
                return if (isDebug(context)) {
                    throw e
                } else {
                    // In release mode, just ignore
                    release()
                    false
                }
            }
        }
        blurScript?.setRadius(radius) ?: return false
        blurInput = Allocation.createFromBitmap(
            renderScript, buffer,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )?.also {
            blurOutput = Allocation.createTyped(renderScript, it.type)
        }
        return true
    }

    override fun release() {
        blurInput?.destroy()
        blurInput = null

        blurOutput?.destroy()
        blurOutput = null

        blurScript?.destroy()
        blurScript = null

        renderScript?.destroy()
        renderScript = null
    }

    override fun blur(input: Bitmap, output: Bitmap) {
        blurInput?.copyFrom(input)
        blurScript?.setInput(blurInput)
        blurScript?.forEach(blurOutput)
        blurOutput?.copyTo(output)
    }

    // android:debuggable="true" in AndroidManifest.xml (auto set by build tool)
    var DEBUG: Boolean? = null

    fun isDebug(ctx: Context?): Boolean {
        if (DEBUG == null && ctx != null) {
            DEBUG = ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        }
        return DEBUG === java.lang.Boolean.TRUE
    }
}
