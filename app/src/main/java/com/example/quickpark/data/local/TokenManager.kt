package com.example.quickpark.data.local

import android.content.Context
import android.util.Base64
import org.json.JSONObject

class TokenManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "JWT_TOKEN"
    }

    // ---------------- SAVE TOKEN ----------------
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    // ---------------- GET TOKEN ----------------
    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    // ---------------- CLEAR ----------------
    fun clear() {
        prefs.edit().clear().apply()
    }

    // ---------------- GET USER ID FROM JWT ----------------
    fun getUserId(): String? {
        val token = getToken() ?: return null

        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE),
                Charsets.UTF_8
            )

            val json = JSONObject(payload)
            json.getString("id") // 🔑 MUST MATCH BACKEND JWT
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- GET USER ROLE (OPTIONAL) ----------------
    fun getUserRole(): String? {
        val token = getToken() ?: return null

        return try {
            val parts = token.split(".")
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE),
                Charsets.UTF_8
            )
            JSONObject(payload).optString("role", null)
        } catch (e: Exception) {
            null
        }
    }
}
