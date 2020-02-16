package com.grappiapp.grappygis.SearchController

import android.content.Context
import android.util.Log
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController
import com.grappiapp.grappygis.R
import java.lang.Exception


object FeatureSearchController {

    val TAG = "featureSearch"
    var flag = false
    var featureLayerResult: FeatureLayer? = null
    var searchResults = mutableListOf<SearchResult>()
    var isFeatureSelected = false
    var groupNumber = 0
    var layerNumber = 0

    fun unselectFeature(){
        featureLayerResult?.let{
            if (isFeatureSelected){
                searchResults.forEach {
                    featureLayerResult!!.unselectFeature(it.feature)
                    isFeatureSelected = false
                }
            }
        }
    }

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


    fun searchInLayer(search: String, featureLayer: FeatureLayer, isUserLayer: Boolean, context: Context, callback: (MutableList<SearchResult>) -> Unit )  {
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
                            var fid = mFeature.attributes["FID"]
                            if (fid == null){
                                fid = mFeature.attributes["ObjectId"]
                            }
                            var fieldName = attribute.key
                            val fieldValue = attribute.value
                            featureLayer.featureTable.fields.forEach {
                                if (it.name == fieldName) {
                                    var alias = it.alias
                                    if (isUserLayer){
                                        when (fieldName){
                                            "category" -> alias = context.getString(R.string.category)
                                            "description" -> alias = context.getString(R.string.description_alias)
                                            "isUpdated" -> {
                                                if (fieldValue == "yes"){
                                                    alias = context.getString(R.string.yes)
                                                } else {
                                                    alias = context.getString(R.string.no)
                                                }
                                            }
                                            "imageURL" -> return@forEach
                                            "length" -> alias = context.getString(R.string.length)
                                        }
                                    }
                                    val result = SearchResult(fid?.toString(), alias, fieldValue.toString(), mFeature)
                                    searchResults.add(result)
                                    return@forEach
                                }
                            }

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