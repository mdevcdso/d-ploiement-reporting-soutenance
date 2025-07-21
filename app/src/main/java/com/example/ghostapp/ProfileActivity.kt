package com.example.ghostapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.services.AuthServices
import org.json.JSONObject
import kotlin.concurrent.thread
import androidx.core.content.edit
import com.example.ghostapp.fragments.HeaderDetailFragment

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileActivityUserName: TextView
    private lateinit var profileActivityUserFirstName: TextView
    private lateinit var profileActivityUserEmail: TextView
    private lateinit var profileActivityUserPhone: TextView
    private lateinit var profileActivityLogoutBtn: Button

    //private lateinit var userInfo: Map<String, String>
    private var authServices: AuthServices = AuthServices()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        setContentView(R.layout.activity_profile)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDetailFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.profile_activity_title))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileActivityUserName = findViewById(R.id.profile_activity_user_name)
        profileActivityUserFirstName = findViewById(R.id.profile_activity_user_first_name)
        profileActivityUserEmail = findViewById(R.id.profile_activity_user_email)
        profileActivityUserPhone = findViewById(R.id.profile_activity_user_phone)
        profileActivityLogoutBtn = findViewById(R.id.profile_activity_logout_button)
        thread {
            authServices.userInfo (
                JSONObject(userToken.toString()).getString("session"),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        profileActivityUserName.text = JSONObject(responseBody.toString()).getString("lastName")
                        profileActivityUserFirstName.text = JSONObject(responseBody.toString()).getString("firstName")
                        profileActivityUserEmail.text = "Email : ${JSONObject(responseBody.toString()).getString("email")}"
                        profileActivityUserPhone.text = "Telephone : ${JSONObject(responseBody.toString()).getString("phoneNumber")}"
                    }
                },
                onError = { errorMessage: String ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Erreur lors du chargement des données")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }
                            .show()
                    }
                }
            )
        }
        profileActivityLogoutBtn.setOnClickListener {
            getSharedPreferences("save", MODE_PRIVATE).edit { clear() }
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}