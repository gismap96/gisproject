package com.bgvofir.grappygis.LayerCalloutControl

import android.util.Log
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import java.util.concurrent.ExecutionException


object FeatureLayerController {

    fun layerClicked(point: android.graphics.Point, mMap: MapView, onLayerClickListener: OnLayerClickListener){
        identifyClickedLayerResults(point,mMap){
            res ->
            var layerNames = ArrayList<String>()
            res.forEach {
                layerNames.add(it.layerContent.name)
            }
            onLayerClickListener.onLayerClickListener(layerNames, res)
        }
    }

    fun layerDetails(forLayer: IdentifyLayerResult){
        val resultGeoElements = forLayer.elements
        if (!resultGeoElements.isEmpty()){
            var mAttributesString = ArrayList<String>()
            resultGeoElements.forEach {
                if (it is ArcGISFeature){
                    val mArcGISFeature = it as? ArcGISFeature
                    mAttributesString.add(mArcGISFeature?.attributes.toString())

                }
            }
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

    interface OnLayerClickListener{
        fun onLayerClickListener(layerNames: ArrayList<String>, identifiedLayers: MutableList<IdentifyLayerResult>)
    }

}