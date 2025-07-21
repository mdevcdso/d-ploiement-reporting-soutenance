package com.example.ghostapp.services

import android.content.Context.MODE_PRIVATE
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReportServices {
    private val httpEnv = HttpEnv()
    private val client = httpEnv.client
    private val baseUrl = httpEnv.baseUrl

    fun createReport(
        reportBody: JSONObject,
        token: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = reportBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$baseUrl/reports/")
            .post(requestBody)
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

    fun getMyReports(
        token: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/reports/user")
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

    fun getReportById(
        token: String,
        id: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/reports/user/$id")
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

    fun getReportAddress(
        token: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/reports/addresses")
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

    fun updateReport(
        reportBody: JSONObject,
        token: String,
        id: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = reportBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$baseUrl/reports/user/$id")
            .patch(requestBody)
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

    fun deleteReport(
        token: String,
        id: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/reports/user/$id")
            .delete()
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