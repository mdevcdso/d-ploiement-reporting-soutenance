package com.example.ghostapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.Adapter.PhotoAdapter
import com.example.ghostapp.fragments.HeaderDetailFragment
import com.example.ghostapp.services.BestiaryServices
import com.example.ghostapp.services.PhotoServices
import org.json.JSONArray
import org.json.JSONObject
import kotlin.compareTo
import kotlin.concurrent.thread

class BestiaryDetailsActivity : AppCompatActivity() {
    lateinit var bestiaryDetailsActivityName: TextView
    lateinit var bestiaryDetailsActivityLevel: TextView
    lateinit var bestiaryDetailsActivityLevelCardView: com.google.android.material.card.MaterialCardView
    lateinit var bestiaryDetailsActivityType: TextView
    lateinit var bestiaryDetailsActivityImageRecyclerView: RecyclerView
    lateinit var bestiaryDetailsActivityDescription: TextView
    lateinit var bestiaryDetailsActivityCaracteristics: TextView

    private val bestiaryServices = BestiaryServices()
    private val photoServices = PhotoServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bestiary_details)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDetailFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.bestiary_detail_activity_title))
        headerDefaultFragment?.setLastActivity(BestiaryActivity::class.java)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bestiaryDetailsActivityName = findViewById(R.id.bestiary_name_tv)
        bestiaryDetailsActivityLevel = findViewById(R.id.bestiary_level_tv)
        bestiaryDetailsActivityLevelCardView = findViewById(R.id.bestiary_level_cv)
        bestiaryDetailsActivityType = findViewById(R.id.bestiary_type_tv)
        bestiaryDetailsActivityDescription = findViewById(R.id.bestiary_description_tv)
        bestiaryDetailsActivityCaracteristics = findViewById(R.id.bestiary_caracteristics_tv)

        val monsterId = intent.getStringExtra("monster_id")
        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        thread {
            bestiaryServices.getMonsterById(
                JSONObject(userToken.toString()).getString("session"),
                monsterId.toString(),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        bestiaryDetailsActivityName.text = JSONObject(responseBody.toString()).getString("name")
                        var monsterLevel = JSONObject(responseBody.toString()).getInt("level")
                        val dangerText: String
                        when {
                            monsterLevel <= 3 -> {
                                bestiaryDetailsActivityLevelCardView.setCardBackgroundColor(
                                    ContextCompat.getColor(this@BestiaryDetailsActivity, R.color.green))
                                bestiaryDetailsActivityLevelCardView.invalidate()
                                dangerText = "Danger réduit"
                            }
                            monsterLevel <= 6 -> {
                                bestiaryDetailsActivityLevelCardView.setCardBackgroundColor(
                                    ContextCompat.getColor(this@BestiaryDetailsActivity, R.color.orange))
                                bestiaryDetailsActivityLevelCardView.invalidate()
                                dangerText = "Danger moyen"
                            }
                            else -> {
                                bestiaryDetailsActivityLevelCardView.setCardBackgroundColor(
                                    ContextCompat.getColor(this@BestiaryDetailsActivity, R.color.red))
                                bestiaryDetailsActivityLevelCardView.invalidate()
                                dangerText = "Danger élevé"
                            }
                        }
                        bestiaryDetailsActivityLevel.text = dangerText
                        bestiaryDetailsActivityType.text = JSONObject(responseBody.toString()).getString("type")
                        bestiaryDetailsActivityDescription.text = JSONObject(responseBody.toString()).getString("description")
                        bestiaryDetailsActivityCaracteristics.text = JSONObject(responseBody.toString()).getString("caracteristics")

                        photoAdapter("bestiary", monsterId.toString(), JSONObject(userToken.toString()).getString("session"))

                    }
                },
                onError = { errorMessage: String ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Erreur lors du chargement des données")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog2, _ ->
                                val intent = Intent(this, BestiaryActivity::class.java)
                                startActivity(intent)
                            }
                            .show()
                    }
                }
            )
        }
    }
    private fun photoAdapter(
        category: String,
        id: String,
        userToken: String
    ) {
        thread {
            photoServices.getPhoto(
                category,
                id,
                userToken,
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        this.bestiaryDetailsActivityImageRecyclerView = findViewById(R.id.bestiary_detail_activity_photo_list)
                        this.bestiaryDetailsActivityImageRecyclerView.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                        val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
                        snapHelper.attachToRecyclerView(this.bestiaryDetailsActivityImageRecyclerView)

                        // Animation de transition
                        val animation = androidx.recyclerview.widget.DefaultItemAnimator()
                        animation.addDuration = 300
                        animation.removeDuration = 300
                        this.bestiaryDetailsActivityImageRecyclerView.itemAnimator = animation


                        this.bestiaryDetailsActivityImageRecyclerView.adapter = PhotoAdapter(
                            JSONObject(responseBody.toString()))
                    }
                },
                onError = { errorMessage: String ->
                    runOnUiThread {
                        Log.e("photoAdapter", "Error: $errorMessage")
                    }
                }
            )
        }
    }

}