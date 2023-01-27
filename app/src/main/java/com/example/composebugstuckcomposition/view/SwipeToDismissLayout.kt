package com.example.composebugstuckcomposition.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

open class SwipeToDismissLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var swipeDetector: SwipeDetector? = null
    private var dismissLimit = 100
    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    fun setOnTouchListener(touchEventInterceptor: TouchEventInterceptor) {
        swipeDetector?.setOnTouchInterceptor(touchEventInterceptor)
    }

    fun dismiss() {
        onDismissListener?.onDismiss()
    }

    fun animateDismiss() {
        visibility = View.GONE
        onDismissListener?.onDismiss()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            translationX = 0f
        }
        super.setVisibility(visibility)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.visibility = visibility
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        visibility = savedState.visibility
        super.onRestoreInstanceState(savedState.superState)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        swipeDetector = SwipeDetector(this, dismissLimit)
    }

    override fun onDetachedFromWindow() {
        if (swipeDetector != null) {
            swipeDetector!!.cancel()
        }
        super.onDetachedFromWindow()
    }

    class SavedState : BaseSavedState {

        var visibility: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            visibility = source.readInt()
        }

        constructor(source: Parcel, loader: ClassLoader) : super(source, loader)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(visibility)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    interface OnDismissListener {
        fun onDismiss()
    }
}
