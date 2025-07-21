package com.example.ghostapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.Adapter.PhotoAdapter
import com.example.ghostapp.Adapter.SuppPhotoAdapter
import com.example.ghostapp.fragments.HeaderDetailFragment
import com.example.ghostapp.services.MapServices
import com.example.ghostapp.services.PhotoServices
import com.example.ghostapp.services.ReportServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.internal.OpDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.toString

class UpdateReportActivity : AppCompatActivity() {

    private lateinit var updateReportActivityDate: TextView
    private lateinit var updateReportActivitySpinner: Spinner
    private lateinit var updateReportActivityAdresse : EditText
    private lateinit var updateReportActivityVille : EditText
    private lateinit var updateReportActivityCodePostal : EditText
    private lateinit var updateReportActivityUseLocation: CheckedTextView
    private lateinit var updateReportActivityDescription : EditText
    private lateinit var updateReportActivityPhotosRecyclerView: RecyclerView
    private lateinit var updateReportActivitySubmitButton: Button

    private lateinit var reportId: String
    private lateinit var reportDate: String
    private lateinit var reportType: String
    private lateinit var reportFullAddress: String
    private lateinit var reportAddress: String
    private lateinit var reportVille: String
    private lateinit var reportCodePostal: String
    private lateinit var reportDescription: String

    private val photoServices = PhotoServices()
    private val reportServices = ReportServices()
    private val mapServices = MapServices()
    private lateinit var userLocation: LatLng

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reportId = intent.getStringExtra("report_id") ?: ""
        reportDate = intent.getStringExtra("date") ?: ""
        reportFullAddress = intent.getStringExtra("address") ?: ""
        val addressParts = reportFullAddress.split(",").map { it.trim() }
        reportAddress = addressParts.getOrElse(0) { "" }
        reportVille = addressParts.getOrElse(1) { "" }
        reportCodePostal = addressParts.getOrElse(2) { "" }
        reportDescription = intent.getStringExtra("description") ?: ""
        reportType = intent.getStringExtra("type") ?: ""

        enableEdgeToEdge()
        setContentView(R.layout.activity_update_report)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDetailFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.update_report_activity_title))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateReportActivityDate = findViewById(R.id.update_report_activity_date_text_view)
        updateReportActivitySpinner = findViewById(R.id.update_report_activity_report_type_spinner)
        updateReportActivityAdresse = findViewById(R.id.update_report_activity_address_edit_text)
        updateReportActivityVille = findViewById(R.id.update_report_activity_ville_edit_text)
        updateReportActivityCodePostal = findViewById(R.id.update_report_activity_code_postal_edit_text)
        updateReportActivityUseLocation = findViewById(R.id.update_report_activity_use_location_check_box)
        updateReportActivityDescription = findViewById(R.id.update_report_activity_description_edit_text)
        updateReportActivitySubmitButton = findViewById(R.id.update_report_activity_submit_button)

        val adapter = ArrayAdapter.createFromResource(
            this, R.array.report_types, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        updateReportActivitySpinner.adapter = adapter

        updateReportActivityDate.text = "Signalement du : ${formatISODate(reportDate)}"
        updateReportActivityAdresse.hint = reportAddress
        updateReportActivityVille.hint = reportVille
        updateReportActivityCodePostal.hint = reportCodePostal
        updateReportActivityDescription.hint = reportDescription

        updateReportActivityUseLocation.setOnClickListener {
            updateReportActivityUseLocation.toggle()
            if( updateReportActivityUseLocation.isChecked ) {
                mapServices.getUserLocationWithoutMap(this, this) { latLng ->
                    if (latLng != null) {
                        userLocation = latLng
                        getAddressFromLocation(userLocation)
                        Toast.makeText(this, "Position actuelle utilisée", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Impossible d'obtenir votre position actuelle", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                updateReportActivityAdresse.text.clear()
                updateReportActivityVille.text.clear()
                updateReportActivityCodePostal.text.clear()
            }
        }

        val userToken =
            getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        photoAdapter(
            reportId.toString(),
            JSONObject(userToken.toString()).getString("session")
        )

        updateReportActivitySubmitButton.setOnClickListener {
            val reportType = updateReportActivitySpinner.selectedItem.toString().ifBlank { reportType }
            val address = updateReportActivityAdresse.text.toString().ifBlank { reportAddress }
            val city = updateReportActivityVille.text.toString().ifBlank { reportVille }
            val postalCode = updateReportActivityCodePostal.text.toString().ifBlank { reportCodePostal }
            val location = "$address, $city, $postalCode"
            val description = updateReportActivityDescription.text.toString().ifBlank { reportDescription }


            var confirmReportPopup = AlertDialog.Builder(this)
            confirmReportPopup.setTitle("Confirmation de signalement")
            confirmReportPopup.setMessage("Type: $reportType\nAdresse: $address\nVille: $city\nCode Postal: $postalCode\nDescription: $description")
            confirmReportPopup.setPositiveButton("Confirmer") { dialog, _ ->
                dialog.dismiss()
                thread {
                    val reportInfo = JSONObject().apply {
                        put("type", reportType)
                        put("address", location)
                        put("description", description)
                        put("photos", JSONArray())
                    }
                    reportServices.updateReport(
                        reportInfo,
                        JSONObject(userToken.toString()).getString("session"),
                        reportId,
                        onSuccess = { responseBody: String? ->
                            runOnUiThread {
                                AlertDialog.Builder(this)
                                    .setTitle("Signalement modifié")
                                    .setMessage("Votre signalement a été modifié avec succès.")
                                    .setPositiveButton("OK") { dialog2, _ ->
                                        dialog2.dismiss()
                                        val intent = Intent(this, ReportDetailsActivity::class.java)
                                        intent.putExtra("report_id", reportId)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .show()
                            }
                        },
                        onError = { errorMessage: String ->
                            runOnUiThread {
                                AlertDialog.Builder(this)
                                    .setTitle("Probleme lors de la modification du signalement")
                                    .setMessage(errorMessage)
                                    .setPositiveButton("OK") { dialog2, _ ->
                                        dialog2.dismiss()
                                    }
                                    .show()
                            }
                        }
                    )
                }
            }
            confirmReportPopup.setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            confirmReportPopup.setCancelable(false)
            confirmReportPopup.show()


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
            return isoDate
        }
    }

    fun getAddressFromLocation(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                updateReportActivityAdresse.setText(
                    address.thoroughfare ?: ("" + " " + (address.subThoroughfare ?: ""))
                )
                updateReportActivityVille.setText(address.locality ?: "")
                updateReportActivityCodePostal.setText(address.postalCode ?: "")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la récupération de l'adresse", Toast.LENGTH_SHORT).show()
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
                        this.updateReportActivityPhotosRecyclerView = findViewById(R.id.update_report_activity_photo_list)
                        this.updateReportActivityPhotosRecyclerView.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                        val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
                        snapHelper.attachToRecyclerView(this.updateReportActivityPhotosRecyclerView)

                        val animation = androidx.recyclerview.widget.DefaultItemAnimator()
                        animation.addDuration = 300
                        animation.removeDuration = 300
                        this.updateReportActivityPhotosRecyclerView.itemAnimator = animation


                        this.updateReportActivityPhotosRecyclerView.adapter = SuppPhotoAdapter(
                            JSONObject(responseBody.toString()), userToken)
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

