package com.example.ghostapp.adapter

import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.ghostapp.R
import com.example.ghostapp.services.HttpEnv
import org.json.JSONObject
import com.bumptech.glide.Glide
import com.example.ghostapp.services.PhotoServices


class SuppPhotoAdapter(data : JSONObject, private val userToken: String): RecyclerView.Adapter<UpdatePhotoViewHolder>() {
    private val photos = data.getJSONArray("photos")
    private val photoServices = PhotoServices()
    private val httpEnv = HttpEnv()
    private val baseUrl = httpEnv.baseUrl

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): UpdatePhotoViewHolder {
        var view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.update_photo_view_holder, parent, false)
        return UpdatePhotoViewHolder(view)
    }


    override fun getItemCount(): Int {
        return photos.length()
    }

    override fun onBindViewHolder(holder: UpdatePhotoViewHolder, position: Int) {
        try {
            val photoPath = photos.getString(position)
            val fullImageUrl = baseUrl + photoPath
            val parts = photoPath.trim('/').split('/')
            val reportId = parts[2]
            val fileName = parts[3]

            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.getImageView())

            holder.suppPhotoBtn.setOnClickListener {
                Log.v("SuppPhotoAdapter", "Report id : $reportId, File name: $fileName")
                kotlin.concurrent.thread {
                    photoServices.suppPhoto(
                        "reports",
                        reportId,
                        userToken,
                        fileName,
                        onSuccess = { response ->
                            (holder.itemView.context as android.app.Activity).runOnUiThread {
                                photos.remove(position)
                                notifyItemRemoved(position)
                                Log.v("SuppPhotoAdapter", "Photo deleted successfully: $response")
                            }
                        },
                        onError = { error ->
                            (holder.itemView.context as android.app.Activity).runOnUiThread {
                                Log.e("SuppPhotoAdapter", "Error deleting photo: $error")
                            }
                        }
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("SuppPhotoAdapter", "Error loading photo at position $position", e)
            holder.getImageView().setImageResource(R.drawable.ic_launcher_background)
        }
    }
}

class UpdatePhotoViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val photoImageView: ImageView = itemView.findViewById(R.id.photo_image_view)
    val suppPhotoBtn: ImageView = itemView.findViewById(R.id.supp_photo_image_view)

    fun getImageView(): ImageView = photoImageView
}