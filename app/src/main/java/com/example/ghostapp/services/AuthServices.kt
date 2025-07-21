package com.example.ghostapp.services

import android.content.Context.MODE_PRIVATE
import android.util.Log
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.code
import kotlin.toString

class AuthServices {
    private val httpEnv = HttpEnv()
    private val client = httpEnv.client
    private val baseUrl = httpEnv.baseUrl

    fun login(
        loginBody: JSONObject,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = loginBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/login")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur d'authentification : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun subscribe(
        subscribeBody: JSONObject,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = subscribeBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/subscribe")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur lors de l'inscription : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun userInfo(
        session: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/auth/me")
            .header("Authorization", "Bearer $session")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur de récupération des informations utilisateur : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun verifyEmail(
        email: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonBody = JSONObject().apply {
            put("email", email)
        }
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl/auth/request-password-reset")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur lors de la vérification de l'email : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun verifyCode(
        code: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonBody = JSONObject().apply {
            put("code", code)
        }
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl/auth/verify-security-code")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur de vérification du code : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("code", code)
            put("newPassword", newPassword)
        }
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl/auth/reset-password")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                onSuccess(responseBody)
            } else {
                Log.e("Erreur", responseBody.toString())
                onError("Erreur de réinitialisation du mot de passe : ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("Erreur", e.toString())
            onError("Erreur de connexion : ${e.message}")
        }
    }

    companion object
}