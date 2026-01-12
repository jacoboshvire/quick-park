package com.example.quickpark.ui.seller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quickpark.R
import com.example.quickpark.data.local.TokenManager
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.ui.dashboard.DashboardActivity
import com.example.quickpark.ui.notification.NotificationActivity
import com.example.quickpark.ui.profile.ProfileActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SellerActivity : AppCompatActivity() {

    private var imageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            imageUri = it
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        findViewById<LinearLayout>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.mainBackBtn).setOnClickListener{
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        findViewById<FrameLayout>(R.id.imagePicker).setOnClickListener {
            imagePicker.launch("image/*")
        }

        findViewById<Button>(R.id.submitBtn).setOnClickListener {
            submitSeller()
        }

        findViewById<LinearLayout>(R.id.notificationBtn).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    private fun submitSeller() {
        lifecycleScope.launch {
            try {
                // 🔐 TOKEN
                val token = TokenManager(this@SellerActivity).getToken()
                if (token.isNullOrEmpty()) {
                    toast("Not authenticated")
                    return@launch
                }

                // 🖼 IMAGE
                val uri = imageUri ?: run {
                    toast("Image required")
                    return@launch
                }

                // FIELDS
                val location = getInputText(R.id.locationInput)
                val postcode = getInputText(R.id.postcodeInput)
                val phone = getInputText(R.id.phoneInput)
                val price = getInputText(R.id.priceInput)
                val accName = getInputText(R.id.accountNameInput)
                val accNum = getInputText(R.id.accountNumberInput)
                val sortCode = getInputText(R.id.sortCodeInput)
                val duration = getInputText(R.id.durationInput)


                if (
                    location.isEmpty() || postcode.isEmpty() || phone.isEmpty() ||
                    price.isEmpty() || accName.isEmpty() ||
                    accNum.isEmpty() || sortCode.isEmpty() || duration.isEmpty()
                ) {
                    toast("All fields are required")
                    return@launch
                }

                // 🧾 API
                val api = RetrofitClient.create(this@SellerActivity)

                val file = uriToFile(uri)
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )

                fun text(v: String) =
                    v.toRequestBody("text/plain".toMediaType())

                val response = api.createSellerPost(
                    token = "Bearer $token",
                    image = imagePart,
                    locations = text(location),
                    postalcode = text(postcode),
                    phonenumber = text(phone),
                    price = text(price),
                    accountname = text(accName),
                    accountnumber = text(accNum),
                    sortcode = text(sortCode),
                    timeNeeded = text("now"),
                    duration = text(duration)
                )

                if (response.isSuccessful) {
                    toast("Seller post created")
                    finish()
                } else {
                    toast("Failed (${response.code()})")
                }

            } catch (e: Exception) {
                toast(e.message ?: "Error")
            }
        }
    }

    private fun getInputText(id: Int): String =
        findViewById<EditText>(id).text.toString().trim()

    private fun uriToFile(uri: Uri): File {
        val input = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { input.copyTo(it) }
        return file
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
