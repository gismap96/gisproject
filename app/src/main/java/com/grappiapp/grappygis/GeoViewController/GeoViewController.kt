package com.grappiapp.grappygis.GeoViewController

import android.util.Log
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.MapView

object GeoViewController{

    var currentViewPoint: Viewpoint? = null
    val TAG = "geoViewController"
    var padding = 20.0

    fun calculateAndSetCurrentLocation(mMap: MapView){
        val mType = Viewpoint.Type.CENTER_AND_SCALE
        currentViewPoint = mMap.getCurrentViewpoint(mType)
    }

    fun moveToLocationByGeometry(envelope: Envelope, mMapView: MapView){
//        mMapView.setViewpointGeometryAsync(envelope, padding)
        mMapView.setViewpointAsync(Viewpoint(envelope)).addDoneListener {
            val scale = mMapView.mapScale * 1.2
            mMapView.setViewpointScaleAsync(scale)

        }


    }
    fun setCurrentViewPointForMap(mMap: MapView){
        currentViewPoint?.let {
            mMap.setViewpoint(it)
            Log.d(TAG, currentViewPoint.toString())
        }

    }
}