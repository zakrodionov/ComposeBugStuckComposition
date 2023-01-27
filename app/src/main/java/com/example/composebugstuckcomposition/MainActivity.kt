package com.example.composebugstuckcomposition

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.composebugstuckcomposition.fragments.ComposeFragment
import com.example.composebugstuckcomposition.fragments.ViewFragment

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val showCompose = false
        if (showCompose) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.Container, ComposeFragment()).commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.Container, ViewFragment()).commit()
        }
    }
}

