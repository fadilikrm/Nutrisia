package com.example.nutrisia

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var skipButton: MaterialButton
    private lateinit var finishButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        viewPager = findViewById(R.id.viewPager)
        skipButton = findViewById(R.id.btn_skip)
        finishButton = findViewById(R.id.btn_finish)

        val onboardingItems = listOf(
            OnboardingItem("Welcome!", "Selamat datang di Nutrisia", R.mipmap.nutrisia),
            OnboardingItem("Body Mass Index", "Ketahui tinggi dan berat idealmu", R.drawable.bmi1),
            OnboardingItem("Penuhi kebutuhan kalori", "Hitung kalori harian lebih cepat dan mudah", R.drawable.calories2),
            OnboardingItem("Mulai aktivitas fisik", "Imbangi kebutuhan kalori dan aktivitas fisikmu", R.drawable.treadmill)
        )

        val adapter = OnboardingPagerAdapter(this, onboardingItems)
        viewPager.adapter = adapter

        // Menambahkan PageTransformer untuk efek transisi
        viewPager.setPageTransformer(FadePageTransformer())

        // Setup TabLayout dengan ViewPager
        TabLayoutMediator(findViewById(R.id.tabLayout), viewPager) { _, _ -> }.attach()

        // Menambahkan animasi fade-in pada tombol Skip dan Next
        skipButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        finishButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        // Skip button action
        skipButton.setOnClickListener {
            goToLoginActivity()
        }

        // Finish button action
        finishButton.setOnClickListener {
            if (viewPager.currentItem == onboardingItems.size - 1) {
                goToLoginActivity()
            } else {
                viewPager.currentItem += 1
            }
        }
    }

    // Fungsi untuk berpindah ke LoginActivity dengan animasi fade-out
    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // animasi fade
        finish() // Menutup OnboardingActivity agar tidak bisa kembali
    }
}

// PageTransformer dengan efek fade
class FadePageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: android.view.View, position: Float) {
        when {
            position < -1 -> page.alpha = 0f
            position <= 1 -> page.alpha = 1 - Math.abs(position)
            else -> page.alpha = 0f
        }
    }
}
