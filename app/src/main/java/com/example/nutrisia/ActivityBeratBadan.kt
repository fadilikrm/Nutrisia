package com.example.nutrisia

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrisia.databinding.ActivityBeratBadanBinding

class ActivityBeratBadan : AppCompatActivity() {

    private lateinit var binding: ActivityBeratBadanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeratBadanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set listener untuk FAB
        binding.fabAdd.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        // Inflate layout dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_berat_badan, null)

        // Buat AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                // Implementasi tombol Simpan
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}
