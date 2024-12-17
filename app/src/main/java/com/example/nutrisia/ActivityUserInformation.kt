package com.example.nutrisia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nutrisia.databinding.ActivityUserInformationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityUserInformation : AppCompatActivity() {

    private lateinit var binding: ActivityUserInformationBinding
    private var profileData: SelectProfile? = null // Variabel untuk menyimpan data profil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil ID user dari sesi login
        val userId = getLoggedInUserId()
        if (userId != null) {
            fetchUserProfile(userId)
        } else {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Tombol logout
        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }

        // Tombol navigasi ke halaman Edit Profile
        binding.buttonEditProfile.setOnClickListener {
            profileData?.let { data ->
                val intent = Intent(this@ActivityUserInformation, ProfileActivity::class.java).apply {
                    putExtra("foto", "https://22tkja.com/api/kelompok_1/${data.foto}")
                    putExtra("fullname", data.fullname)
                    putExtra("username", data.username)
                    putExtra("email", data.email)
                    putExtra("tb", data.tb?.toString())
                    putExtra("bb", data.bb?.toString())
                    putExtra("jk", data.jk)
                    putExtra("age", data.age?.toString())
                    putExtra("aktivitas", data.aktivitas)
                    putExtra("password", data.password)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Profil belum dimuat", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigasi dengan ikon di toolbar
        val iconAbout = findViewById<ImageView>(R.id.IconAbout)
        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val iconProfile = findViewById<ImageView>(R.id.IconProfile)

        // Set tindakan untuk ikon Pengaturan
        iconAbout.setOnClickListener {
            // Membuka Activity About Us
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }

        // Set tindakan untuk ikon Beranda
        homeIcon.setOnClickListener {
            // Membuka DiaryActivity
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }

        // Set tindakan untuk ikon Profil
        iconProfile.setOnClickListener {
            // Membuka ActivityUserInformation (mungkin ke halaman ini sendiri jika ingin)
            val intent = Intent(this, ActivityUserInformation::class.java)
            startActivity(intent)
        }
    }

    /**
     * Ambil profil dari server dengan Retrofit
     */
    private fun fetchUserProfile(userId: Int) {
        val params = mapOf("id" to userId.toString())

        RetrofitClient.instance.getProfile(params).enqueue(object : Callback<SelectProfileResponse> {
            override fun onResponse(call: Call<SelectProfileResponse>, response: Response<SelectProfileResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    profileData = response.body()?.data?.getOrNull(0)
                    if (profileData != null) {
                        populateProfileFields(profileData!!)
                    } else {
                        Toast.makeText(this@ActivityUserInformation, "Profil tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ActivityUserInformation, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SelectProfileResponse>, t: Throwable) {
                Toast.makeText(this@ActivityUserInformation, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Mengisi data profil di tampilan dengan data dari server
     */
    private fun populateProfileFields(data: SelectProfile) {
        binding.editTextFullName.setText(data.fullname)
        binding.editUsername.setText(data.username)
        binding.editTextEmail.setText(data.email)
        binding.editTb.setText(data.tb?.toString())
        binding.editBb.setText(data.bb?.toString())
        binding.editAktivitas.setText(data.aktivitas)
        binding.editJk.setText(data.jk)
        binding.editAge.setText(data.age?.toString())
        var originalPassword = data.password

        // Tampilkan placeholder di EditText
        binding.editTextPassword.apply {
            setText("****") // Placeholder
        }
        // Debug: Log URL yang akan dimuat untuk foto
        val imageUrl = "https://22tkja.com/api/kelompok_1/${data.foto}"
        Log.d("Image URL", imageUrl)

        // Gunakan Glide untuk memuat foto profil dari URL dengan penanganan placeholder dan error
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile) // Gambar placeholder
            .error(R.drawable.ic_profile) // Gambar saat error
            .into(binding.imageViewProfile)
    }

    /**
     * Ambil ID yang sudah disimpan di SharedPreferences setelah login
     */
    private fun getLoggedInUserId(): Int? {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

    /**
     * Keluar dari sesi dan kembali ke halaman login
     */
    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityUserInformation, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
