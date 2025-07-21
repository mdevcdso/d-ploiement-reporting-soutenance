package com.example.ghostapp

import android.adservices.ondevicepersonalization.EventInput
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.MainActivity
import com.example.ghostapp.services.AuthServices
import org.json.JSONObject
import org.w3c.dom.Text
import kotlin.collections.get
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.text.get
import kotlin.toString

class SubscribeActivity : AppCompatActivity() {
    val authServices = AuthServices()

    private lateinit var subscribeActivityBackArrow: ImageView
    private lateinit var subscribeActivityProgressBar: ProgressBar
    private lateinit var subscribeActivityProcessText: TextView

    private lateinit var subscribeActivityTitle: TextView
    private lateinit var subscribeActivityText: TextView

    private lateinit var subscribeActivityInputInfo: TextView
    private lateinit var subscribeActivityInput: EditText
    private lateinit var subscribeActivityNextStepButton: Button

    private var step = 1
    private var maxStep = 5

    private var subscribeInfo = arrayOf("", "", "", "", "")
    private val textStep = arrayOf(
        arrayOf("Étape 1", "Saisir votre prénom", "", "Prénom"),
        arrayOf("Étape 2", "Saisir votre nom", "", "Nom"),
        arrayOf("Étape 3", "Saisir votre numero de telephone", "", "telephone"),
        arrayOf("Étape 4", "Saisir votre email", "Votre mail doit être au format : xxx@xxx.xx", "Email"),
        arrayOf("Étape 5", "Création de mot de passe", "Votre mot de passe doit contenir au minimum 12 caractères, 1 minuscule, 1 majuscule, 1 chiffre et 1 caractère spécial", "Mot de passe"),
        )

    private fun isValidPassword(password: String): Boolean {
        val minLength = 12
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= minLength &&
                hasLowerCase &&
                hasUpperCase &&
                hasDigit &&
                hasSpecialChar
    }

    @SuppressLint("SetTextI18n")
    private fun updateStep(){
        subscribeActivityProgressBar.progress = step
        subscribeActivityProcessText.text = "$step/$maxStep"


        subscribeActivityTitle.text = textStep[step-1][1]
        subscribeActivityText.text = textStep[step-1][2]
        subscribeActivityInputInfo.text = textStep[step-1][0]
        if(!subscribeInfo[step-1].isEmpty()) {
            subscribeActivityInput.setText(subscribeInfo[step-1])
        }else{
            subscribeActivityInput.setText("")
        }

        subscribeActivityInput.hint = textStep[step-1][3]
        if(step == maxStep) {
            subscribeActivityNextStepButton.text = "Valider"
        }
        when(step) {
            3 -> subscribeActivityInput.inputType = android.text.InputType.TYPE_CLASS_PHONE
            4 -> subscribeActivityInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            5 -> subscribeActivityInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            else -> subscribeActivityInput.inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_subscribe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        subscribeActivityProgressBar = findViewById(R.id.subscribe_activity_progress_bar)
        subscribeActivityProcessText = findViewById(R.id.subscribe_activity_progress_text)

        subscribeActivityTitle = findViewById(R.id.subscribe_activity_title)
        subscribeActivityText = findViewById(R.id.subscribe_activity_text)
        subscribeActivityInputInfo = findViewById(R.id.subscribe_activity_info_input)
        subscribeActivityInput = findViewById(R.id.subscribe_activity_input)

        subscribeActivityBackArrow = findViewById(R.id.subscribe_activity_back_arrow)
        subscribeActivityNextStepButton = findViewById(R.id.subscribe_activity_next_step_button)

        subscribeActivityBackArrow.setOnClickListener {
            if(step > 1) {
                step--
                updateStep()
            } else {
                val intent = Intent(this, WelcomeScreenActivity::class.java)
                startActivity(intent)
            }

        }

        subscribeActivityNextStepButton.setOnClickListener {
            if(step == 5) {
                val password = subscribeActivityInput.text.toString()
                if(!isValidPassword(password)) {
                    subscribeActivityInput.error = "Le mot de passe ne respecte pas les critères"
                    return@setOnClickListener
                }
            }
            if(step == 4) {
                val email = subscribeActivityInput.text.toString()
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    subscribeActivityInput.error = "L'email n'est pas valide"
                    return@setOnClickListener
                }
            }
            if(step == 3) {
                val phoneNumber = subscribeActivityInput.text.toString()
                if(!phoneNumber.matches(Regex("^\\+?[0-9]{10,15}\$"))) {
                    subscribeActivityInput.error = "Le numéro de téléphone n'est pas valide"
                    return@setOnClickListener
                }
            }
            if (step < maxStep){
                if(!subscribeActivityInput.text.isEmpty()) {
                    subscribeInfo[step-1] = subscribeActivityInput.text.toString()
                    step++
                    updateStep()
                }else{
                    subscribeActivityInput.error = "Ce champ ne peut pas être vide"
                }
            } else {
                if (!subscribeActivityInput.text.isEmpty()) {
                    subscribeInfo[step - 1] = subscribeActivityInput.text.toString()

                    var confirmSubscribePopup = AlertDialog.Builder(this)
                    confirmSubscribePopup.setTitle("Confirmer votre inscription")
                    confirmSubscribePopup.setMessage(
                        "firstName: ${subscribeInfo[0]}\n" +
                                "Premon : ${subscribeInfo[1]}\n" +
                                "Nom : ${subscribeInfo[2]}\n" +
                                "Email : ${subscribeInfo[3]}\n"
                    )
                    confirmSubscribePopup.setPositiveButton("Confirmer") { dialog, _ ->
                        dialog.dismiss()
                        thread {
                            val subscribeBody = JSONObject().apply {
                                put("firstName", subscribeInfo[0])
                                put("lastName", subscribeInfo[1])
                                put("phoneNumber", subscribeInfo[2])
                                put("email", subscribeInfo[3])
                                put("password", subscribeInfo[4])
                            }

                            authServices.subscribe(
                                subscribeBody,
                                onSuccess = { responseBody: String? ->
                                    runOnUiThread {
                                        AlertDialog.Builder(this)
                                            .setTitle("Inscription réussie")
                                            .setMessage("Vous avez creer un compte a avec succès.\n Vous pouvez maintenant vous connecter.")
                                            .setPositiveButton("OK") { dialog2, _ ->
                                                dialog2.dismiss()
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .show()
                                    }
                                },
                                onError = { errorMessage: String ->
                                    runOnUiThread {
                                        AlertDialog.Builder(this)
                                            .setTitle("Probleme lors de votre inscription")
                                            .setMessage(errorMessage)
                                            .setPositiveButton("OK") { dialog2, _ ->
                                                dialog2.dismiss()
                                            }
                                            .show()
                                    }
                                    step = 1
                                    updateStep()
                                }
                            )
                        }
                    }
                    confirmSubscribePopup.setNegativeButton("Annuler") { dialog, _ ->
                        dialog.dismiss()
                    }
                    confirmSubscribePopup.setCancelable(false)
                    confirmSubscribePopup.show()
                }
            }
        }
    }
}