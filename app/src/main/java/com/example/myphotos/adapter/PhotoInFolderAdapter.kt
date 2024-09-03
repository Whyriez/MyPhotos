package com.example.myphotos.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myphotos.R
import com.example.myphotos.model.ImageModel

class PhotoInFolderAdapter(val context: Context): RecyclerView.Adapter<PhotoInFolderAdapter.PhotoInFolderViewHolder>() {
    private val image : MutableList<ImageModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoInFolderViewHolder {
        return PhotoInFolderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoInFolderViewHolder, position: Int) {
        holder.bindmodel(image[position])
    }

    override fun getItemCount(): Int {
        return image.size
    }

    fun setPhoto(data: List<ImageModel>){
        image.clear()
        image.addAll(data)
        notifyDataSetChanged()
    }

    inner class PhotoInFolderViewHolder(view:View):RecyclerView.ViewHolder(view){
        val itemName: TextView = view.findViewById(R.id.imageName)
        val itemPhoto: ImageView = view.findViewById(R.id.imagePhoto)

        fun bindmodel(p:ImageModel){
            itemName.text = p.name
            displayImageFromBase64(p.image, itemPhoto)
        }
    }

    fun displayImageFromBase64(base64String: String, imageView: ImageView) {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        imageView.setImageBitmap(bitmap)
    }
}