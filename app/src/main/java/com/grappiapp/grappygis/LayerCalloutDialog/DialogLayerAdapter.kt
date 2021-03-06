package com.grappiapp.grappygis.LayerCalloutDialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import kotlinx.android.synthetic.main.row_for_callout_dialog.view.*
import java.util.concurrent.ExecutionException
import com.grappiapp.grappygis.R
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView


class DialogLayerAdapter(val context: Context,var layerNames: ArrayList<String>,internal var onRowClickListener: OnRowClickListener, val identifiedLayers: MutableList<IdentifyLayerResult>): RecyclerView.Adapter<DialogLayerAdapter.DialogLayerAdapterViewHolder>(){

//    var keyList =  arrayListOf<String>()
//    var valueList =  arrayListOf<String>()
//
//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        keyList.clear()
//        valueList.clear()
//        layers.forEach {
//            keyList.add(it.key)
//            valueList.add(it.value)
//        }
//    }
    init {
        layerNames = ArrayList(layerNames.distinct())
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DialogLayerAdapterViewHolder {

        val v = LayoutInflater.from(p0.context).inflate(com.grappiapp.grappygis.R.layout.row_for_callout_dialog, p0, false)
        return DialogLayerAdapterViewHolder(v)

    }

    override fun getItemCount(): Int {
        if (layerNames.size > 5){
            return 5
        }
        return layerNames.size
    }

    override fun onBindViewHolder(p0: DialogLayerAdapterViewHolder, p1: Int) {
//        val mIndex = keyList[p1]
        val mTitle = layerNames[p1]

//        p0.bind(mTitle, onRowClickListener)

        p0.bind(mTitle, identifiedLayers[p1], onRowClickListener)
    }

    inner class DialogLayerAdapterViewHolder(v: View): RecyclerView.ViewHolder(v){
        private var mTextView: TextView = v.text_for_row_callout_dialog
        private var mLayerSelectionDialogLegendImage = v.layerSelectionDialogLegendImage

        fun bind(layerTitle: String, identifiedLayer: IdentifyLayerResult,onRowClickListener: OnRowClickListener){

            val newTitle = layerTitle.replace("\$\$##", "")
            //getting legend image
            mTextView.text = newTitle
            var layerLegend = identifiedLayer.layerContent.fetchLegendInfosAsync()
            layerLegend.addDoneListener {
                try {
                    var legendInfo = layerLegend.get()
                    if (legendInfo.size > 0) {
                        val legendSymbol = legendInfo[0].symbol
                        val symbolSwatch = legendSymbol.createSwatchAsync(context, Color.TRANSPARENT)
                        val symbolBitmap = symbolSwatch.get()
                        mLayerSelectionDialogLegendImage.setImageBitmap(symbolBitmap)
                    }
                } catch (e: InterruptedException) {

                } catch (e: ExecutionException) {

                }
            }
            //end of legend image
            itemView.setOnClickListener {
                onRowClickListener.onRowClickListener(layerTitle, identifiedLayer)
            }
            if (layerTitle.contains("\$\$##") && layerTitle.contains(context.getString(R.string.polyline_layer))){
                mLayerSelectionDialogLegendImage.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_polyline_soft_red))
            } else if (layerTitle.contains("\$\$##") && layerTitle.contains(context.getString(R.string.my_points))){
                mLayerSelectionDialogLegendImage.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_star_blue))
            } else if (layerTitle.contains("\$\$##") && layerTitle.contains(context.getString(R.string.my_polygon))) {
                mLayerSelectionDialogLegendImage.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_polygon_area_measurement))
            }
        }
    }

    interface OnRowClickListener{
        fun onRowClickListener(layerIndex: String, layerIdentified: IdentifyLayerResult)
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }



}
