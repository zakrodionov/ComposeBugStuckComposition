package com.example.composebugstuckcomposition.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.composebugstuckcomposition.R

class CustomNotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val view: View = View.inflate(context, R.layout.view_custom_notification, this)
}