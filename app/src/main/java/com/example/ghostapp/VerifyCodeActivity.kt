package com.example.ghostapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.services.AuthServices
import kotlin.concurrent.thread

class VerifyCodeActivity : AppCompatActivity() {
    private lateinit var verifyCodeActivityCodeEditText: EditText
    private lateinit var verifyCodeActivityValidBtn: Button

    private val authServices = AuthServices()
    private lateinit var email: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_code)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        email = intent.getStringExtra("email") ?: ""
        verifyCodeActivityCodeEditText = findViewById(R.id.verify_code_activity_code_input)
        verifyCodeActivityValidBtn = findViewById(R.id.verify_code_activity_button)

        verifyCodeActivityValidBtn.setOnClickListener {
            val code = verifyCodeActivityCodeEditText.text.toString()

            thread {
                authServices.verifyCode(
                    code,
                    onSuccess = { responseBody: String? ->
                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setTitle("Code Vérifié")
                                .setMessage("Le code a été vérifié avec succès.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    val intent = Intent(this, NewMdpActivity::class.java)
                                    intent.putExtra("email", this.email)
                                    intent.putExtra("code", code)
                                    startActivity(intent)
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }
                    },
                    onError = { errorMessage: String ->
                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setTitle("Erreur de Vérification")
                                .setMessage(errorMessage)
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }
                    }
                )
            }

        }
    }
}