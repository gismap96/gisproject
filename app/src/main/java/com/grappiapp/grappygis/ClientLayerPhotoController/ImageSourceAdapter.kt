package com.grappiapp.grappygis.ClientLayerPhotoController

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.row_for_image_source_selection.view.*

class ImageSourceAdapter(var context: Context, val callback: ImageSourceAdapter.OnImageSourceSelectedClickListener): RecyclerView.Adapter<ImageSourceAdapter.ImageSourceSelectorViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSourceSelectorViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_for_image_source_selection, parent, false)
        return ImageSourceSelectorViewHolder(v, callback)
    }

    override fun getItemCount(): Int {
        return ImageSourcesEnum.values().size
    }

    override fun onBindViewHolder(holder: ImageSourceSelectorViewHolder, position: Int) {
        val model = ImageSourcesEnum.values()[position]
        holder.bind(model, context)
    }

    inner class ImageSourceSelectorViewHolder(view: View, val callback: OnImageSourceSelectedClickListener): RecyclerView.ViewHolder(view){
        var imageSourceName = view.imageSourceNameTV
        var imageSourceIcon = view.imageSourceSelectionIconIV

        fun bind(model: ImageSourcesEnum, context: Context){
            imageSourceName.text = context.getString(model.title)
            imageSourceIcon.background = ContextCompat.getDrawable(context, model.getImageAddress())
            itemView.setOnClickListener {
                callback.onImageSourceSelected(model)
            }
        }

    }

    interface OnImageSourceSelectedClickListener{
        fun onImageSourceSelected(source: ImageSourcesEnum)
    }
}