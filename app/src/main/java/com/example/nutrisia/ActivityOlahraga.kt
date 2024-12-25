package com.example.nutrisia

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrisia.databinding.ActivityOlahragaBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ActivityOlahraga : AppCompatActivity() {

    private lateinit var binding: ActivityOlahragaBinding
    private lateinit var adapter: ActivityOlahragaAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOlahragaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = ActivityOlahragaAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Format tanggal
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Atur tanggal hari ini di EditText
        val todayDate = dateFormat.format(calendar.time)
        binding.etTanggal.setText(todayDate)

        // Ketika EditText tanggal diklik, tampilkan DatePicker
        binding.etTanggal.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val selectedDate = dateFormat.format(calendar.time)
                    binding.etTanggal.setText(selectedDate)
                    fetchSportData(selectedDate) // Ambil data sesuai tanggal yang dipilih
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Ambil data awal untuk tanggal hari ini
        fetchSportData(todayDate)

        // Ketika tombol tambah ditekan, buka dialog
        binding.fabAdd.setOnClickListener {
            showAddSportDialog()
        }
    }

    private fun fetchSportData(tanggal: String) {
        // Ambil user_id dari SharedPreferences
        val userId = getLoggedInUserId()
        if (userId != null) {
            val params = mapOf(
                "user_id" to userId.toString(),
                "tanggal" to tanggal
            )

            RetrofitClient.instance.getSport(params).enqueue(object : Callback<ViewSportResponse> {
                override fun onResponse(call: Call<ViewSportResponse>, response: Response<ViewSportResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val dataList = response.body()?.data
                        if (dataList != null && dataList.isNotEmpty()) {
                            adapter.updateData(dataList) // Perbarui RecyclerView dengan data baru
                            updateTotalKalori(dataList)  // Update total kalori
                        } else {
                            adapter.updateData(emptyList())
                            Toast.makeText(this@ActivityOlahraga, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("API Error", response.errorBody()?.string() ?: "Unknown error")
                        Toast.makeText(this@ActivityOlahraga, "Data Aktifitas Fisik Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ViewSportResponse>, t: Throwable) {
                    Log.e("API Failure", t.message ?: "Unknown error")
                    Toast.makeText(this@ActivityOlahraga, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalKalori(dataList: List<ViewSport>) {
        var totalKalori = 0f  // Gunakan tipe data Float untuk total kalori
        for (data in dataList) {
            totalKalori += data.kalori_out  // Ambil kalori_out dari data
        }
        // Format total kalori dengan dua angka desimal
        binding.tvTotalKalori.text = "Total Kalori: %.2f".format(totalKalori)
    }


    private fun showAddSportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_olahraga, null)

        val spinnerJenisOlahraga = dialogView.findViewById<Spinner>(R.id.spinnerJenisOlahraga)
        val etDurasi = dialogView.findViewById<EditText>(R.id.etDurasi)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpanOlahraga)

        // Ambil daftar olahraga dari strings.xml
        val listOlahragaText = resources.getStringArray(R.array.jenis_olahraga_text)
        val listOlahragaValue = resources.getStringArray(R.array.jenis_olahraga_value)

        // Atur adapter untuk spinner menggunakan list yang diambil dari strings.xml
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOlahragaText)
        spinnerJenisOlahraga.adapter = adapter

        // Tampilkan dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()

        // Ketika tombol simpan ditekan
        btnSimpan.setOnClickListener {
            val selectedOlahragaText = spinnerJenisOlahraga.selectedItem.toString()

            // Cari index dari item yang dipilih di listOlahragaText
            val selectedIndex = listOlahragaText.indexOf(selectedOlahragaText)
            if (selectedIndex != -1) {
                val selectedOlahragaValue = listOlahragaValue[selectedIndex]

                val durasi = etDurasi.text.toString()

                if (durasi.isEmpty()) {
                    Toast.makeText(this, "Durasi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Ambil user_id dari SharedPreferences
                val userId = getLoggedInUserId()
                if (userId != null) {
                    // Mengirim data dengan status (misalnya true untuk menandakan berhasil)
                    val request = InsertSport(
                        user_id = userId,
                        jenis_olahraga = selectedOlahragaValue,  // Gunakan nilai dari jenis_olahraga_value
                        durasi = durasi.toInt(),
                        status = true // Misalnya diset status true jika data berhasil ditambahkan
                    )

                    // Debug: Periksa nilai yang akan dikirim
                    Log.d("InsertSport", "Request: $request")

                    // Lakukan Retrofit request
                    RetrofitClient.instance.InsertSport(request).enqueue(object : Callback<InsertSport> {
                        override fun onResponse(call: Call<InsertSport>, response: Response<InsertSport>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ActivityOlahraga, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                fetchSportData(binding.etTanggal.text.toString()) // Refresh data
                                dialog.dismiss() // Tutup dialog setelah data berhasil dikirim
                            } else {
                                Log.e("API Error", response.errorBody()?.string() ?: "Unknown error")
                                Toast.makeText(this@ActivityOlahraga, "Gagal menambahkan data", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<InsertSport>, t: Throwable) {
                            Log.e("API Failure", t.message ?: "Unknown error")
                            Toast.makeText(this@ActivityOlahraga, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Olahraga yang dipilih tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLoggedInUserId(): Int? {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

}
