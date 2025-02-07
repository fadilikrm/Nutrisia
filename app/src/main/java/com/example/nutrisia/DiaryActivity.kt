package com.example.nutrisia

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrisia.databinding.ActivityDailyStreakBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyStreakBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyStreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menampilkan tanggal sekarang
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = currentDate

        // Ambil ID pengguna dari SharedPreferences
        val userId = getLoggedInUserId()
        if (userId == null) {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Navigasi tombol lainnya
        binding.IconAbout.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        fetchUserData(userId)

        binding.IconProfile.setOnClickListener {
            val intent = Intent(this, ActivityUserInformation::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.Olahraga.setOnClickListener {
            val intent = Intent(this, ActivityOlahraga::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.HistoryMakanan.setOnClickListener {
            val intent = Intent(this, FoodHistoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnIsiProfile.setOnClickListener {
            val intent = Intent(this, ActivityInsertProfile::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // Navigasi ke OCRActivity saat ll_food_target diklik
        val llFoodTarget = findViewById<LinearLayout>(R.id.ll_food_target)
        llFoodTarget.setOnClickListener {
            val intent = Intent(this, OcrActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }

    // Pastikan data selalu diperbarui saat activity kembali muncul
    override fun onResume() {
        super.onResume()
        val userId = getLoggedInUserId()
        if (userId != null) {
            fetchUserData(userId)
        }
    }

    private fun fetchUserData(userId: Int) {
        val params = mapOf("id" to userId.toString())

        RetrofitClient.instance.getProgram(params).enqueue(object : Callback<ViewProgramResponse> {
            override fun onResponse(call: Call<ViewProgramResponse>, response: Response<ViewProgramResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val programData = response.body()?.data
                    if (programData != null) {
                        populateDiaryFields(programData)
                    } else {
                        Toast.makeText(this@DiaryActivity, "Program tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@DiaryActivity, "Gagal memuat program", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ViewProgramResponse>, t: Throwable) {
                Toast.makeText(this@DiaryActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateDiaryFields(data: ViewProgram) {
        val totalCalories = data.jml_kal
        val remainingCalories = data.remaining_calories

        binding.tvNameUser2.text = "${data.fullname}👋"
        binding.tvProgram2.text = data.program
        binding.textView3.text = data.status_bmi
        binding.textViewbmivalue.text = data.bmi.toString()
        binding.tvCalorieTitle3.text = data.jml_kal.toString()
        binding.tvcalorieremainingvalue.text = data.remaining_calories.toString()

        if (remainingCalories < totalCalories * 0.2) {
            // Ubah warna menjadi merah
            binding.tvcalorieremainingvalue.setTextColor(Color.RED)
            showWarningDialog()
        } else {
            // Ubah warna menjadi default (misalnya hitam)
            binding.tvcalorieremainingvalue.setTextColor(Color.BLACK)
        }
    }

    private fun showWarningDialog() {
        // Inflate layout dialog_warning.xml
        val dialogView = layoutInflater.inflate(R.layout.dialog_warning, null)

        // Buat instance AlertDialog dengan layout custom
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Dialog tidak bisa ditutup dengan tombol kembali
            .create()

        // Temukan button "OK" dari layout dialog
        val btnOkay = dialogView.findViewById<Button>(R.id.btn_okay)

        // Atur properti dan aksi tombol "OK"
        btnOkay.setBackgroundColor(resources.getColor(R.color.greenPrimer, null)) // Pastikan warnanya sesuai
        btnOkay.setTextColor(resources.getColor(R.color.white, null))
        btnOkay.setOnClickListener {
            dialog.dismiss() // Tutup dialog saat tombol diklik
        }

        // Tampilkan dialog
        dialog.show()
    }


    private fun getLoggedInUserId(): Int? {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) {
            // Log error jika userId tidak ditemukan
            Toast.makeText(this, "User ID tidak ditemukan di SharedPreferences", Toast.LENGTH_SHORT).show()
        }
        return userId.takeIf { it != -1 }
    }
}