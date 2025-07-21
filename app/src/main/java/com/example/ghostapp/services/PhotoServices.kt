package com.example.ghostapp.services

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


class PhotoServices {
    private val httpEnv = HttpEnv()
    private val client = httpEnv.client
    private val baseUrl = httpEnv.baseUrl

    fun getPhoto(
        category: String,
        id: String,
        token: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ){
        val request = okhttp3.Request.Builder()
            .url("$baseUrl/photos/$category/$id")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                Log.v("Photo", responseBody.toString())
                onSuccess(responseBody)
            } else {
                Log.e("Photo error", responseBody.toString())
                onError("${response.code}")
            }
        } catch (e: Exception) {
            Log.e("PhotoServices", "Exception détaillée", e)
            onError("Erreur de connexion: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    fun uploadPhoto(
        category: String,
        id: String,
        token: String,
        photos: List<File>,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ){
        val formDataBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        Log.d("PhotoServices", "Nombre de photos à uploader: ${photos.size}")
        photos.forEach { photo ->
            val requestBody = photo.asRequestBody("image/jpeg".toMediaType())
            formDataBuilder.addFormDataPart(
                "photos",
                photo.name,
                requestBody
            )
        }
        val formData = formDataBuilder.build()

        val request = okhttp3.Request.Builder()
            .url("$baseUrl/photos/$category/$id")
            .post(formData)
            .header("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                Log.v("Photo", "Upload reussi !!!!")
                onSuccess(responseBody)
            } else {
                Log.e("Photo error", response.code.toString())
                onError("${response.code}")
            }
        } catch (e: Exception) {
            Log.e("PhotoServices", "Exception détaillée", e)
            onError("Erreur de connexion: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    fun suppPhoto(
        category: String,
        id: String,
        token: String,
        photoName: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ){
        val request = okhttp3.Request.Builder()
            .url("$baseUrl/photos/$category/$id/$photoName")
            .delete()
            .header("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) {
                Log.v("Photo", "Suppression reussi !!!!")
                onSuccess(responseBody)
            } else {
                Log.e("Photo error", response.code.toString())
                onError("${response.code}")
            }
        } catch (e: Exception) {
            Log.e("PhotoServices", "Exception détaillée", e)
            onError("Erreur de connexion: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

}