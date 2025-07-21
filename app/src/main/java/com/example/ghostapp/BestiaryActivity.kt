package com.example.ghostapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.adapter.DataBestiaryAdapter
import com.example.ghostapp.fragments.HeaderDefaultFragment
import com.example.ghostapp.services.BestiaryServices
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class BestiaryActivity : AppCompatActivity() {
    lateinit var bestiaryActivitySearchEditText: EditText
    lateinit var bestiaryActivityListMonsters: RecyclerView

    lateinit var monsterData: String
    lateinit var filteredMonsterData : String

    val bestiaryServices = BestiaryServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bestiary)

        val headerDefaultFragment = supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderDefaultFragment
        headerDefaultFragment?.setHeaderTitle(getString(R.string.bestiary_activity_title))

        val userToken = getSharedPreferences("save", MODE_PRIVATE).getString("token", null)
        thread {
            bestiaryServices.getMonsters(
                JSONObject(userToken.toString()).getString("session"),
                onSuccess = { responseBody: String? ->
                    runOnUiThread {
                        monsterData = responseBody.toString()
                        setUpListView(JSONArray(monsterData), userToken.toString())
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

        setUpListView(JSONArray(), userToken.toString())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bestiaryActivitySearchEditText = findViewById(R.id.bestiary_activity_search_edit_text)
        bestiaryActivitySearchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString()
                filterMonsters(searchText, userToken.toString())
            }
        })
    }


    fun filterMonsters(query: String, userToken: String) {
        filteredMonsterData = if (query.isEmpty()) {
            monsterData
        } else {
            val jsonArray = JSONArray(monsterData)
            val filteredArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val monster = jsonArray.getJSONObject(i)
                if (monster.getString("name").contains(query, ignoreCase = true)) {
                    filteredArray.put(monster)
                }
            }
            filteredArray.toString()
        }
        setUpListView(JSONArray(filteredMonsterData), userToken)
    }

    private fun setUpListView(data : JSONArray, userToken: String) {
        this.bestiaryActivityListMonsters = findViewById(R.id.bestiary_activity_monster_list)
        this.bestiaryActivityListMonsters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        this.bestiaryActivityListMonsters.adapter = DataBestiaryAdapter(data, userToken)
    }
}