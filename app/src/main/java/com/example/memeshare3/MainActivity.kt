package com.example.memeshare3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar.visibility = View.GONE

        Handler().postDelayed({
            progressBar.visibility = View.VISIBLE
        }, 1000)

        Handler().postDelayed({
            progressBar.visibility = View.GONE
        }, 2250)

        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            startActivity(Intent(this,MenuScreen::class.java))

            // close this activity
            finish()
        }, 2350)
    }
}