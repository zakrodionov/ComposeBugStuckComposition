package com.example.composebugstuckcomposition.view.custom

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.ui.graphics.Color
import com.example.composebugstuckcomposition.R
import kotlin.math.max

class CustomRealtimeBlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var downSampleFactor: Float
    private var overlayColor: Int
    private var blurRadius: Float

    private var blur: IBlur? = null
    private var paint: Paint? = null
    private var renderingCount = 0
    private var blurImpl = 0
    private var dirty = false
    private var isRendering = false
    private var differentRoot = false
    private var bitmapToBlur: Bitmap? = null
    private var blurredBitmap: Bitmap? = null
    private var blurringCanvas: Canvas? = null
    private var decorView: View? = null
    private var rectSrc = Rect()
    private var rectDst = Rect()

    init {
        blur = getBlur()
        blurRadius = context.resources.getDimension(R.dimen.dimens_10dp)
        downSampleFactor = 4f
        overlayColor = -0x55000001

        paint = Paint()
    }

    private fun getBlur(): IBlur {
        if (blurImpl == 0) {
            blurImpl = try {
                val impl = AndroidStockBlurImpl()
                val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                impl.prepare(context, bmp, 4F)
                impl.release()
                bmp.recycle()
                1
            } catch (e: Throwable) {
                0
            }
        }
        return when (blurImpl) {
            1 -> AndroidStockBlurImpl()
            else -> EmptyBlurImpl()
        }
    }

    fun setBlurRadius(radius: Float) {
        if (blurRadius != radius) {
            blurRadius = radius
            dirty = true
            invalidate()
        }
    }

    fun setDownSampleFactor(factor: Float) {
        require(factor > 0) { "Downsample factor must be greater than 0." }
        if (downSampleFactor != factor) {
            downSampleFactor = factor
            dirty = true // may also change blur radius
            releaseBitmap()
            invalidate()
        }
    }

    fun setOverlayColor(color: Int) {
        if (overlayColor != color) {
            overlayColor = color
            invalidate()
        }
    }

    private fun releaseBitmap() {
        bitmapToBlur?.recycle()
        bitmapToBlur = null
        blurredBitmap?.recycle()
        blurredBitmap = null
    }

    protected fun release() {
        releaseBitmap()
        blur?.release()
    }

    protected fun prepare(): Boolean {
        if (blurRadius == 0f) {
            release()
            return false
        }
        var downSampleFactor: Float = this.downSampleFactor
        var radius: Float = blurRadius / downSampleFactor
        if (radius > 25) {
            downSampleFactor = downSampleFactor * radius / 25
            radius = 25f
        }
        val width = width
        val height = height
        val scaledWidth = max(1, (width / downSampleFactor).toInt())
        val scaledHeight = max(1, (height / downSampleFactor).toInt())
        var dirty: Boolean = dirty
        if (blurringCanvas == null || blurredBitmap == null ||
            blurredBitmap?.width != scaledWidth || blurredBitmap?.height != scaledHeight
        ) {
            dirty = true
            releaseBitmap()
            var r = false
            try {
                bitmapToBlur =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                bitmapToBlur?.let {
                    blurringCanvas = Canvas(it)
                } ?: return false

                blurredBitmap =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                if (blurredBitmap == null) return false

                r = true
            } catch (e: OutOfMemoryError) {
                // Bitmap.createBitmap() may cause OOM error
                // Simply ignore and fallback
            } finally {
                if (!r) {
                    release()
                    // ignore exceptions
                }
            }
        }
        if (dirty) {
            bitmapToBlur?.let {
                if (blur?.prepare(context, it, radius) == true) {
                    dirty = false
                } else {
                    return false
                }
            } ?: return false
        }

        return true
    }

    protected fun blur(bitmapToBlur: Bitmap?, blurredBitmap: Bitmap?) {
        if (bitmapToBlur == null || blurredBitmap == null) return
        blur?.blur(bitmapToBlur, blurredBitmap)
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        drawBlurOnDecorView()
        true
    }

    private fun drawBlurOnDecorView() {
        // post is important
        decorView?.post(drawBlurOnDecorViewRunnable)
    }

    private val drawBlurOnDecorViewRunnable = Runnable {
        val locations = IntArray(2)
        val oldBmp: Bitmap? = blurredBitmap
        if (decorView != null && isShown && prepare()) {
            val redrawBitmap = blurredBitmap != oldBmp
            decorView?.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]

            // just erase transparent
            bitmapToBlur?.eraseColor(overlayColor and 0xffffff)
            blurringCanvas?.let { canvas ->
                val rc: Int = canvas.save()
                isRendering = true
                renderingCount++
                bitmapToBlur?.let { bitmap ->
                    try {
                        canvas.scale(1f * bitmap.width / width, 1f * bitmap.height / height)
                        canvas.translate(-x.toFloat(), -y.toFloat())
                        decorView?.background?.draw(canvas)
                        decorView?.draw(canvas)
                    } catch (e: StopException) {
                    } finally {
                        isRendering = false
                        renderingCount--
                        canvas.restoreToCount(rc)
                    }
                    blur(bitmapToBlur, blurredBitmap)
                    if (redrawBitmap || differentRoot) {
                        invalidate()
                    }
                }
            }
        }
    }

    protected fun getActivityDecorView(): View? {
        var ctx = context
        var i = 0
        while (i < 4 && ctx != null && ctx !is Activity && ctx is ContextWrapper) {
            ctx = ctx.baseContext
            i++
        }
        return if (ctx is Activity) {
            ctx.window.decorView
        } else {
            null
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        decorView = getActivityDecorView()
        decorView?.let {
            it.viewTreeObserver.addOnPreDrawListener(preDrawListener)
            differentRoot = it.rootView !== rootView
            if (differentRoot) it.postInvalidate()
        } ?: run {
            differentRoot = false
        }
    }

    override fun onDetachedFromWindow() {
        decorView?.removeCallbacks(drawBlurOnDecorViewRunnable)
        decorView?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
        release()
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas) {
        when {
            isRendering -> {
                // Quit here, don't draw views above me
                throw STOP_EXCEPTION
            }

            renderingCount > 0 -> {
                // Doesn't support blurview overlap on another blurview
            }

            else -> {
                super.draw(canvas)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurredBitmap(canvas, blurredBitmap, overlayColor)
    }

    /**
     * Custom draw the blurred bitmap and color to define your own shape
     *
     * @param canvas
     * @param blurredBitmap
     * @param overlayColor
     */
    protected fun drawBlurredBitmap(canvas: Canvas, blurredBitmap: Bitmap?, overlayColor: Int) {
        if (blurredBitmap != null) {
            rectSrc.right = blurredBitmap.width
            rectSrc.bottom = blurredBitmap.height
            rectDst.right = width
            rectDst.bottom = height
            canvas.drawBitmap(blurredBitmap, rectSrc, rectDst, null)
        }
        paint?.let {
            it.color = overlayColor
            canvas.drawRect(rectDst, it)
        }
    }

    private class StopException : RuntimeException()

    private val STOP_EXCEPTION = StopException()
}
