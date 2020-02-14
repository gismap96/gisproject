package com.grappiapp.grappygis.SketchController

import com.grappiapp.grappygis.R

enum class SketcherEditorTypes(val title: Int){
    //POINT("נקודה"),
//    DISTANCE("מרחק בין 2 נקודות"),
    POINT(R.string.point_layer),
    POLYLINE(R.string.polyline_layer),
    POLYGON(R.string.polygon_layer);
}