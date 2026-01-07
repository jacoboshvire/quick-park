package com.example.quickpark.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.ui.seller.SellerActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var fullnameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var saveBtn: Button
    private lateinit var profileImage: ImageView

    private var imageUri: Uri? = null
    private var userId: String = ""

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                profileImage.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<LinearLayout>(R.id.sellerBtn).setOnClickListener {
            startActivity(Intent(this, SellerActivity::class.java))
        }

        usernameInput = findViewById(R.id.usernameInput)
        fullnameInput = findViewById(R.id.fullnameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        saveBtn = findViewById(R.id.saveButton)
        profileImage = findViewById(R.id.profileImage)

        loadProfile()

        profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        saveBtn.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@ProfileActivity)
                val response = api.getMe()

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    userId = user.id
                    usernameInput.setText(user.username)
                    fullnameInput.setText(user.fullname)
                    emailInput.setText(user.email)

                    user.avatar?.let {
                        Glide.with(this@ProfileActivity)
                            .load(it)
                            .into(profileImage)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@ProfileActivity)

                val fields = mutableMapOf<String, RequestBody>()

                if (usernameInput.text.isNotEmpty())
                    fields["username"] = usernameInput.text.toString()
                        .toRequestBody("text/plain".toMediaType())

                if (fullnameInput.text.isNotEmpty())
                    fields["fullname"] = fullnameInput.text.toString()
                        .toRequestBody("text/plain".toMediaType())

                if (emailInput.text.isNotEmpty())
                    fields["email"] = emailInput.text.toString()
                        .toRequestBody("text/plain".toMediaType())

                if (passwordInput.text.isNotEmpty()) {
                    if (passwordInput.text.length < 6) {
                        Toast.makeText(this@ProfileActivity, "Password too short", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    fields["password"] = passwordInput.text.toString()
                        .toRequestBody("text/plain".toMediaType())
                }

                val imagePart = imageUri?.let { uri ->
                    val file = createTempFileFromUri(uri)
                    val body = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("avatar", file.name, body)
                }

                val response = api.updateUser(userId, fields, imagePart)

                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Update error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output -> inputStream.copyTo(output) }
        return file
    }
}


