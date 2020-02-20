package com.grappiapp.grappygis.SketchController

import com.esri.arcgisruntime.geometry.GeometryType
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
}