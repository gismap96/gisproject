package com.bgvofir.grappygis.LayerDetailsDialog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bgvofir.grappygis.LayerCalloutDialog.DialogLayerAdapter
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.view.*
import kotlinx.android.synthetic.main.row_for_layer_details_dialog.view.*
import java.lang.Exception
import java.util.ArrayList

class DialogLayerDetailsAdapter(val context: Context, displayLayers: ArrayList<Map<String, String>>): RecyclerView.Adapter<DialogLayerDetailsAdapter.DialogLayerDetailsAdapterViewHolder>(){

    var elementsColor = mutableMapOf<Int, Boolean>()
    var rowValues = ArrayList<RowValue>()

    init{
        var isColored = false
        elementsColor.clear()
        rowValues.clear()
        displayLayers.forEach {
            it.forEach {
                val rowValue = RowValue(it.key, it.value)
                elementsColor[rowValues.size] = isColored
                rowValues.add(rowValue)
            }
            isColored = !isColored
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DialogLayerDetailsAdapterViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(com.bgvofir.grappygis.R.layout.row_for_layer_details_dialog, p0, false)
        return DialogLayerDetailsAdapterViewHolder(v)
    }


    override fun getItemCount(): Int {
        return rowValues.size
    }

    override fun onBindViewHolder(p0: DialogLayerDetailsAdapterViewHolder, p1: Int) {
        elementsColor[p1]?.let {
            p0.bind(rowValues[p1].key, rowValues[p1].value)
            if (it){
                p0.itemView.setBackgroundColor(Color.GRAY)
            } else {
                p0.itemView.setBackgroundColor(Color.WHITE)
            }
        }
    }


    inner class DialogLayerDetailsAdapterViewHolder(v: View): RecyclerView.ViewHolder(v){
        private var keyTextView = v.rowLayersDetailsKey
        private var valueTextView = v.rowLayersDetailsValue
        private var previewImage = v.rowDetailsPreviewImageView

        fun bind(key: String, value: String){
            if (key == "תצוגה מקדימה"){
                valueTextView.visibility = View.GONE
                Picasso.get().load(value).into(previewImage)
                previewImage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = (Uri.parse(value))
                    context.startActivity(intent)
                }

            } else {
                valueTextView.text = value
                previewImage.visibility = View.GONE
            }

            keyTextView.text = key

        }
    }
    inner class RowValue(key: String, value: String){
        var key = ""
        var value = ""
        init {
            this.key = key
            this.value = value
        }
    }
}