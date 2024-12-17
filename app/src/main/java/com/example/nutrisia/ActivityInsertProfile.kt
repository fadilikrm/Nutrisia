package com.example.nutrisia

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class ActivityInsertProfile : AppCompatActivity() {

    private var userId by Delegates.notNull<Int>()
    private lateinit var editTextBB: EditText
    private lateinit var editTextTB: EditText
    private lateinit var editTextUsia: EditText
    private lateinit var radioGroupJk: RadioGroup
    private lateinit var radioGroupAktivitas: RadioGroup
    private lateinit var buttonInsertProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_profile_activity)

        // Mengambil user_id dari Intent
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak valid", Toast.LENGTH_SHORT).show()
            finish() // Tutup activity jika user_id tidak valid
            return
        }

        // Inisialisasi view
        editTextBB = findViewById(R.id.editTextBB)
        editTextTB = findViewById(R.id.editTextTB)
        editTextUsia = findViewById(R.id.editTextUsia)
        radioGroupJk = findViewById(R.id.radioGroupJk)
        radioGroupAktivitas = findViewById(R.id.radioGroupAktivitas)
        buttonInsertProfile = findViewById(R.id.buttonInsertProfile)

        buttonInsertProfile.setOnClickListener {
            // Validasi input
            if (!validateInputs()) return@setOnClickListener

            // Ambil data dari input
            val bb = editTextBB.text.toString().toFloat()
            val tb = editTextTB.text.toString().toFloat()
            val usia = editTextUsia.text.toString().toInt()

            val jk = when (radioGroupJk.checkedRadioButtonId) {
                R.id.radioMale -> "laki"
                R.id.radioFemale -> "perempuan"
                else -> ""
            }

            val aktivitas = when (radioGroupAktivitas.checkedRadioButtonId) {
                R.id.radioLow -> "rendah"
                R.id.radioMedium -> "sedang"
                R.id.radioHigh -> "tinggi"
                else -> ""
            }

            // Kirimkan data ke API menggunakan Retrofit
            val profile = InsertProfile(userId, tb, bb, jk, usia, aktivitas)
            RetrofitClient.instance.insertProfile(profile).enqueue(object : Callback<InsertProfile> {
                override fun onResponse(call: Call<InsertProfile>, response: Response<InsertProfile>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ActivityInsertProfile, "Profile berhasil disimpan", Toast.LENGTH_SHORT).show()
                        // Intent kembali ke DiaryActivity
                        val intent = Intent(this@ActivityInsertProfile, DiaryActivity::class.java)
                        intent.putExtra("USER_ID", userId) // Jika diperlukan, kirim kembali user_id
                        startActivity(intent)
                        finish() // Tutup ActivityInsertProfile
                    } else {
                        Toast.makeText(this@ActivityInsertProfile, "Gagal menyimpan profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<InsertProfile>, t: Throwable) {
                    Toast.makeText(this@ActivityInsertProfile, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun validateInputs(): Boolean {
        if (editTextBB.text.isNullOrEmpty()) {
            Toast.makeText(this, "Berat badan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (editTextTB.text.isNullOrEmpty()) {
            Toast.makeText(this, "Tinggi badan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (editTextUsia.text.isNullOrEmpty()) {
            Toast.makeText(this, "Usia tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (radioGroupJk.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return false
        }

        if (radioGroupAktivitas.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih tingkat aktivitas", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
