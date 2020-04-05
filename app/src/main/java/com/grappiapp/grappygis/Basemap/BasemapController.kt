package com.grappiapp.grappygis.Basemap

import android.util.Log
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.raster.Raster

object BasemapController {
    val TAG = "BasemapCtrl"
    fun inserBasemap(mapView: MapView){
        val tiledVector = ArcGISVectorTiledLayer("https://basemaps.arcgis.com/arcgis/rest/services/OpenStreetMap_GCS_v2/VectorTileServer") as Layer
        tiledVector.isVisible = true
        val opr = mapView.map.operationalLayers.add(tiledVector)
        Log.d(TAG, opr.toString())
    }
}