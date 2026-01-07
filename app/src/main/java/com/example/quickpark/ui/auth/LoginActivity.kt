package com.example.quickpark.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quickpark.R
import com.example.quickpark.data.local.TokenManager
import com.example.quickpark.data.network.LoginRequest
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEt = findViewById<EditText>(R.id.emailEditText)
        val passwordEt = findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val createAccount = findViewById<TextView>(R.id.createAccountText)

        createAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        loginBtn.setOnClickListener {

            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.create(this@LoginActivity)
                    val response = api.login(LoginRequest(email, password))

                    Log.d("LOGIN_DEBUG", "HTTP ${response.code()}")

                    if (response.isSuccessful && response.body() != null) {

                        val body = response.body()!!
                        Log.d("LOGIN_DEBUG", "User: ${body.user.email}")

                        // Save JWT
                        TokenManager(this@LoginActivity).saveToken(body.token)

                        // Navigate
                        startActivity(
                            Intent(this@LoginActivity, DashboardActivity::class.java)
                        )
                        finish()

                    } else {
                        // Invalid credentials / backend error
                        val errorMsg = response.errorBody()?.string() ?: "Login failed"

                        Log.e("LOGIN_ERROR", errorMsg)

                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    // Network / SSL / parsing error
                    Log.e("LOGIN_EXCEPTION", e.stackTraceToString())

                    Toast.makeText(
                        this@LoginActivity,
                        "Network error. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
