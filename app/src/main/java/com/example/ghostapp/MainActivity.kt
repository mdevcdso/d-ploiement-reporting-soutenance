package com.example.ghostapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.services.AuthServices
import kotlin.concurrent.thread
import okhttp3.FormBody
import org.json.JSONObject
import kotlin.apply
import kotlin.collections.remove

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityForgottenMdp: TextView
    private lateinit var mainActivityEmailEditText: EditText
    private lateinit var mainActivityPasswordEditText: EditText
    private lateinit var mainActivityMdpVisibility: ImageView
    private lateinit var mainActivityLoginButton: Button
    private lateinit var mainActivitySubscribeButton: TextView
    private val authServices = AuthServices()

    private var isMdpVisible: Boolean = false

    fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("save", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var userToken = getToken()
        if(userToken != null) {
            Log.i("session", JSONObject(userToken).getString("session"))
            thread {
                authServices.userInfo(
                    JSONObject(userToken).getString("session"),
                    onSuccess = { responseBody: String? ->
                        runOnUiThread {
                            val intent = Intent(this, MapActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    },
                    onError = {
                        val sharedPreferences = getSharedPreferences("save", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.remove("token")
                        editor.apply()
                    }
                )
            }
        }

        mainActivityEmailEditText = findViewById(R.id.main_activity_email_input)
        mainActivityPasswordEditText = findViewById(R.id.main_activity_mdp_input)
        mainActivityLoginButton = findViewById(R.id.main_activity_connexion_button)
        mainActivityMdpVisibility = findViewById(R.id.main_activity_mdp_visibility_button)
        mainActivityForgottenMdp = findViewById(R.id.main_activity_forgot_password_button)


        mainActivityMdpVisibility.setOnClickListener {
            if(!isMdpVisible) {
                mainActivityPasswordEditText.transformationMethod = null
                mainActivityMdpVisibility.setImageResource(R.drawable.baseline_fish_eye)
            } else {
                mainActivityPasswordEditText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                mainActivityMdpVisibility.setImageResource(R.drawable.line_icon)
            }
            isMdpVisible = !isMdpVisible
        }

        mainActivityForgottenMdp.setOnClickListener {
            val intent = Intent(this, ForgottenPasswordActivity::class.java)
            startActivity(intent)
        }


        mainActivityLoginButton.setOnClickListener {
            val email = mainActivityEmailEditText.text.toString()
            val password = mainActivityPasswordEditText.text.toString()

            thread {
                val loginBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                authServices.login(
                    loginBody,
                    onSuccess = { responseBody: String? ->
                        runOnUiThread {
                            Log.i("Response", responseBody.toString())
                            var saveToken = getSharedPreferences("save", MODE_PRIVATE)
                            var edit = saveToken.edit()
                            edit.putString("token", responseBody)
                            edit.apply()

                            val intent = Intent(this, MapActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    },
                    onError = { errorMessage: String ->
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }

        mainActivitySubscribeButton = findViewById(R.id.main_activity_inscription_button)
        mainActivitySubscribeButton.setOnClickListener {
            val intent = Intent(this, WelcomeScreenActivity::class.java)
            startActivity(intent)
        }


    }
}