package com.example.ghostapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.services.AuthServices
import kotlin.concurrent.thread

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var forgottenPasswordActivityEmailEditText: EditText
    private lateinit var forgottenPasswordActivityValidBtn: Button

    private val authServices = AuthServices()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgotten_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        forgottenPasswordActivityEmailEditText = findViewById(R.id.forgotten_psw_activity_email_input)
        forgottenPasswordActivityValidBtn = findViewById(R.id.forgotten_psw_activity_send_email_button)

        forgottenPasswordActivityValidBtn.setOnClickListener {
            val email = forgottenPasswordActivityEmailEditText.text.toString()
            thread {
                authServices.verifyEmail(
                    email,
                    onSuccess = { responseBody: String? ->
                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setTitle("Email Sent")
                                .setMessage("Un email de vérification a été envoyé à $email. Veuillez vérifier votre boîte de réception.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    var intent = Intent(this, VerifyCodeActivity::class.java)
                                    intent.putExtra("email", email)
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
                                .setTitle("Erreur lors de la vérification de l'email")
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