package com.example.nutrisia

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        // Set up Register button click listener
        btnRegister.setOnClickListener {
            val fullname = etFullName.text.toString()
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (fullname.isBlank() || username.isBlank() || email.isBlank() ||
                password.isBlank() || confirmPassword.isBlank()
            ) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(fullname, username, email, password)
            }
        }

        // Login link click listener
        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun registerUser(fullname: String, username: String, email: String, password: String) {
        val user = UserNutrisia(
            fullname = fullname,
            username = username,
            email = email,
            password = password
        )

        RetrofitClient.instance.addStudent(user).enqueue(object : Callback<UserNutrisia> {
            override fun onResponse(call: Call<UserNutrisia>, response: Response<UserNutrisia>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.status == true) {
                        Toast.makeText(this@RegisterActivity, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Username atau email sudah digunakan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Registrasi Gagal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserNutrisia>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Gagal terhubung: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
