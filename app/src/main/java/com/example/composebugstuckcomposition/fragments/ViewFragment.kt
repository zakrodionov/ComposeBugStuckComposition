package com.example.composebugstuckcomposition.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.composebugstuckcomposition.R
import com.example.composebugstuckcomposition.view.NotificationView

class ViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            findViewById<Button>(R.id.btnShowNotification).setOnClickListener {
                showNotification()
            }

            findViewById<Button>(R.id.btnToggleProgress).setOnClickListener {
                val progressBar = findViewById<ProgressBar>(R.id.progress)
                if (progressBar.visibility == View.GONE) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            val tvColor = findViewById<TextView>(R.id.tvColor)
            tvColor.setOnClickListener {
                tvColor.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
        }
    }

    private fun showNotification() {
        val viewGroup =
            requireActivity().window?.decorView?.findViewById(android.R.id.content) as? ViewGroup
        val view = NotificationView(requireContext())
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER
        viewGroup?.addView(view, params)
        view.bringToFront()
    }
}