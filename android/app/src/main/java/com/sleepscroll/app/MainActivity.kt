package com.sleepscroll.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sleepscroll.app.service.SleepController
import com.sleepscroll.app.ui.SleepScrollRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark, so always use light system-bar icons. The default follows the
        // system theme and gave dark (near-invisible) icons when the phone is in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { SleepScrollRoot() }
    }

    override fun onStart() {
        super.onStart()
        // A scheduled session may have been saved while Android refused to start its service in the
        // background (the process stayed alive, so Application.onCreate won't run again). We're in the
        // foreground now, so the start is allowed.
        SleepController.resumeOrFinish(this)
    }
}
