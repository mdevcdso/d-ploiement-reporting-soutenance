package com.example.ghostapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.adapter.PhotoAdapter
import com.example.ghostapp.fragments.HeaderDetailFragment
import com.example.ghostapp.services.PhotoServices
import com.example.ghostapp.services.ReportServices
import org.json.JSONObject
import kotlin.concurrent.thread

class ReportDetailsActivity : AppCompatActivity() {

    lateinit var reportDetailsActivityAddress: TextView
    lateinit var reportDetailsActivityCreationDate: TextView
    lateinit var reportDetailsActivityDescription: TextView
    lateinit var reportDetailsActivityPhotoRecyclerView: RecyclerView
    lateinit var reportDetailsActivityStatus: TextView
    lateinit var reportDetailsActivityUpdateButton: Button
    lateinit var reportDetailsActivityDeleteButton: Button

    private val reportServices = ReportServices()
    private val photoServices = PhotoServices()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_details)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDetailFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.report_detail_activity_detail_activity_title))
        headerDefaultFragment?.setLastActivity(ReportActivity::class.java)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        reportDetailsActivityAddress = findViewById(R.id.report_detail_activity_address_tv)
        reportDetailsActivityCreationDate = findViewById(R.id.report_detail_activity_date_tv)
        reportDetailsActivityDescription = findViewById(R.id.report_detail_activity_description_tv)
        reportDetailsActivityStatus = findViewById(R.id.report_detail_activity_status_tv)

        reportDetailsActivityUpdateButton = findViewById(R.id.report_detail_activity_update_btn)
        reportDetailsActivityDeleteButton = findViewById(R.id.report_detail_activity_supp_btn)

        val reportId = intent.getStringExtra("report_id")
        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        thread {
            reportServices.getReportById(
                JSONObject(userToken.toString()).getString("session"),
                reportId.toString(),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        reportDetailsActivityAddress.text = JSONObject(responseBody.toString()).getString("address")
                        val jsonResponse = JSONObject(responseBody.toString())
                        val dateString = jsonResponse.getString("createdAt")
                        reportDetailsActivityCreationDate.text = "Créé le ${formatISODate(dateString)}"
                        reportDetailsActivityDescription.text = JSONObject(responseBody.toString()).getString("description")
                        val status = JSONObject(responseBody.toString()).getString("status")
                        reportDetailsActivityStatus.text = "Statut : $status"
                        val statusCardView = reportDetailsActivityStatus.parent as com.google.android.material.card.MaterialCardView
                        when (status) {
                            "En attente" -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.button_grey))
                            "Assigné" -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.light_blue))
                            "En cours" -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.light_blue))
                            "Terminé" -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.green))
                            "Annulé" -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.red))
                            else -> statusCardView.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.button_grey))
                        }

                        photoAdapter(
                            reportId.toString(),
                            JSONObject(userToken.toString()).getString("session")
                        )
                    }

                    reportDetailsActivityUpdateButton.setOnClickListener {
                        if(JSONObject(responseBody.toString()).getString("status") == "En attente") {
                            var intent = Intent(this, UpdateReportActivity::class.java)
                            intent.putExtra("report_id", reportId)
                            intent.putExtra("date", JSONObject(responseBody.toString()).getString("createdAt"))
                            intent.putExtra("type", JSONObject(responseBody.toString()).getString("type"))
                            intent.putExtra("address", JSONObject(responseBody.toString()).getString("address"))
                            intent.putExtra("description", JSONObject(responseBody.toString()).getString("description"))
                            startActivity(intent)
                            finish()
                        } else {
                            AlertDialog.Builder(this)
                                .setTitle("Suppression impossible")
                                .setMessage("Vous ne pouvez pas supprimer un signalement qui a ete assigné ou qui est en cours de traitement.")
                                .setPositiveButton("OK") { dialog2, _ ->
                                    dialog2.dismiss()
                                }
                                .show()
                        }
                    }

                    reportDetailsActivityDeleteButton.setOnClickListener {
                        if(JSONObject(responseBody.toString()).getString("status") == "En attente") {
                            suppReport(userToken.toString(), reportId.toString())
                        } else {
                            AlertDialog.Builder(this)
                                .setTitle("Suppression impossible")
                                .setMessage("Vous ne pouvez pas supprimer un signalement qui a ete assigné ou qui est en cours de traitement.")
                                .setPositiveButton("OK") { dialog2, _ ->
                                    dialog2.dismiss()
                                }
                                .show()
                        }
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

    private fun formatISODate(isoDate: String): String {
        try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm", java.util.Locale.FRANCE)

            val date = inputFormat.parse(isoDate)
            return date?.let { outputFormat.format(it) } ?: "Date inconnue"
        } catch (e: Exception) {
            Log.e("Error", "Error parsing date: $isoDate", e)
            return isoDate
        }
    }

    private fun photoAdapter(
        id: String,
        userToken: String
    ) {
        thread {
            photoServices.getPhoto(
                "reports",
                id,
                userToken,
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        this.reportDetailsActivityPhotoRecyclerView = findViewById(R.id.report_detail_activity_photo_list)
                        this.reportDetailsActivityPhotoRecyclerView.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                        val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
                        snapHelper.attachToRecyclerView(this.reportDetailsActivityPhotoRecyclerView)

                        val animation = androidx.recyclerview.widget.DefaultItemAnimator()
                        animation.addDuration = 300
                        animation.removeDuration = 300
                        this.reportDetailsActivityPhotoRecyclerView.itemAnimator = animation


                        this.reportDetailsActivityPhotoRecyclerView.adapter = PhotoAdapter(
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

    private fun suppReport(userToken: String, reportId: String){
        AlertDialog.Builder(this)
            .setTitle("Supprimer le signalement")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce signalement ?")
            .setPositiveButton("Oui") { dialog, _ ->
                thread {
                    reportServices.deleteReport(
                        JSONObject(userToken).getString("session"),
                        reportId,
                        onSuccess = { responseBody: String? ->
                            runOnUiThread {
                                AlertDialog.Builder(this)
                                    .setTitle("Signalement supprimé")
                                    .setMessage("Le signalement a été supprimé avec succès.")
                                    .setPositiveButton("OK") { dialog2, _ ->
                                        var intent = Intent(this, ReportActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .show()
                            }
                        },
                        onError = { errorMessage: String ->
                            runOnUiThread {
                                AlertDialog.Builder(this)
                                    .setTitle("Erreur lors de la suppression")
                                    .setMessage(errorMessage)
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    )
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }
}