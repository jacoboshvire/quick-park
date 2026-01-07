package com.example.quickpark.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quickpark.R
import android.widget.TextView
import android.content.Intent

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        findViewById<TextView>(R.id.createAccountText).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}
