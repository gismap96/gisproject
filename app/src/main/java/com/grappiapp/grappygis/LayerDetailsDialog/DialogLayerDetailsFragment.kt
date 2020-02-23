package com.grappiapp.grappygis.LayerDetailsDialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grappiapp.grappygis.ClientLayerPhotoController.ClientPhotoController
import com.grappiapp.grappygis.LayerCalloutControl.FeatureLayerController
import com.grappiapp.grappygis.ProjectRelated.UserPoints
import com.grappiapp.grappygis.ProjectRelated.UserPolyline
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import com.grappiapp.grappygis.SketchController.SketcherSaveDialogFragment
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.GeoViewController.GeoViewController
import com.grappiapp.grappygis.ProjectRelated.UserPolygon
import java.util.concurrent.ExecutionException


class DialogLayerDetailsFragment(val mMap: MapView, var activity: Activity, internal var adapter: RecyclerView.Adapter<*>, var headline: String, val identifiedLayer: IdentifyLayerResult, val context: Activity, val callback: OnEditSelectedListener): Dialog(activity), View.OnClickListener {

    val TAG = "DialogDetails"
    override fun onStart() {
        super.onStart()
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_layer_details)
        var layoutManager = LinearLayoutManager(activity)
        var recycler = dialogLayerDetailsRecyclerview
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
        var finalHeadline = headline.replace("\$\$##", "")
        when {
            headline.contains(activity.resources.getString(R.string.my_points)) -> {
                finalHeadline = context.resources.getString(R.string.my_points)
                layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_star_blue))
            }
            headline.contains(activity.resources.getString(R.string.my_polyline)) -> {
                finalHeadline = activity.resources.getString(R.string.my_polyline)
                layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_polyline_soft_red))
            }
            headline.contains(context.getString(R.string.my_polygon)) -> {
                finalHeadline = context.getString(R.string.my_polygon)
                layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(R.drawable.ic_polygon_area_measurement))
            }
            else -> {
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
        }
        upperEditLayerCommandBar.visibility = View.GONE
        if (FeatureLayerController.isUserLayer){
            upperEditLayerCommandBar.visibility = View.VISIBLE
            deleteClientLayerIV.setOnClickListener(this)
            editClientLayerGeometryIV.setOnClickListener(this)
            editClientLayerAttributesIV.setOnClickListener(this)
            editFeatureImageIV.setOnClickListener(this)
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
                val layerId = FeatureLayerController.layerId
               // Toast.makeText(context, "objectid: ${FeatureLayerController.layerId}", Toast.LENGTH_LONG).show()
                val builder = AlertDialog.Builder(context)
                builder.setMessage(context.getString(R.string.delete_layer_dialog_message))
                        .setTitle(context.getString(R.string.delete_layer_dialog_headline))
                        .setPositiveButton(R.string.yes,
                                DialogInterface.OnClickListener { dialog, id ->
                                    when (FeatureLayerController.shapeType){
                                        SketcherEditorTypes.POINT -> {
                                            UserPoints.userPoints!!.deleteFeature(layerId, context)
                                        }
                                        SketcherEditorTypes.POLYLINE -> {
                                            UserPolyline.userPolyline?.let {
                                                it.deleteFeature(layerId, context)
                                            }
                                        }
                                        SketcherEditorTypes.POLYGON -> {
                                            UserPolygon.userPolygon?.let{
                                                it.deleteFeature(layerId, context)
                                            }
                                        }
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
                    callback.onEditSelectedListener(FeatureLayerController.shapeType, layerId)
                } else {
                    Log.d(TAG, "error loading feature")
                }
                dismiss()
            }
            R.id.editClientLayerAttributesIV->{
                dismiss()
                val saveDialog = SketcherSaveDialogFragment(context, mMap, null, null, null, true)
                saveDialog.show()
            }
            R.id.editFeatureImageIV->{
                dismiss()
                GeoViewController.setNewSavedViewPoint(mMap)
                ClientPhotoController.editPhoto(context)
            }
        }
    }

    interface OnEditSelectedListener{
        fun onEditSelectedListener(type: SketcherEditorTypes, layerId: String)
    }
}