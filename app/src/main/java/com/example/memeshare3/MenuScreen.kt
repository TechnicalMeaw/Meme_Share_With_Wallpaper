package com.example.memeshare3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_menu_screen.*

class MenuScreen : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_screen)

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                adView.loadAd(adRequest)
            }
        }


//        MobileAds.initialize(this) {
//            mInterstitialAd = InterstitialAd(this)
//            mInterstitialAd.adUnitId = "ca-app-pub-4382859222380692/7440515947"
//            mInterstitialAd.loadAd(AdRequest.Builder().build())
//        }


    }

    fun btnImages(view: View) {
        val intent = Intent(this, Download::class.java)
        intent.putExtra("category", "Image")
        startActivity(intent)
    }

    fun btnMemes(view: View) {
        val intent = Intent(this, Download::class.java)
        intent.putExtra("category", "Meme")
        startActivity(intent)
    }

    //for back
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

//            if (mInterstitialAd.isLoaded) {
//                mInterstitialAd.show()
//            }

            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}