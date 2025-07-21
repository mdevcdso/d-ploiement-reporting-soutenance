package com.example.ghostapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeScreenActivity : AppCompatActivity() {

    private lateinit var welcomeScreenConnexionTextView: TextView
    private lateinit var welcomeScreenSubscribeButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        welcomeScreenConnexionTextView = findViewById(R.id.welcome_screen_activity_connexion_button)
        welcomeScreenConnexionTextView.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        welcomeScreenSubscribeButton = findViewById(R.id.welcome_screen_activity_subscribe_button)
        welcomeScreenSubscribeButton.setOnClickListener {
            var intent = Intent(this, SubscribeActivity::class.java)
            startActivity(intent)
        }
    }
}