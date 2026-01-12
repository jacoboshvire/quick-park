package com.example.quickpark.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.data.network.SellerItem
import com.example.quickpark.ui.notification.NotificationActivity
import com.example.quickpark.ui.profile.ProfileActivity
import com.example.quickpark.ui.seller.SellerActivity
import com.example.quickpark.ui.seller.SellerDetailsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: SellerAdapter

    private var sellersCache: List<SellerItem> = emptyList()
    private var isMapReady = false

    // Permission launcher
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableUserLocation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Navigation
        findViewById<LinearLayout>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.sellerBtn).setOnClickListener {
            startActivity(Intent(this, SellerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.notificationBtn).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // Search
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) filterSellers(query)
            true
        }

        // Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Recycler
        val recycler = findViewById<RecyclerView>(R.id.parkingRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = SellerAdapter { seller ->
            openSellerDetails(seller.id)
        }
        recycler.adapter = adapter

        fetchSellers()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        requestLocationPermission()

        if (sellersCache.isNotEmpty()) {
            showMarkers(sellersCache)
        }
    }

    // -------- LOCATION --------
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation() {
        if (!::googleMap.isInitialized) return

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener

            val userLatLng = LatLng(location.latitude, location.longitude)
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
            )
        }
    }

    // -------- API --------
    private fun fetchSellers() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@DashboardActivity)
                val response = api.getSellers()

                if (response.isSuccessful && response.body() != null) {
                    sellersCache = response.body()!!.sellers
                    adapter.submitList(sellersCache)
                    if (isMapReady) showMarkers(sellersCache)
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Failed to load sellers", e)
            }
        }
    }

    // -------- MARKERS --------
    private fun showMarkers(items: List<SellerItem>) {
        googleMap.clear()

        items.forEach { seller ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(seller.lat, seller.lng))
                    .title(seller.locations)
                    .snippet("£${seller.price}")
            )
            marker?.tag = seller.id
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            val sellerId = marker.tag as? String ?: return@setOnInfoWindowClickListener
            openSellerDetails(sellerId)
        }
    }

    private fun filterSellers(query: String) {
        val filtered = sellersCache.filter {
            it.locations.contains(query, true)
        }
        adapter.submitList(filtered)
        if (isMapReady) showMarkers(filtered)
    }

    private fun openSellerDetails(id: String) {
        val intent = Intent(this, SellerDetailsActivity::class.java)
        intent.putExtra("SELLER_ID", id)
        startActivity(intent)
    }
}
