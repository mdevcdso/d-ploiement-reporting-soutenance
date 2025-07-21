package com.example.ghostapp.services

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request

class BestiaryServices {
    private val httpEnv = HttpEnv()
    private val client = httpEnv.client
    private val baseUrl = httpEnv.baseUrl

    fun getMonsters(
        token: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("$baseUrl/bestiary")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Report error", responseBody.toString())
                onError("${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Connexion error", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun getMonsterById(
        token: String,
        monsterId: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("$baseUrl/bestiary/$monsterId")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Report error", responseBody.toString())
                onError("${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Connexion error", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }
}