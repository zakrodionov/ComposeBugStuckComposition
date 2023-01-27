package com.example.composebugstuckcomposition.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.composebugstuckcomposition.R
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class NotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TouchEventInterceptor,
    SwipeToDismissLayout.OnDismissListener {

    private val view: View = View.inflate(context, R.layout.view_notification, this)
    private val blurView = view.findViewById<BlurView>(R.id.blurView)
    private val swipeToDismissLayout =
        view.findViewById<SwipeToDismissLayout>(R.id.swipeToDismissLayout)

    init {
        val decorView = getActivityDecorView()
        val rootView = decorView?.findViewById(android.R.id.content) as ViewGroup
        val windowBackground = decorView.background

        blurView.setupWith(rootView, RenderScriptBlur(context)) // or RenderEffectBlur
            //.setFrameClearDrawable(windowBackground)
            .setBlurRadius(2f)

        swipeToDismissLayout.setOnDismissListener(this)
        swipeToDismissLayout.setOnTouchListener(this)
    }

    override fun onInterceptTouch(event: MotionEvent) {

    }

    override fun onDismiss() {
        (parent as? ViewGroup)?.removeView(this)
    }

    private fun getActivityDecorView(): View? {
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
}