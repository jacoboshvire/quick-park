package com.example.quickpark.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.data.network.SellerItem
import com.example.quickpark.ui.auth.LoginActivity
import com.example.quickpark.ui.profile.ProfileActivity
import com.example.quickpark.ui.seller.SellerActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.util.Locale
class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var adapter: SellerAdapter

    private var sellersCache: List<SellerItem> = emptyList()
    private var isMapReady = false

    // ---------- LOCATION PERMISSION ----------
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                enableUserLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<LinearLayout>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.sellerBtn).setOnClickListener {
            startActivity(Intent(this, SellerActivity::class.java))
        }

        // 🔍 SEARCH BAR
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                moveMapToSearch(query)
                filterSellers(query)
            }
            true
        }

        // 🗺 MAP
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 📋 RECYCLER
        val recycler = findViewById<RecyclerView>(R.id.parkingRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = SellerAdapter()
        recycler.adapter = adapter

        // ⬆️ BOTTOM SHEET
        val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        fetchSellers()
    }

    // ---------- MAP READY ----------
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        requestLocationPermission()

        if (sellersCache.isNotEmpty()) {
            showMarkers(sellersCache)
        }
    }

    // ---------- LOCATION ----------
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            if (location == null) return@addOnSuccessListener

            val userLat = location.latitude
            val userLng = location.longitude

            val sortedByDistance = sellersCache
                .map { seller ->
                    val distance = distanceInKm(
                        userLat,
                        userLng,
                        seller.lat,
                        seller.lng
                    )
                    seller to distance
                }
                .sortedBy { it.second }
                .map { it.first }

            adapter.submitList(sortedByDistance)

            if (isMapReady) {
                showMarkers(sortedByDistance)
            }

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLat, userLng),
                    15f
                )
            )
        }
    }

    // ---------- DISTANCE ----------
    private fun distanceInKm(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, result)
        return result[0] / 1000
    }

    // ---------- FILTER ----------
    private fun filterSellers(query: String) {
        val filtered = sellersCache.filter {
            it.locations.contains(query, true) ||
                    it.user.username.contains(query, true) ||
                    it.user.email.contains(query, true)
        }

        adapter.submitList(filtered)

        if (isMapReady) {
            showMarkers(filtered)
        }
    }

    // ---------- API ----------
    private fun fetchSellers() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@DashboardActivity)
                val response = api.getSellers()

                if (response.isSuccessful && response.body() != null) {
                    sellersCache = response.body()!!.sellers
                    adapter.submitList(sellersCache)

                    if (isMapReady) {
                        showMarkers(sellersCache)
                    }
                } else {
                    Log.e("DASHBOARD_API", "HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD_API", "Error loading sellers", e)
            }
        }
    }

    // ---------- SEARCH GEOCODE ----------
    private fun moveMapToSearch(query: String) {
        lifecycleScope.launch {
            try {
                val geocoder = Geocoder(this@DashboardActivity, Locale.getDefault())
                val results = geocoder.getFromLocationName(query, 1)

                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                    )
                }
            } catch (e: Exception) {
                Log.e("GEOCODE", "Failed to geocode", e)
            }
        }
    }

    // ---------- MARKERS ----------
    private fun showMarkers(items: List<SellerItem>) {
        if (!::googleMap.isInitialized) return

        googleMap.clear()

        items.forEach { seller ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(seller.lat, seller.lng))
                    .title(seller.locations)
                    .snippet("£${seller.price}")
            )
        }
    }
}
