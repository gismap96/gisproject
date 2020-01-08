package com.bgvofir.grappygis.SketchController

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.bgvofir.grappygis.FormatGeometry.FormatJSONGeometry
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.symbology.*
import java.text.DecimalFormat


object SketchEditorController {

    var sketchEditor = SketchEditor()
    var sketcherEditorTypes = SketcherEditorTypes.POLYLINE
    var layoutHeight = 0
    val TAG = "sketcherController"


    fun openSketcherBarContainer(layout: ConstraintLayout){
        layout.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(layout,"translationY", 200f).apply {
            duration = 0
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                ObjectAnimator.ofFloat(layout, "translationY", 0f).apply {
                    duration = 500
                    start()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })

    }
    fun initSketchBarContainer(layout: ConstraintLayout){
        layoutHeight = layout.height
        layout.visibility = View.GONE
    }
    fun freehandMode(){

        sketchEditor.start(SketchCreationMode.FREEHAND_LINE)
    }

    fun pointMode(){
        sketchEditor.start(SketchCreationMode.POINT)
    }
    fun savePoint(){
        val geometry = sketchEditor.geometry
        sketchEditor.stop()
        var graphic = Graphic(geometry)
    }
    fun polylineMode(){
        sketchEditor.start(SketchCreationMode.POLYLINE)
    }

    fun polygonMode(){
        sketchEditor.start(SketchCreationMode.POLYGON)
    }

    fun clean(mMapView: MapView){
        sketchEditor.clearGeometry()
    }

    fun toJson(geometry: Geometry){
        FormatJSONGeometry.polygonToJson(geometry)
        Log.d(TAG, geometry.toString())
    }
    fun polygonArea(mMapView: MapView, context: Context){
        val geometry = sketchEditor.geometry
        val envelope = geometry.extent
        var area = GeometryEngine.area(envelope)
        val unit = mMapView.spatialReference.unit.abbreviation
        if (unit == "mi"){
            area *= 1609.344
        }
        var toastMSG1 = "השטח ל"
//        if (sketcherEditorTypes == SketcherEditorTypes.POLYLINE){
//            toastMSG1 = "המרחק ל"
//            GeometryEngine.length()
//        }
        val toastMSG2 = " הוא "
        val toastMSG4 = "ובדונם הוא "
        val decimalFormat = DecimalFormat("#.00")
        val dunam = area / 1000.0
        val toastMSG5 = decimalFormat.format(dunam).toString()
        val finalMSG = toastMSG1+sketcherEditorTypes.title+toastMSG2+area.toInt().toString()+
                "m\n" + toastMSG4 +toastMSG5
        val toast = Toast.makeText(context, finalMSG, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        Log.d(TAG, "the polygonArea for ${sketcherEditorTypes.title} is: $area, unit: $unit")
        Log.d(TAG, "json ${geometry.toJson()}")
        toJson(geometry)
    }

    fun polylineDistance(mMapView: MapView, context: Context){
        val geometry = sketchEditor.geometry
        val line = geometry as Polyline
        var distance = GeometryEngine.length(line)
        val decimalFormat = DecimalFormat("#.00")
        val toastMsg1 = "מרחק הפוליליין הוא "
        val toastMsg2 = "m"
        val unit = mMapView.spatialReference.unit.abbreviation
        if (unit == "mi"){
            distance *= 1609.344
        }
        val formattedDistance = decimalFormat.format(distance).toString()
        val toast = Toast.makeText(context, toastMsg1+ formattedDistance+ toastMsg2, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        FormatJSONGeometry.polylineToJSON(geometry)
    }

    fun stopSketcher(layout: ConstraintLayout){
        sketchEditor.stop()
        ObjectAnimator.ofFloat(layout,"translationY", 200f).apply{
            duration = 500
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                layout.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
    }

    fun startSketching(sketcherEditorTypes: SketcherEditorTypes, mMapView: MapView) {
        sketchEditor.stop()
        this.sketcherEditorTypes = sketcherEditorTypes
        this.sketchEditor = SketchEditor()
        sketchEditor = sketchEditor
        mMapView.sketchEditor = sketchEditor
        when (sketcherEditorTypes) {
            SketcherEditorTypes.POLYLINE -> {
                polylineMode()
            }
            SketcherEditorTypes.POLYGON -> {
                polygonMode()
            }

        }
    }

    fun undo(){
        sketchEditor.undo()
    }
}