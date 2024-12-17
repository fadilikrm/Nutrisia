package com.example.nutrisia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nutrisia.databinding.ActivityProfileBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil ID user dari SharedPreferences
        userId = getLoggedInUserId()
        if (userId == null) {
            Toast.makeText(this, "ID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Tangkap data dari intent
        val foto = intent.getStringExtra("foto")
        val fullname = intent.getStringExtra("fullname")
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val tb = intent.getStringExtra("tb")
        val bb = intent.getStringExtra("bb")
        val jk = intent.getStringExtra("jk")
        val age = intent.getStringExtra("age")
        val aktivitas = intent.getStringExtra("aktivitas")

        // Isi data di form
        binding.editTextFullName.setText(fullname)
        binding.editUsername.setText(username)
        binding.editTextEmail.setText(email)
        binding.editTb.setText(tb)
        binding.editBb.setText(bb)
        binding.editAge.setText(age)

        // Isi RadioButton Jenis Kelamin
        when (jk) {
            "laki" -> binding.radioGroupJk.check(binding.radioMale.id)
            "perempuan" -> binding.radioGroupJk.check(binding.radioFemale.id)
        }

        // Isi RadioButton Aktivitas
        when (aktivitas) {
            "rendah" -> binding.radioGroupAktivitas.check(binding.radioLow.id)
            "sedang" -> binding.radioGroupAktivitas.check(binding.radioMedium.id)
            "tinggi" -> binding.radioGroupAktivitas.check(binding.radioHigh.id)
        }

        // Gunakan Glide untuk memuat gambar
        Glide.with(this)
            .load(foto)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.imageViewProfile)

        // Tambahkan logika untuk memilih foto dari galeri
        binding.buttonUploadPhoto.setOnClickListener {
            selectImageFromGallery()
        }

        // Tombol untuk simpan perubahan profil
        binding.buttonEditProfile.setOnClickListener {
            saveProfileChanges()
        }
    }

    /**
     * Ambil ID user dari SharedPreferences.
     */
    private fun getLoggedInUserId(): Int? {
        val sharedPreferences = getSharedPreferences("NutrisiaPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

    /**
     * Memilih gambar dari galeri.
     */
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.imageViewProfile)
        }
    }

    /**
     * Validasi dan simpan perubahan profil.
     */
    private fun saveProfileChanges() {
        val fullname = binding.editTextFullName.text.toString()
        val username = binding.editUsername.text.toString()
        val email = binding.editTextEmail.text.toString()
        val tb = binding.editTb.text.toString()
        val bb = binding.editBb.text.toString()
        val age = binding.editAge.text.toString()
        val jk = if (binding.radioMale.isChecked) "laki" else "perempuan"
        val aktivitas = when {
            binding.radioLow.isChecked -> "rendah"
            binding.radioMedium.isChecked -> "sedang"
            binding.radioHigh.isChecked -> "tinggi"
            else -> ""
        }
        val newPassword = binding.editTextPassword.text.toString().takeIf { it.isNotBlank() }

        val params = hashMapOf<String, RequestBody>(
            "id" to createRequestBody(userId.toString()),
            "fullname" to createRequestBody(fullname),
            "username" to createRequestBody(username),
            "email" to createRequestBody(email),
            "tb" to createRequestBody(tb),
            "bb" to createRequestBody(bb),
            "age" to createRequestBody(age),
            "jk" to createRequestBody(jk),
            "aktivitas" to createRequestBody(aktivitas)
        )

        if (!newPassword.isNullOrEmpty()) {
            params["password"] = createRequestBody(newPassword)
        }

        val fotoPart = imageUri?.let {
            try {
                val file = getFileFromUri(it)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("foto", file.name, requestFile)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membaca foto: ${e.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }

        RetrofitClient.instance.updateProfile(params, fotoPart).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileActivity, ActivityUserInformation::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Mengonversi URI menjadi file yang valid.
     */
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Gagal membuka URI")
        val file = File(cacheDir, "upload_image.jpg")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }


    private fun createRequestBody(value: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }
}
