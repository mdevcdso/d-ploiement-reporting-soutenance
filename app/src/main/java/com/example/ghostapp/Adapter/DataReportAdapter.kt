package com.example.ghostapp.Adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.BestiaryDetailsActivity
import com.example.ghostapp.MapActivity
import com.example.ghostapp.R
import com.example.ghostapp.ReportDetailsActivity
import org.json.JSONArray

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

class DataReportAdapter(val data: JSONArray): RecyclerView.Adapter<DataReportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_report_view_holder, parent, false)
        return DataReportViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.length()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DataReportViewHolder, position: Int) {
        val obj = data.getJSONObject(position)
        val dateString = obj.getString("createdAt")
        val formattedDate = formatISODate(dateString)
        holder.dateTextView.text = "Créé le $formattedDate"
        holder.addressTextView.text = obj.getString("address")
        val status = obj.getString("status")
        holder.statusTextView.text = status
        when (status) {
            "En attente" -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.button_grey))
            "Assigné" -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.light_blue))
            "En cours" -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.orange))
            "Terminé" -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green))
            "Annulé" -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.red))
            else -> holder.statusCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.button_grey))
        }
        holder.moreInfoButton.setOnClickListener {
            val context = holder.itemView.context
            val monsterId = obj.getString("_id")
            val intent = Intent(context, ReportDetailsActivity::class.java)
            intent.putExtra("report_id", monsterId)
            context.startActivity(intent)
        }
    }
}

class DataReportViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val dateTextView: TextView = itemView.findViewById(R.id.vh_date)
    val addressTextView: TextView = itemView.findViewById(R.id.vh_address)
    val statusCardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.vh_status_card_view)
    val statusTextView: TextView = itemView.findViewById(R.id.vh_status)
    val moreInfoButton: Button = itemView.findViewById(R.id.vh_more_button)
}