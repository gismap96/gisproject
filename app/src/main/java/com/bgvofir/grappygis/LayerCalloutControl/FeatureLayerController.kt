package com.bgvofir.grappygis.LayerCalloutControl

import android.util.JsonReader
import android.util.Log
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.StringReader
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
    private fun convertAttributeStringToMap(forString: ArrayList<String>): MutableList<Map<String, String>>{
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        var resultMapList = mutableListOf<Map<String, String>>()
        if (forString.size > 0){
            forString.forEach {
                val trimmed = it.trim()
                val mMap = Gson().fromJson<Map<String, String>>(trimmed, mapType)
                resultMapList.add(mMap)
            }
        }
        return resultMapList
    }

    fun layerDetails(forLayer: IdentifyLayerResult): ArrayList<Map<String, String>>{
        val resultGeoElements = forLayer.elements
        var mAttributesString = ArrayList<String>()
        var layersAttributeList = ArrayList<Map<String, String>>()

        if (!resultGeoElements.isEmpty()){
            resultGeoElements.forEach {
                if (it is ArcGISFeature){
                    val mArcGISFeature = it as? ArcGISFeature
                    mAttributesString.add(mArcGISFeature?.attributes.toString())
                    var mTempMap = mutableMapOf<String, String>()
                    mArcGISFeature?.attributes?.forEach {
                        if (!it.value.toString().isEmpty() && !it.key.toString().isEmpty()
                                && !it.key.toString().contains(".FID"))
                            mTempMap[it.key.toString()] = it.value.toString()
                    }
                    layersAttributeList.add(mTempMap)
                }
            }

        }

        return layersAttributeList
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