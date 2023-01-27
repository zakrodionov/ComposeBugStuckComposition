package com.example.composebugstuckcomposition.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator

class SwipeDetector(
    private val swipeLayout: SwipeToDismissLayout,
    private val dismissLimit: Int
) : View.OnTouchListener {

    private var x1: Float = 0.toFloat()
    private var x2: Float = 0.toFloat()
    private var deltaX: Float = 0.toFloat()
    private var action = ACTION.NONE
    private var animator: ViewPropertyAnimator? = null
    private var onTouchInterceptor: TouchEventInterceptor? = null

    init {
        this.swipeLayout.setOnTouchListener(this)
        initAnimationListener()
    }

    private fun initAnimationListener() {
        animator = swipeLayout.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (action == ACTION.DISMISSING) {
                    swipeLayout.dismiss()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                if (action == ACTION.DISMISSING) {
                    swipeLayout.dismiss()
                } else {
                    swipeLayout.translationX = 0f
                }
            }
        })
    }

    fun cancel() {
        animator?.apply {
            setListener(null)
            cancel()
        }
    }

    fun setOnTouchInterceptor(touchEventInterceptor: TouchEventInterceptor) {
        onTouchInterceptor = touchEventInterceptor
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        onTouchInterceptor?.onInterceptTouch(motionEvent)
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                action = ACTION.NONE
                deltaX = 0f
                x1 = motionEvent.rawX
            }

            MotionEvent.ACTION_MOVE -> {
                x2 = motionEvent.rawX
                deltaX = Math.abs(x2 - x1)
                when (if (x2 > x1) Direction.RIGHT else Direction.LEFT) {
                    Direction.LEFT -> view.translationX = -deltaX
                    Direction.RIGHT -> view.translationX = deltaX
                }
            }

            MotionEvent.ACTION_UP -> {
                when (if (x2 > x1) Direction.RIGHT else Direction.LEFT) {
                    Direction.LEFT -> if (deltaX > dismissLimit) {
                        action = ACTION.DISMISSING
                        view.animate()
                            .translationX((swipeLayout.left - swipeLayout.width).toFloat()).start()
                    } else {
                        action = ACTION.NONE
                        view.animate().translationX(0f).start()
                    }
                    Direction.RIGHT -> if (deltaX > dismissLimit) {
                        action = ACTION.DISMISSING
                        view.animate().translationX(swipeLayout.right.toFloat()).start()
                    } else {
                        action = ACTION.NONE
                        view.animate().translationX(0f).start()
                    }
                }
            }
        }
        return true
    }

    private enum class Direction {
        LEFT,
        RIGHT
    }

    private enum class ACTION {
        DISMISSING,
        NONE
    }
}
