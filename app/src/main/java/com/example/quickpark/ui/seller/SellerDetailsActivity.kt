package com.example.quickpark.ui.seller

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.data.network.SellerItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import android.widget.Button
import com.example.quickpark.data.local.TokenManager


class SellerDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var seller: SellerItem? = null
    private lateinit var sellerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_details)

        findViewById<Button>(R.id.buyBtn).setOnClickListener {
            bookParking()
        }

        sellerId = intent.getStringExtra("SELLER_ID")
            ?: run {
                Toast.makeText(this, "Invalid seller", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapPreview) as? SupportMapFragment
            ?: return

        mapFragment.getMapAsync(this)

        fetchSeller()
    }

    private fun bookParking() {
        lifecycleScope.launch {
            try {
                val token = TokenManager(this@SellerDetailsActivity).getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@SellerDetailsActivity, "Login required", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val api = RetrofitClient.create(this@SellerDetailsActivity)

                val response = api.bookParking(
                    token = "Bearer $token",
                    sellerId = sellerId
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@SellerDetailsActivity,
                        "Booking request sent 🚗",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@SellerDetailsActivity,
                        response.errorBody()?.string() ?: "Booking failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@SellerDetailsActivity,
                    "Error sending booking",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        seller?.let { showOnMap(it) }
    }

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

    private fun bindSeller(seller: SellerItem) {
        findViewById<TextView>(R.id.locationText)?.text = seller.locations
        findViewById<TextView>(R.id.priceText)?.text = "£${seller.price}"
        findViewById<TextView>(R.id.sellerName)?.text = seller.user.fullname

        Glide.with(this)
            .load(seller.image)
            .into(findViewById(R.id.parkingImage))

        googleMap?.let {
            showOnMap(seller)
        }
    }

    private fun showOnMap(seller: SellerItem) {
        val map = googleMap ?: return

        val latLng = LatLng(seller.lat, seller.lng)
        map.clear()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(seller.locations)
        )
    }

    private fun showError() {
        Toast.makeText(this, "Failed to load parking space", Toast.LENGTH_SHORT).show()
        finish()
    }
}
