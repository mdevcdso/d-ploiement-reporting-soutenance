package com.example.ghostapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.services.AuthServices
import kotlin.concurrent.thread

class NewMdpActivity : AppCompatActivity() {
    private lateinit var newMdpActivityNewPasswordEditText: EditText
    private lateinit var newMdpActivityVerifNewMdpEditText: EditText
    private lateinit var newMdpActivityConfirmBtn: Button
    private lateinit var newMdpActivityMdp1Visibility: ImageView
    private lateinit var newMdpActivityMdp2Visibility: ImageView

    private var isMdp1Visible: Boolean = false
    private var isMdp2Visible: Boolean = false


    private val authServices = AuthServices()
    private lateinit var email: String
    private lateinit var code: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_mdp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        email = intent.getStringExtra("email") ?: ""
        code = intent.getStringExtra("code") ?: ""
        newMdpActivityNewPasswordEditText = findViewById(R.id.new_mdp_input)
        newMdpActivityVerifNewMdpEditText = findViewById(R.id.verif_new_mdp_input)
        newMdpActivityConfirmBtn = findViewById(R.id.new_mdp_activity_confirm_button)
        newMdpActivityMdp1Visibility = findViewById(R.id.new_mdp_visibility_button1)
        newMdpActivityMdp2Visibility = findViewById(R.id.new_mdp_visibility_button2)

        newMdpActivityMdp1Visibility.setOnClickListener {
            if(!isMdp1Visible) {
                newMdpActivityNewPasswordEditText.transformationMethod = null
                newMdpActivityMdp1Visibility.setImageResource(R.drawable.baseline_fish_eye)
            } else {
                newMdpActivityNewPasswordEditText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                newMdpActivityMdp1Visibility.setImageResource(R.drawable.line_icon)
            }
            isMdp1Visible = !isMdp1Visible
        }

        newMdpActivityMdp2Visibility.setOnClickListener {
            if(!isMdp2Visible) {
                newMdpActivityVerifNewMdpEditText.transformationMethod = null
                newMdpActivityMdp2Visibility.setImageResource(R.drawable.baseline_fish_eye)
            } else {
                newMdpActivityVerifNewMdpEditText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                newMdpActivityMdp2Visibility.setImageResource(R.drawable.line_icon)
            }
            isMdp2Visible = !isMdp2Visible
        }

        newMdpActivityConfirmBtn.setOnClickListener {
            val newPassword = newMdpActivityNewPasswordEditText.text.toString()
            val verifNewPassword = newMdpActivityVerifNewMdpEditText.text.toString()
            if(newPassword != verifNewPassword) {
                AlertDialog.Builder(this)
                    .setTitle("Erreur")
                    .setMessage("Les mots de passe ne correspondent pas.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir changer votre mot de passe ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    thread {
                        authServices.resetPassword(
                            this.email,
                            this.code,
                            newPassword,
                            onSuccess = { responseBody: String? ->
                                runOnUiThread {
                                    AlertDialog.Builder(this)
                                        .setTitle("Mot de passe changé")
                                        .setMessage("Votre mot de passe a été changé avec succès.")
                                        .setPositiveButton("OK") { dialog, _ ->
                                            var intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            dialog.dismiss()
                                            finish()
                                        }
                                        .create()
                                        .show()
                                }
                            },
                            onError = { errorMessage: String ->
                                runOnUiThread {
                                    AlertDialog.Builder(this)
                                        .setTitle("Erreur")
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
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }
}