package com.grappiapp.grappygis.SearchController

import android.util.Log
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController
import java.lang.Exception


object FeatureSearchController {

    val TAG = "featureSearch"
    var flag = false
    var featureLayerResult: FeatureLayer? = null
    var searchResults = mutableListOf<SearchResult>()

    fun getGroupTitles(): MutableList<String>{
        val legendGroups = LegendLayerDisplayController.legendGroups
        val titles = mutableListOf<String>()
        if (legendGroups.count() > 0) {
            legendGroups.forEach {
                titles.add(it.title)
            }
        }
        return titles
    }

    fun getLayerTitlesForCategory(category: String): MutableList<String>{
        val legendGroups = LegendLayerDisplayController.legendGroups
        val titles = mutableListOf<String>()
        val layers = mutableListOf<Layer>()
        if (legendGroups.count()> 0){
            legendGroups.forEach {
                if (it.title == category){
                    layers.plusAssign(it.layers.toMutableList())
                }
            }
        }
        layers.forEach {
            titles.add(it.name.replace("$$##",""))
        }
        return titles
    }

    fun searchInLayer(search: String, groupNum:Int, layerNum: Int, callback: (MutableList<SearchResult>) -> Unit )  {
        val legendGroups = LegendLayerDisplayController.legendGroups
        val layer = legendGroups[groupNum].layers[layerNum]
        val featureLayer = layer as FeatureLayer
        this.featureLayerResult = featureLayer
        val extent = featureLayer.fullExtent
        val query = QueryParameters()
        query.geometry = extent
        val future = featureLayer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
        val searchResults = mutableListOf<SearchResult>()
        this.searchResults = mutableListOf()
        future.addDoneListener {
            try{
                val result = future.get()
                result.forEach {
                    mFeature->
                    featureLayer.unselectFeature(mFeature)
                    mFeature.attributes.forEach { attribute->
                        val value = attribute.value.toString()
                        if (value.contains(search, ignoreCase = true)){
                            val fid = mFeature.attributes["FID"]
                            val fieldName = attribute.key
                            val fieldValue = attribute.value
                            val result = SearchResult(fid?.toString(), fieldName, fieldValue.toString(), mFeature)
                            searchResults.add(result)
                        }
                    }
                }
                this.searchResults = searchResults
                callback(searchResults)
            } catch (e: Exception){
                Log.e(TAG, "Select feature failed: $e")
            }
        }
    }

    fun turnLayerLabelOn(mMapView: MapView){
        val layers = mMapView.map.operationalLayers
        layers.forEach {
            if (it.name.contains("מספרי ח")){
                val feature = it as FeatureLayer
                //feature.labelDefinitions() to find if it has labels
                feature.isLabelsEnabled = !flag
                flag = !flag
            }
        }
    }
}

//                var flag = false
//                result.forEach {
//                    Log.d(TAG, it.attributes.toString())
//                    if (!flag){
//                        it.attributes.forEach { att ->
//                            val value = att.value.toString()
//                            if (value.contains(search, ignoreCase = true)){
//                                GeoViewController.moveToLocationByGeometry(it.geometry.extent, 10.0, mMapView)
//                                flag = !flag
//                            }
//                        }
//                    } else {
//                        featureLayer.unselectFeature(it)
//                    }
//                }
//                if (!flag){
//                    val context = mMapView.context
//                    Toast.makeText(context, "לא נמצאה ישות", Toast.LENGTH_SHORT).show()
//                }