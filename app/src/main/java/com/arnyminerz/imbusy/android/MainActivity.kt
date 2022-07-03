package com.arnyminerz.imbusy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.arnyminerz.imbusy.android.activity.IntroActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, IntroActivity::class.java))
    }
}
