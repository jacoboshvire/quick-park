package com.example.quickpark.ui.notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import com.example.quickpark.ui.dashboard.DashboardActivity
import com.example.quickpark.ui.profile.ProfileActivity
import com.example.quickpark.ui.seller.SellerActivity
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val recycler = findViewById<RecyclerView>(R.id.notificationRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<LinearLayout>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.sellerBtn).setOnClickListener {
            startActivity(Intent(this, SellerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.mainBackBtn).setOnClickListener{
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        adapter = NotificationAdapter { notification ->
            markAsRead(notification.id)
        }

        recycler.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@NotificationActivity)
                val response = api.getNotifications()

                if (response.isSuccessful && response.body() != null) {
                    adapter.submitList(response.body()!!)
                }

            } catch (e: Exception) {
                Log.e("NOTIFICATION", "Failed to load", e)
            }
        }
    }

    private fun markAsRead(id: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@NotificationActivity)
                api.markNotificationRead(id)
                loadNotifications() // refresh

            } catch (e: Exception) {
                Log.e("NOTIFICATION", "Mark read failed", e)
            }
        }
    }
}
