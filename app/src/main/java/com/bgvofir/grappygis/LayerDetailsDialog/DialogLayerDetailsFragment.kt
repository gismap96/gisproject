package com.bgvofir.grappygis.LayerDetailsDialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.*
import android.support.v7.widget.DividerItemDecoration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController
import com.bgvofir.grappygis.ProjectRelated.UserPolyline
import com.bgvofir.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import java.util.concurrent.ExecutionException


class DialogLayerDetailsFragment(var activity: Activity, internal var adapter: RecyclerView.Adapter<*>, var headline: String, val identifiedLayer: IdentifyLayerResult, val context: Activity, val callback: OnEditSelectedListener): Dialog(activity), View.OnClickListener {

    val TAG = "DialogDetails"
    override fun onStart() {
        super.onStart()
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(com.bgvofir.grappygis.R.layout.fragment_dialog_layer_details)
        var layoutManager = LinearLayoutManager(activity)
        var recycler = dialogLayerDetailsRecyclerview
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
        var finalHeadline = headline.replace("\$\$##", "")
        if (headline == "Feature Collection") {
            finalHeadline = context.resources.getString(com.bgvofir.grappygis.R.string.client_point)
            layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_star_blue))
        } else if (headline.contains(activity.resources.getString(R.string.my_polyline))){
            finalHeadline = activity.resources.getString(R.string.my_polyline)
            layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_polyline_soft_red))
        } else {
            var layerLegend = identifiedLayer.layerContent.fetchLegendInfosAsync()
            layerLegend.addDoneListener {
                try {
                    var legendInfo = layerLegend.get()
                    if (legendInfo.size > 0) {
                        val legendSymbol = legendInfo[0].symbol
                        val symbolSwatch = legendSymbol.createSwatchAsync(context, Color.TRANSPARENT)
                        val symbolBitmap = symbolSwatch.get()
                        layerIconForDetailsDialog.setImageBitmap(symbolBitmap)
                    }
                } catch (e: InterruptedException) {

                } catch (e: ExecutionException) {

                }
            }
        }
        editClientLayerGeometryIV.visibility = View.GONE
        deleteClientLayerIV.visibility = View.GONE
        editClientLayerAttributesIV.visibility = View.GONE
        if (FeatureLayerController.isUserLayer){
            deleteClientLayerIV.visibility = View.VISIBLE
            editClientLayerGeometryIV.visibility = View.VISIBLE
            editClientLayerAttributesIV.visibility = View.VISIBLE
            deleteClientLayerIV.setOnClickListener(this)
            editClientLayerGeometryIV.setOnClickListener(this)
            
        }
        fragmentDialogLayerDetailsHeadline.text = finalHeadline
        fragmentDialogLayerDetailsClose.setOnClickListener(this)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fragmentDialogLayerDetailsClose ->{
                dismiss()
            }
            R.id.deleteClientLayerIV->{
               // Toast.makeText(context, "objectid: ${FeatureLayerController.layerId}", Toast.LENGTH_LONG).show()
                val builder = AlertDialog.Builder(context)
                builder.setMessage(context.getString(R.string.delete_layer_dialog_message))
                        .setTitle(context.getString(R.string.delete_layer_dialog_headline))
                        .setPositiveButton(R.string.yes,
                                DialogInterface.OnClickListener { dialog, id ->
                                    UserPolyline.userPolyline?.let {
                                        it.deleteFeature(FeatureLayerController.layerId, context)
                                    }
                                    dismiss()
                                })
                        .setNegativeButton(R.string.cancel,
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog

                                })
                // Create the AlertDialog object and return it
                builder.create().show()

            }
            R.id.editClientLayerGeometryIV->{
                val layerId = FeatureLayerController.layerId
                if (layerId.count() > 0){
                    callback.onEditSelectedListener(SketcherEditorTypes.POLYLINE, layerId)
                } else {
                    Log.d(TAG, "error loading feature")
                }
                dismiss()
            }
        }
    }

    interface OnEditSelectedListener{
        fun onEditSelectedListener(type: SketcherEditorTypes, layerId: String)
    }
}