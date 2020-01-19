package com.bgvofir.grappygis.LayerDetailsDialog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController
import com.bgvofir.grappygis.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_for_layer_details_dialog.view.*
import java.lang.Exception
import java.util.ArrayList

class DialogLayerDetailsAdapter(val context: Context,val displayLayers: ArrayList<MutableMap<String, String>>): RecyclerView.Adapter<DialogLayerDetailsAdapter.DialogLayerDetailsAdapterViewHolder>(){

    val TAG = "layerDetails"
//    var elementsColor = mutableMapOf<Int, Boolean>()
    var rowValues = ArrayList<RowValue>()

    init{
        var isColored = false
//        elementsColor.clear()
        rowValues.clear()
        if (FeatureLayerController.isUserLayer){
            clientPolylineInit()

        }else {
            displayLayers.forEach {
                it.forEach {
                    if (it.key != "uid") {
                        val rowValue = RowValue(it.key, it.value)
                        //elementsColor[rowValues.size] = isColored
                        rowValues.add(rowValue)
                    }
//                if (it.key == "ObjectID"){
//                    isUserLayer = true
//                }
                }
                isColored = !isColored
            }
        }

    }
    private fun clientPolylineInit(){
        displayLayers.forEach {
            it.forEach {
                when (it.key){
                    "OBJECTID"-> rowValues.add(RowValue("FID", it.value))
                    "ObjectID"-> rowValues.add(RowValue("FID", it.value))
                    "category"-> rowValues.add(RowValue(context.resources.getString(R.string.category), it.value))
                    "description"-> rowValues.add(RowValue(context.resources.getString(R.string.description), it.value))
                    "isUpdated"-> {
                        if (it.value == "yes"){
                            rowValues.add(RowValue(context.resources.getString(R.string.update_system_show),
                                    context.resources.getString(R.string.yes)))
                        } else {
                            rowValues.add(RowValue(context.resources.getString(R.string.update_system_show),
                                    context.resources.getString(R.string.yes)))
                        }
                    }
                    "number"-> rowValues.add(RowValue(context.resources.getString(R.string.number), it.value))
                    "imageURL"-> {
                        if (it.value.count() > 4) {
                            rowValues.add(RowValue("imageURL", it.value))
                        }
                    }
                }
            }
        }
        val element = rowValues[rowValues.size - 1]
        rowValues.remove(element)
        rowValues.add(0, element)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DialogLayerDetailsAdapterViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.row_for_layer_details_dialog, p0, false)
        return DialogLayerDetailsAdapterViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, rowValues.size.toString())
        return rowValues.size
    }

    override fun onBindViewHolder(p0: DialogLayerDetailsAdapterViewHolder, p1: Int) {

        p0.bind(rowValues[p1].key, rowValues[p1].value, p1)
        if (rowValues[p1].key == "FID" || rowValues[p1].key == "ObjectID"){
            p0.itemView.setBackgroundColor(Color.WHITE)
            p0.itemView.rowLayersDetailsValue.setBackgroundColor(Color.WHITE)
            p0.itemView.rowLayersDetailsValue.setTextColor(ResourcesCompat.getColor(context.resources, R.color.dark_blue, null))
            p0.itemView.rowLayersDetailsKey.setBackgroundColor(Color.WHITE)
            p0.itemView.rowLayersDetailsKey.setTextColor(ResourcesCompat.getColor(context.resources, R.color.dark_blue, null))
        } else {
            val backgroundColor = ResourcesCompat.getColor(context.resources, R.color.details_dialog_background, null)
            val keyTextColor = ResourcesCompat.getColor(context.resources, R.color.details_dialog_key_text_color, null)
            val white = Color.WHITE
            p0.itemView.setBackgroundColor(Color.TRANSPARENT)
            p0.itemView.rowLayersDetailsValue.setBackgroundColor(backgroundColor)
            p0.itemView.rowLayersDetailsValue.setTextColor(white)
            p0.itemView.rowLayersDetailsKey.setBackgroundColor(backgroundColor)
            p0.itemView.rowLayersDetailsKey.setTextColor(keyTextColor)
        }
    }


    inner class DialogLayerDetailsAdapterViewHolder(v: View): RecyclerView.ViewHolder(v){
        private var keyTextView = v.rowLayersDetailsKey
        private var valueTextView = v.rowLayersDetailsValue
        private var previewImage = v.rowDetailsPreviewImageView

        fun bind(key: String, value: String, itemNum: Int){

            if (key == "תצוגה מקדימה"|| key == "imageURL"){
                valueTextView.visibility = View.GONE
                keyTextView.visibility = View.GONE
                previewImage.visibility = View.VISIBLE
                Picasso.get().load(value).placeholder(R.drawable.ic_placeholder).into(object: com.squareup.picasso.Target{
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        previewImage.setImageDrawable(placeHolderDrawable)
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                        bitmap?.let{
                            previewImage.scaleType = ImageView.ScaleType.CENTER_CROP
                            previewImage.setImageBitmap(bitmap)

                        }

                    }
                })
                previewImage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = (Uri.parse(value))
                    context.startActivity(intent)
                }

            }
            else {
                val newValue = value.replace("_"," ")
                valueTextView.text = newValue
            }
            val newKey = key.replace("_", " ")
            keyTextView.text = newKey

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