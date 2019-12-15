package com.bgvofir.grappygis.LayerCalloutControl

import android.util.Log
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import java.util.concurrent.ExecutionException


object FeatureLayerController {

    fun layerClicked(point: android.graphics.Point, mMap: MapView, onLayerClickListener: OnLayerClickListener){
        identifyClickedLayerResults(point,mMap) { res ->

            if (res.size > 0) {
                var layerNames = ArrayList<String>()
//                var cleanList = mutableListOf<IdentifyLayerResult>()
                res.forEach {
                    val mLayerName = it.layerContent.name
                    layerNames.add(mLayerName)
//                        if (it.layerContent.name != "Feature Collection"){
//                            layerNames.add(mLayerName)
//                            cleanList.add(it)
//
//                        }
                }
                onLayerClickListener.onLayerClickListener(layerNames, res)
            }

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