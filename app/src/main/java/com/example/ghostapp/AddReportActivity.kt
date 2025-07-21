package com.example.ghostapp

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
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ghostapp.fragments.HeaderDetailFragment
import com.example.ghostapp.services.MapServices
import com.example.ghostapp.services.PhotoServices
import com.example.ghostapp.services.ReportServices
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import kotlin.concurrent.thread

@Suppress("DEPRECATION")
class AddReportActivity : AppCompatActivity() {
    private lateinit var addReportActivitySpinner: Spinner
    private lateinit var addReportActivityAdresse : EditText
    private lateinit var addReportActivityVille : EditText
    private lateinit var addReportActivityCodePostal : EditText
    private lateinit var addReportActivityUseLocation: CheckedTextView
    private lateinit var addReportActivityDescription : EditText
    private lateinit var addReportActivityUploadPhotoButton: Button
    private lateinit var addReportActivitySubmitButton: Button

    private val pickImagesRequest = 1
    private lateinit var selectedPhotos: MutableList<Uri>

    private val photoServices = PhotoServices()
    private val reportServices = ReportServices()
    private val mapServices = MapServices()
    private lateinit var userLocation: LatLng

    private var loadingDialog: AlertDialog? = null
    private var downloadPhotoDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_report)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDetailFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.new_report))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addReportActivitySpinner = findViewById(R.id.add_report_activity_report_type_spinner)
        addReportActivityAdresse = findViewById(R.id.add_report_activity_address_edit_text)
        addReportActivityVille = findViewById(R.id.add_report_activity_ville_edit_text)
        addReportActivityCodePostal = findViewById(R.id.add_report_activity_code_postal_edit_text)
        addReportActivityUseLocation = findViewById(R.id.add_report_activity_use_location_check_box)
        addReportActivityDescription = findViewById(R.id.add_report_activity_description_edit_text)
        addReportActivityUploadPhotoButton = findViewById(R.id.add_report_activity_photo_button)
        addReportActivitySubmitButton = findViewById(R.id.add_report_activity_submit_button)


        val adapter = ArrayAdapter.createFromResource(
            this, R.array.report_types, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        addReportActivitySpinner.adapter = adapter

        addReportActivityUseLocation.setOnClickListener {
            addReportActivityUseLocation.toggle()
            if( addReportActivityUseLocation.isChecked ) {
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
                addReportActivityAdresse.text.clear()
                addReportActivityVille.text.clear()
                addReportActivityCodePostal.text.clear()
            }
        }

        selectedPhotos = mutableListOf()
        addReportActivityUploadPhotoButton.setOnClickListener {
            openImagePicker()
        }


        addReportActivitySubmitButton.setOnClickListener {
            val reportType = addReportActivitySpinner.selectedItem.toString()
            val address = addReportActivityAdresse.text.toString()
            val city = addReportActivityVille.text.toString()
            val postalCode = addReportActivityCodePostal.text.toString()
            val location = "$address, $city, $postalCode"
            val description = addReportActivityDescription.text.toString()


            var confirmReportPopup = AlertDialog.Builder(this)
                .setTitle("Confirmation de signalement")
                .setMessage("Type: $reportType\nAdresse: $address\nVille: $city\nCode Postal: $postalCode\nDescription: $description\nPhotos: ${selectedPhotos.size}")
                .setPositiveButton("Confirmer") { dialog, _ ->
                    dialog.dismiss()
                    thread {
                        val reportInfo = JSONObject().apply {
                            put("type", reportType)
                            put("address", location)
                            put("description", description)
                            put("photos", JSONArray())
                        }
                        val userToken =
                            getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
                        reportServices.createReport(
                            reportInfo,
                            JSONObject(userToken.toString()).getString("session"),
                            onSuccess = { responseBody: String? ->
                                runOnUiThread {
                                    val jsonResponse = JSONObject(responseBody ?: "")
                                    val reportId = jsonResponse.getString("_id")

                                    if (selectedPhotos.isEmpty()){
                                        AlertDialog.Builder(this)
                                            .setTitle("Signalement envoyé")
                                            .setMessage("Votre signalement a été envoyé avec succès.")
                                            .setCancelable(false)
                                            .setPositiveButton("OK") { dialog2, _ ->
                                                loadingDialog?.dismiss()
                                                dialog2.dismiss()
                                                val intent = Intent(this, ReportActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .show()
                                    }else {
                                        uploadPhotosForReport(reportId, userToken.toString())
                                        downloadPhotoDialog = AlertDialog.Builder(this)
                                            .setTitle("Signalement envoyé")
                                            .setMessage("Photo en cours de telechargement...")
                                            .setCancelable(false)
                                            .show()
                                    }
                                }
                            },
                            onError = { errorMessage: String ->
                                runOnUiThread {
                                    AlertDialog.Builder(this)
                                        .setTitle("Probleme lors de l'envoye du signalement")
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
                .setNegativeButton("Annuler") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
            confirmReportPopup.show()
        }
    }

    fun getAddressFromLocation(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                addReportActivityAdresse.setText(
                    address.thoroughfare ?: ("" + " " + (address.subThoroughfare ?: ""))
                )
                addReportActivityVille.setText(address.locality ?: "")
                addReportActivityCodePostal.setText(address.postalCode ?: "")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la récupération de l'adresse $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Sélectionner des photos"), pickImagesRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImagesRequest && resultCode == RESULT_OK) {
            loadingDialog = AlertDialog.Builder(this)
                .setTitle("Chargement des photos")
                .setMessage("Veuillez patienter...")
                .setCancelable(false)
                .show()
            selectedPhotos.clear()
            var rejectedCount = 0

            thread {
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        val compressedFile = compressImage(imageUri)

                        if (compressedFile != null) {
                            selectedPhotos.add(Uri.fromFile(compressedFile))
                        } else {
                            rejectedCount++
                        }
                    }
                } else if (data?.data != null) {
                    val imageUri = data.data!!
                    val compressedFile = compressImage(imageUri)

                    if (compressedFile != null) {
                        selectedPhotos.add(Uri.fromFile(compressedFile))
                    } else {
                        rejectedCount++
                    }
                }

                runOnUiThread {
                    loadingDialog?.dismiss()
                    if (rejectedCount > 0) {
                        Toast.makeText(
                            this,
                            "$rejectedCount photo(s) n'ont pas pu être compressées",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this,
                        "${selectedPhotos.size} photo(s) sélectionnée(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun compressImage(uri: Uri): File? {
        return try {

            val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")

            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            var quality = 100
            var fileSize: Long

            do {
                compressedFile.outputStream().use { outputStream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
                }
                fileSize = compressedFile.length()
                quality -= 10
            } while (fileSize > 1024 * 1024 && quality > 10)

            if (fileSize <= 1024 * 1024) {
                compressedFile
            } else {
                compressedFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("Compression", "Erreur lors de la compression", e)
            null
        }
    }

    private fun uploadPhotosForReport(reportId: String, userToken: String) {
        thread {
            val photoFiles = selectedPhotos.mapNotNull { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    file
                } catch (e: Exception) {
                    Log.e("PhotoUpload", "Erreur conversion photo", e)
                    null
                }
            }
            photoServices.uploadPhoto(
                category = "reports",
                id = reportId,
                token = JSONObject(userToken).getString("session"),
                photos = photoFiles,
                onSuccess = { response ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Signalement envoyé")
                            .setMessage("Votre signalement a été envoyé avec succès.")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog2, _ ->
                                loadingDialog?.dismiss()
                                downloadPhotoDialog?.dismiss()
                                dialog2.dismiss()
                                val intent = Intent(this, ReportActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .show()
                        Toast.makeText(this, "Photos uploadées avec succès", Toast.LENGTH_SHORT).show()
                    }
                    photoFiles.forEach { it.delete() }
                },
                onError = { error ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Erreur lors de l'envoi des photos")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog2, _ ->
                                dialog2.dismiss()
                                val intent = Intent(this, ReportActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .show()
                        Toast.makeText(this, "Erreur upload photos: $error", Toast.LENGTH_SHORT).show()
                    }
                    photoFiles.forEach { it.delete() }
                }
            )
        }
    }
}