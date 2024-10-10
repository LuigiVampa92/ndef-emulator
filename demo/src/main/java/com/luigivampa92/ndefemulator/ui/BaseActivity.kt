package com.luigivampa92.ndefemulator.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.luigivampa92.ndefemulator.R

abstract class BaseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
}