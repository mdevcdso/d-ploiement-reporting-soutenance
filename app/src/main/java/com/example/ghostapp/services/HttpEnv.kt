package com.example.ghostapp.services

import okhttp3.OkHttpClient

class HttpEnv {
    val client = OkHttpClient()
    val baseUrl = "https://ghostapp.unbonwhisky.fr"
}