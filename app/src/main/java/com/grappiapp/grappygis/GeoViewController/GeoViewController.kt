package com.grappiapp.grappygis.GeoViewController

import android.util.Log
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

object GeoViewController{

    var currentViewPoint: Viewpoint? = null
    val TAG = "geoViewController"

    fun calculateAndSetCurrentLocation(mMap: MapView){
        val mType = Viewpoint.Type.CENTER_AND_SCALE
        currentViewPoint = mMap.getCurrentViewpoint(mType)
    }

    fun moveToLocationByGeometry(envelope: Envelope, padding: Double, mMapView: MapView){
        mMapView.setViewpointGeometryAsync(envelope, padding)
    }
    fun setCurrentViewPointForMap(mMap: MapView){
        currentViewPoint?.let {
            mMap.setViewpointAsync(it)
            Log.d(TAG, currentViewPoint.toString())
        }

    }
}