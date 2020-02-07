package com.grappiapp.grappygis.SearchController

import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.MapView

object FeatureSearchController {

    val TAG = "featureSearch"
    var flag = false

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

    fun searchInLayer(search: String, groupNum:Int, layerNum: Int): FeatureTable?{
        val legendGroups = LegendLayerDisplayController.legendGroups
        val layer = legendGroups[groupNum].layers[layerNum]
        val featureLayer = layer as FeatureLayer
        featureLayer
        return featureLayer.featureTable

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