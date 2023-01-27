package com.example.composebugstuckcomposition.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.composebugstuckcomposition.R
import com.example.composebugstuckcomposition.ui.theme.ComposeBugStuckCompositionTheme
import com.example.composebugstuckcomposition.view.NotificationView

class ComposeFragment : Fragment() {

    private val progress = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Content(progress.value,
                    showNotification = { showNotification() },
                    toggleProgress = { progress.value = !progress.value }
                )
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

@Composable
fun Content(
    progress: Boolean,
    showNotification: () -> Unit,
    toggleProgress: () -> Unit,
) {
    ComposeBugStuckCompositionTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.secondary
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(id = R.string.long_text))
                if (progress) {
                    CircularProgressIndicator(modifier = Modifier.size(200.dp))
                }
                Button(onClick = { showNotification() }) {
                    Text(text = "Show notification")
                }
                Button(onClick = { toggleProgress() }) {
                    Text(text = "Toggle progress")
                }

                val paintText = remember { mutableStateOf(false) }
                Text(
                    text = stringResource(id = R.string.long_text),
                    color = if (paintText.value) Color.Red else Color.Black,
                    modifier = Modifier.clickable { paintText.value = !paintText.value }
                )
                Text(text = stringResource(id = R.string.long_text))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeBugStuckCompositionTheme {
        Content(true, {}, {})
    }
}