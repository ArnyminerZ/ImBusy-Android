package com.arnyminerz.imbusy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.arnyminerz.imbusy.android.activity.IntroActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onResume() {
        super.onResume()

        if (auth.currentUser == null)
            startActivity(Intent(this, IntroActivity::class.java))
    }
}
