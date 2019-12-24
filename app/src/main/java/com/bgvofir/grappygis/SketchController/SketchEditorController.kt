package com.bgvofir.grappygis.SketchController

import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol



object SketchEditorController {

    fun freehandMode(mMap: MapView){

//        val mPointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, -0x10000, 20f)
//        val mLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, -0x7800, 4f)
//        val mFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, mLineSymbol)
        var sketchEditor = SketchEditor()
        mMap.sketchEditor = sketchEditor
        sketchEditor.start(SketchCreationMode.FREEHAND_LINE)
    }
    fun polylineMode(mMap: MapView){
        var sketchEditor = SketchEditor()
        mMap.sketchEditor = sketchEditor
        sketchEditor.start(SketchCreationMode.POLYLINE)
    }

    fun polygonMode(mMap: MapView){
        var sketchEditor = SketchEditor()
        mMap.sketchEditor = sketchEditor
        sketchEditor.start(SketchCreationMode.POLYGON)
    }
}