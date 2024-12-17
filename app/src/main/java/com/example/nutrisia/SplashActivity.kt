package com.example.nutrisia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashTime: Long = 3000 // Durasi splash screen (3 detik)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Mendapatkan elemen logo atau image view di splash screen
        val logoImageView: ImageView = findViewById(R.id.splash_logo)

        // Menambahkan animasi fade-in pada logo
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logoImageView.startAnimation(fadeInAnimation)

        // Handler untuk menjalankan delay sebelum pindah ke aktivitas berikutnya
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences: SharedPreferences = getSharedPreferences("PREF", MODE_PRIVATE)
            val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

            if (isFirstRun) {
                val intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
                startActivity(intent)
                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
            } else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            // Menambahkan animasi slide dengan masuk dari kanan dan keluar ke kiri
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_left_left)

            finish() // Tutup SplashActivity agar tidak bisa kembali ke sini
        }, splashTime)
    }
}
