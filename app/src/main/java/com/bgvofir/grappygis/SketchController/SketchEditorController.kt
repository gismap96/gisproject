package com.bgvofir.grappygis.SketchController

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.bgvofir.grappygis.FormatGeometry.FormatJSONGeometry
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.*
import java.text.DecimalFormat


object SketchEditorController {

    var sketchEditor = SketchEditor()
    var sketcherEditorTypes = SketcherEditorTypes.POLYLINE
    var layoutHeight = 0
    val TAG = "sketcherController"
    var distance = 0.0
    var area = 0.0
    var dunam = 0.0
    var formatArea = ""
    var formatDunam = ""
    var formatDistance = ""

    fun getGeometry():Geometry?{
        return sketchEditor.geometry
    }
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

    fun wertexOriginal(unit: String):Double{
        val geometry = sketchEditor.geometry
        if (geometry.isEmpty) return 0.0
        val lastSection = mutableListOf<Point>()
        if (geometry.geometryType == GeometryType.POLYLINE) {
            val polyline = geometry as Polyline
            val lastPart = polyline.parts.last()
            val points = lastPart.points.toList()
            val pointsCount = points.count()
            if (pointsCount < 2) return 0.0
            lastSection.add(points[pointsCount-2])
            lastSection.add(points.last())
        }  else if (geometry.geometryType == GeometryType.POLYGON){
            val polyline = geometry as Polygon
            val lastPart = polyline.parts.last()
            val points = lastPart.points.toList()
            val pointsCount = points.count()
            if (pointsCount < 2) return 0.0
            lastSection.add(points[pointsCount-2])
            lastSection.add(points.last())
        } else {
            return 0.0
        }
        val pointsCollection = PointCollection(lastSection)
        val partToCalculate = Part(pointsCollection)
        val newPolyline = Polyline(partToCalculate)
        var length = GeometryEngine.length(newPolyline)
        if (unit == "mi"){
            length *= 1609.344
        }
        return length
    }
    fun polygonArea(mMapView: MapView): String{
        val geometry = sketchEditor.geometry
        val envelope = geometry.extent
        area = GeometryEngine.area(envelope)
        val unit = mMapView.spatialReference.unit.abbreviation
        if (unit == "mi"){
            area *= 1609.344
        }
        val decimalFormat = DecimalFormat("#.00")
        val formatArea = decimalFormat.format(area).toString()
        dunam = area / 1000.0
        formatDunam = decimalFormat.format(dunam).toString()
        val msg1 = "m"
        val msg2 = "דונם"
        val sectionLength = decimalFormat.format(wertexOriginal(unit))
        val msg3 = "מקטע "
        val finalmsg = "$msg2 $formatDunam\n $msg3${sectionLength}m"
        return finalmsg
        //val toast = Toast.makeText(context, finalMSG, Toast.LENGTH_LONG)
        //toast.setGravity(Gravity.CENTER, 0, 0)
        //toast.show()
        //Log.d(TAG, "the polygonArea for ${sketcherEditorTypes.title} is: $area, unit: $unit")
        //Log.d(TAG, "json ${geometry.toJson()}")
      //  toJson(geometry)
    }

    fun polylineDistance(mMapView: MapView): String{
        val geometry = sketchEditor.geometry
        val line = geometry as Polyline
        distance = GeometryEngine.length(line)
        val decimalFormat = DecimalFormat("#.00")
        val unit = mMapView.spatialReference.unit.abbreviation
        if (unit == "mi"){
            distance *= 1609.344
        }
        val section = wertexOriginal(unit)

        val msg2 = " ומקטע "
        val formatDistance = decimalFormat.format(distance).toString() +"m\n" + msg2 +
                decimalFormat.format(section) + "m"
        return formatDistance
//        val toast = Toast.makeText(context, toastMsg1+ formattedDistance+ toastMsg2, Toast.LENGTH_LONG)
//        toast.setGravity(Gravity.CENTER, 0, 0)
//        toast.show()
//        FormatJSONGeometry.polylineToJSON(geometry)
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