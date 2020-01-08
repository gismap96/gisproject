package com.bgvofir.grappygis.GeoViewController

import android.util.Log
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

object GeoViewController{

    var currentViewPoint: Viewpoint? = null
    val TAG = "geoViewController"

    fun calculateAndSetCurrentLocation(mMap: MapView){
        val mType = Viewpoint.Type.CENTER_AND_SCALE
        currentViewPoint = mMap.getCurrentViewpoint(mType)
    }

    fun setCurrentViewPointForMap(mMap: MapView){
        currentViewPoint?.let {
            mMap.setViewpointAsync(it)
            Log.d(TAG, currentViewPoint.toString())
        }

    }
}