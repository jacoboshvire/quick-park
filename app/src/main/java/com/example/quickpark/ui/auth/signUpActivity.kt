package com.example.quickpark.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.data.network.SignUpRequest
import kotlinx.coroutines.launch
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {
    private lateinit var fullnameEt: TextInputEditText
    private lateinit var emailEt: TextInputEditText
    private lateinit var usernameEt: TextInputEditText
    private lateinit var passwordEt: TextInputEditText
    private lateinit var confirmPasswordEt: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        findViewById<TextView>(R.id.createAccountText).setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }


        val fullnameEt = findViewById<TextInputEditText>(R.id.fullnameInput)
        val usernameEt = findViewById<TextInputEditText>(R.id.usernameInput)
        val emailEt = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordEt = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmEt = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val signUpBtn = findViewById<Button>(R.id.signupButton)

        signUpBtn.setOnClickListener {

            val fullname = fullnameEt.text?.toString()?.trim() ?: ""
            val email = emailEt.text?.toString()?.trim() ?: ""
            val username = usernameEt.text?.toString()?.trim() ?: ""
            val password = passwordEt.text?.toString()?.trim() ?: ""
            val confirmPassword = confirmEt.text?.toString()?.trim() ?: ""


            // 🔒 Validation
            if (
                fullname.isEmpty() ||
                username.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()
            ) {
                toast("All fields are required")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.create(this@SignUpActivity)

                    val response = api.signUp(
                        SignUpRequest(
                            fullname = fullname,
                            username = username,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword
                        )
                    )

                    if (response.isSuccessful) {
                        toast("Account created successfully 🎉")
                        finish() // back to login
                    } else {
                        toast(response.errorBody()?.string() ?: "Signup failed")
                    }

                } catch (e: Exception) {
                    toast(e.message ?: "Network error")
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

