package com.example.ghostapp.adapter

import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.R
import com.example.ghostapp.services.HttpEnv
import org.json.JSONObject
import com.bumptech.glide.Glide

class PhotoAdapter(data : JSONObject): RecyclerView.Adapter<PhotoViewHolder>() {
    private val photos = data.getJSONArray("photos")
    private val httpEnv = HttpEnv()
    private val baseUrl = httpEnv.baseUrl

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PhotoViewHolder {
        var view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.photo_view_holder, parent, false)

        return PhotoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photos.length()
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        try {
            val photoPath = photos.getString(position)
            val fullImageUrl = baseUrl + photoPath

            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.baseline_downloading_24)
                .error(R.drawable.ic_launcher_foreground)
                .fitCenter()
                .into(holder.getImageView())

        } catch (e: Exception) {
            Log.e("PhotoAdapter", "Error loading photo at position $position", e)
            holder.getImageView().setImageResource(R.drawable.ic_launcher_background)
        }
    }
}

class PhotoViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val photoImageView: ImageView = itemView.findViewById(R.id.photo_image_view)
    fun getImageView(): ImageView = photoImageView
}