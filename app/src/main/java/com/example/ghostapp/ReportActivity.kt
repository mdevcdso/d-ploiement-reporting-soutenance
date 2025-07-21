package com.example.ghostapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.Adapter.DataBestiaryAdapter
import com.example.ghostapp.Adapter.DataReportAdapter
import com.example.ghostapp.fragments.HeaderDefaultFragment
import com.example.ghostapp.services.BestiaryServices
import com.example.ghostapp.services.ReportServices
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.get
import kotlin.concurrent.thread

class ReportActivity : AppCompatActivity() {


    lateinit var reportActivityAddReportButton: Button
    lateinit var reportActivityReportList: RecyclerView
    lateinit var reportData: String

    val reportServices = ReportServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDefaultFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.report_activity_title))

        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        thread {
            reportServices.getMyReports(
                JSONObject(userToken.toString()).getString("session"),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        reportData = responseBody.toString()
                        setUpListView(JSONArray(reportData))
                    }
                },
                onError = { errorMessage: String ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Erreur lors du chargement des données")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog2, _ ->
                                val intent = Intent(this, MapActivity::class.java)
                                startActivity(intent)
                            }
                            .show()
                    }
                }
            )
        }
        setUpListView(JSONArray())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        reportActivityAddReportButton = findViewById(R.id.report_activity_add_report_btn)
        reportActivityAddReportButton.setOnClickListener {
            val intent = Intent(this, AddReportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpListView(data : JSONArray) {
        this.reportActivityReportList = findViewById(R.id.report_activity_monster_list)
        this.reportActivityReportList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        this.reportActivityReportList.adapter = DataReportAdapter(reverseJsonArray(data))
    }

    private fun reverseJsonArray(jsonArray: JSONArray): JSONArray {
        val reversedArray = JSONArray()
        for (i in jsonArray.length() - 1 downTo 0) {
            reversedArray.put(jsonArray.get(i))
        }
        return reversedArray
    }
}