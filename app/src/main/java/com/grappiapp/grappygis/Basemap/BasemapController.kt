package com.grappiapp.grappygis.Basemap

import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView

object BasemapController {
    fun inserBasemap(mapView: MapView){
        val tiledVector = ArcGISVectorTiledLayer("https://basemaps.arcgis.com/arcgis/rest/services/OpenStreetMap_GCS_v2/VectorTileServer")
        val basemap = Basemap(tiledVector)
        val map = ArcGISMap(basemap)
        mapView.map = map
    }
}