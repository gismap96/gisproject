package com.bgvofir.grappygis.LayerCalloutControl

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bgvofir.grappygis.LayerCalloutDialog.DialogLayerAdapter
import com.bgvofir.grappygis.MainActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.MapView
import java.util.concurrent.ExecutionException


object FeatureLayerController {

    fun layerClicked(point: android.graphics.Point, mMap: MapView, onlayerClickListener: OnlayerClickListener){
        identifyClickedLayerResults(point,mMap){
            res ->
            var layerNames = ArrayList<String>()
            res.forEach {
                layerNames.add(it.layerContent.name)
            }
            onlayerClickListener.onLayerClickListener(layerNames, res)
        }
    }

    /** identifies which layers were clicked
     *
     *
     */
    fun identifyClickedLayerResults(point: android.graphics.Point, mMap: MapView, callback: (MutableList<IdentifyLayerResult>) -> Unit){
        val TAG = "ClickedLayerResults"
        var identifyLayerResult: MutableList<IdentifyLayerResult>
        val identifyLayerResultsFuture = mMap
                .identifyLayersAsync(point, 12.0, false, 5)

        identifyLayerResultsFuture.addDoneListener {
            try {
                identifyLayerResult = identifyLayerResultsFuture.get()
                callback(identifyLayerResult)

            } catch (e: InterruptedException) {
                Log.e(TAG, "Error identifying results: " + e.message)
            } catch (e: ExecutionException) {
                Log.e(TAG, "Error identifying results: " + e.message)
            }
        }
    }

    interface OnlayerClickListener{
        fun onLayerClickListener(layerNames: ArrayList<String>, identifiedLayers: MutableList<IdentifyLayerResult>)
    }

}