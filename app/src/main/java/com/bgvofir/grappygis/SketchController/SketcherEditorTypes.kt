package com.bgvofir.grappygis.SketchController

import com.bgvofir.grappygis.R

enum class SketcherEditorTypes(val title: Int){
    //POINT("נקודה"),
//    DISTANCE("מרחק בין 2 נקודות"),
    POLYLINE(R.string.measure_track),
    POLYGON(R.string.measure_polygon_area);
}