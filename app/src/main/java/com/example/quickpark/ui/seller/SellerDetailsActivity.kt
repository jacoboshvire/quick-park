package com.example.quickpark.ui.seller

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.quickpark.R
import com.example.quickpark.data.local.TokenManager
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.data.network.SellerItem
import com.example.quickpark.ui.notification.NotificationActivity
import com.example.quickpark.ui.profile.ProfileActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch

class SellerDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var seller: SellerItem? = null
    private lateinit var sellerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_details)

        findViewById<LinearLayout>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.sellerBtn).setOnClickListener {
            startActivity(Intent(this, SellerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.notificationBtn).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        sellerId = intent.getStringExtra("SELLER_ID")
            ?: run {
                toast("Invalid parking space")
                finish()
                return
            }

        findViewById<Button>(R.id.buyBtn).setOnClickListener {
            bookParking()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapPreview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchSeller()
    }

    // ================= FETCH SELLER =================
    private fun fetchSeller() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@SellerDetailsActivity)
                val response = api.getSellerById(sellerId)

                if (response.isSuccessful && response.body() != null) {
                    seller = response.body()
                    bindSeller(response.body()!!)
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            }
        }
    }

    // ================= BIND DATA =================
    private fun bindSeller(seller: SellerItem) {
        findViewById<TextView>(R.id.locationText).text = seller.locations
        findViewById<TextView>(R.id.priceText).text = "£${seller.price}"
        findViewById<TextView>(R.id.sellerName).text = seller.user.fullname

        Glide.with(this)
            .load(seller.image)
            .into(findViewById(R.id.parkingImage))

        // 🔐 PREVENT SELF BOOKING
        val myUserId = TokenManager(this).getUserId()
        val buyBtn = findViewById<Button>(R.id.buyBtn)

        if (seller.user.id == myUserId) {
            buyBtn.isEnabled = false
            buyBtn.text = "Your listing"
            buyBtn.alpha = 0.5f
        }

        googleMap?.let {
            showOnMap(seller)
        }
    }

    // ================= BOOKING =================
    private fun bookParking() {
        lifecycleScope.launch {
            try {
                val token = TokenManager(this@SellerDetailsActivity).getToken()
                if (token.isNullOrEmpty()) {
                    toast("Please login first")
                    return@launch
                }

                val api = RetrofitClient.create(this@SellerDetailsActivity)
                val response = api.bookParking(
                    token = "Bearer $token",
                    sellerId = sellerId
                )

                when (response.code()) {
                    201 -> toast("Booking request sent 🚗")
                    400 -> toast("You cannot book your own parking")
                    401 -> toast("Session expired. Login again")
                    409 -> toast("Parking already booked")
                    else -> toast("Booking failed (${response.code()})")
                }

            } catch (e: Exception) {
                toast("Network error")
            }
        }
    }

    // ================= MAP =================
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        seller?.let { showOnMap(it) }
    }

    private fun showOnMap(seller: SellerItem) {
        val map = googleMap ?: return
        val latLng = LatLng(seller.lat, seller.lng)

        map.clear()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        map.addMarker(
            MarkerOptions().position(latLng).title(seller.locations)
        )
    }

    private fun showError() {
        toast("Failed to load parking space")
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
