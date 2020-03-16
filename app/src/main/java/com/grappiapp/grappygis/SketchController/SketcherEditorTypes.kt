package com.grappiapp.grappygis.SketchController

import com.esri.arcgisruntime.geometry.GeometryType
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.R

enum class SketcherEditorTypes(val title: Int){
    //POINT("נקודה"),
//    DISTANCE("מרחק בין 2 נקודות"),
    POINT(R.string.point_layer),
    POLYLINE(R.string.polyline_layer),
    POLYGON(R.string.polygon_layer);

    fun getARCGISGeometryType(): GeometryType{
        return when (this){
            POINT -> {
                GeometryType.POINT
            }
            POLYLINE -> {
                GeometryType.POLYLINE
            }
            POLYGON -> {
                GeometryType.POLYGON
            }
        }
    }
    fun getImageAddress(): Int{
        return when (this){
            POINT -> R.drawable.ic_hollow_plus_star
            POLYLINE ->  R.drawable.ic_polyline_soft_red
            POLYGON ->  R.drawable.ic_polygon_area_measurement
        }
    }
    fun getOfflineReference():String{
        return when(this){
            POINT -> Consts.DOES_OFFLINE_POINT_DATA_EXIST
            POLYLINE -> Consts.DOES_OFFLINE_POLYLINE_DATA_EXIST
            POLYGON -> Consts.DOES_OFFLINE_POLYGON_DATA_EXIST
        }
    }
}