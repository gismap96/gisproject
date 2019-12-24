package com.bgvofir.grappygis.SketchController

import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol



object SketchEditorController {
    var isAttached = false
    var sketchEdit = SketchEditor()

    fun attachEditor(mMap: MapView){
        isAttached = true
    }
    fun detachEditor(mMap: MapView){
        isAttached = false
    }

    fun freehandMode(mMap: MapView){
        if(!isAttached) {
            isAttached = !isAttached
        }
        val mPointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, -0x10000, 20f)
        val mLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, -0x7800, 4f)
        val mFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, mLineSymbol)
        mMap.sketchEditor = sketchEdit
        sketchEdit.start(SketchCreationMode.FREEHAND_LINE)
    }
}