package com.example.quickpark.ui.notification

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.RetrofitClient
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val recycler = findViewById<RecyclerView>(R.id.notificationRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

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
