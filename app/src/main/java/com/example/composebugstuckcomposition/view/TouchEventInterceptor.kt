package com.example.composebugstuckcomposition.view

import android.view.MotionEvent

interface TouchEventInterceptor {
    fun onInterceptTouch(event: MotionEvent)
}
