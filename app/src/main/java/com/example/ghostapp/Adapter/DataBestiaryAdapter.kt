package com.example.ghostapp.Adapter

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.JsonToken
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ghostapp.BestiaryDetailsActivity
import com.example.ghostapp.MapActivity
import com.example.ghostapp.R
import com.example.ghostapp.services.HttpEnv
import com.example.ghostapp.services.PhotoServices
import org.json.JSONArray
import org.json.JSONObject
import kotlin.compareTo
import kotlin.concurrent.thread
import kotlin.math.log
import kotlin.toString

class DataBestiaryAdapter(val data: JSONArray, userToken: String): RecyclerView.Adapter<DataBestiaryViewHolder>() {
    private val photoServices = PhotoServices()
    private val userToken = JSONObject(userToken.toString()).getString("session")
    private val httpEnv = HttpEnv()
    private val baseUrl = httpEnv.baseUrl

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBestiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_bestiary_view_holder, parent, false)
        return DataBestiaryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.length()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DataBestiaryViewHolder, position: Int) {
        val obj = data.getJSONObject(position)
        holder.nameTextView.text = obj.getString("name")
        holder.typeTextView.text = "Categorie : " + obj.getString("type")
        var monsterLevel = obj.getInt("level")
        var dangerText: String
        when {
            monsterLevel <= 3 -> {
                holder.levelCardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.green)
                )
                dangerText = "Reduit"
            }
            monsterLevel <= 6 -> {
                holder.levelCardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.orange)
                )
                dangerText = "Moyen"
            }
            else -> {
                holder.levelCardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.red)
                )
                dangerText = "Elevé"
            }
        }
        thread {
            photoServices.getPhoto(
                category = "bestiary",
                id = obj.getString("_id"),
                token = userToken,
                onSuccess = { responseBody: String? ->
                    if (responseBody != null && responseBody.isNotEmpty()) {
                        val jsonResponse = JSONObject(responseBody)
                        val photos = jsonResponse.getJSONArray("photos")
                        if (photos.length() > 0) {
                            val photoUrl = baseUrl + photos.getString(0)
                            holder.itemView.post {
                                Glide.with(holder.itemView.context)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.baseline_downloading_24)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .fitCenter()
                                    .into(holder.getImageView())
                            }
                        }else{
                            holder.itemView.post {
                                holder.getImageView().visibility = View.GONE
                            }
                        }
                    }
                },
                onError = { errorMessage ->
                    Log.e("photoAdapter", "Error: $errorMessage")
                    holder.itemView.post {
                        holder.getImageView().visibility = View.GONE
                    }
                }
            )
        }

        holder.levelTextView.text = dangerText
        holder.moreInfoButton.setOnClickListener {
            val context = holder.itemView.context
            val monsterId = obj.getString("_id")
            val intent = Intent(context, BestiaryDetailsActivity::class.java)
            intent.putExtra("monster_id", monsterId)
            context.startActivity(intent)
        }
    }


    fun getPhotoUrl(monsterId: String): String {
        var photoUrl = ""
        thread {
            photoUrl = photoServices.getPhoto(
                "bestiary",
                monsterId,
                userToken,
                onSuccess = { responseBody: String? ->
                    if (responseBody != null && responseBody.isNotEmpty()) {
                        val jsonResponse = JSONObject(responseBody)
                        val photos = jsonResponse.getJSONArray("photos")
                        if (photos.length() > 0) {
                            photoUrl = photos[0].toString()
                        }
                    }
                },
                onError = { errorMessage: String ->
                    Log.e("photoAdapter", "Error: $errorMessage")
                }
            ).toString()
        }
        return photoUrl
    }

}

class DataBestiaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val nameTextView: TextView = itemView.findViewById(R.id.vh_name)
    val typeTextView: TextView = itemView.findViewById(R.id.vh_type)
    val levelTextView: TextView = itemView.findViewById(R.id.vh_level)
    private val photoImageView: ImageView = itemView.findViewById(com.example.ghostapp.R.id.vh_photo)
    fun getImageView(): ImageView = photoImageView
    val levelCardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.vh_level_card_view)
    val moreInfoButton: Button = itemView.findViewById(R.id.vh_more_button)
}