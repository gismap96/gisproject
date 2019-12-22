package com.bgvofir.grappygis.LayerCalloutControl

import android.graphics.Point
import android.util.JsonReader
import android.util.Log
import com.bgvofir.grappygis.ClientPoint
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.StringReader
import java.util.concurrent.ExecutionException
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.concurrent.ListenableFuture






object FeatureLayerController {
    var point: android.graphics.Point? = null
    var tolerance = 10.0

    fun layerClicked(point: android.graphics.Point, mMap: MapView, onLayerClickListener: OnLayerClickListener){
        this.point = point
        identifyClickedLayerResults(point,mMap) { res ->

            if (res.size > 0) {
                var layerNames = ArrayList<String>()
                res.forEach {
                    val mLayerName = it.layerContent.name
                    layerNames.add(mLayerName)
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
        if (forLayer.layerContent.name == "Feature Collection"){
            //doesn't work here :(
            return featureCollectionDetails(forLayer)
        }
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

    private fun featureCollectionDetails(forLayer: IdentifyLayerResult): ArrayList<Map<String, String>>{
        var resultList = ArrayList<Map<String, String>>()
        forLayer.sublayerResults.forEach {
            var mTempMap = mutableMapOf<String, String>()
            it.elements.forEach {
                it.attributes.forEach {
                    if (it.key.contains("Description")){
                        mTempMap["תאור"] = it.value.toString()
                    }
                    if (it.key.contains("Category")){
                        mTempMap["קטגוריה"] = it.value.toString()
                    }
                    if (it.key.contains("URL") && it.value != null){
                        mTempMap["תצוגה מקדימה"] = it.value.toString()
                    }
                }
            }
            resultList.add(mTempMap)
        }
        val set = HashSet<Map<String, String>>(resultList)
        resultList.clear()
        resultList.addAll(set)
        return resultList
    }

    fun identifyAttForPointDeletion(forLayers: List<IdentifyLayerResult>): Int{
        forLayers.forEach {
            if (it.layerContent.name.toLowerCase() == "feature collection") {
                it.sublayerResults[0].elements.forEach {
                    it.attributes.forEach{
                        if (it.key.contains("CustomPointHash")){
                            return it.value as Int
                        }
                    }
                }
            }
        }
        return 0
    }

    fun featureCollectionHandle(forPoint : android.graphics.Point, forMap: MapView, identifiedLayer: IdentifyLayerResult){
        val envelope = Envelope(forPoint.x - tolerance, forPoint.y - tolerance,
                forPoint.x + tolerance, forPoint.y + tolerance, forMap.spatialReference)
        var query = QueryParameters()
        query.geometry = envelope
        val collection = identifiedLayer.layerContent as FeatureCollectionLayer
        val future = collection.layers[0].featureTable.queryFeaturesAsync(query)
        future.addDoneListener{
            val result = future.get()
            var mString = mutableListOf<String>()
            result.iterator().forEach {
                mString.add(it.attributes.toString())
            }
            print(mString)
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
                .identifyLayersAsync(point, tolerance, false, 5)

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

    interface OnFeatureCollectionListener{
        fun onFeatureCollectionListener()
    }
    interface OnLayerClickListener{
        fun onLayerClickListener(layerNames: ArrayList<String>, identifiedLayers: MutableList<IdentifyLayerResult>)
    }

}
//                (identifyLayerResult.get(0).layerContent as FeatureCollectionLayer).layers.get(0).featureTable
