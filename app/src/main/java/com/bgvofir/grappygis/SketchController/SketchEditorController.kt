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
    var isWorking = false
    var isEditMode = false

    fun getGeometry():Geometry?{
        return sketchEditor.geometry
    }
    fun openSketcherBarContainer(layout: ConstraintLayout){
        layout.visibility = View.VISIBLE
        if (isWorking) return else isWorking = true
        ObjectAnimator.ofFloat(layout,"translationY", layout.height.toFloat()).apply {
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
        distance = 0.0
        sketchEditor.start(SketchCreationMode.POLYLINE)
    }

    fun polygonMode(){
        area = 0.0
        sketchEditor.start(SketchCreationMode.POLYGON)
    }
    fun pointMode(geometry: Geometry){
        sketchEditor.start(geometry, SketchCreationMode.POINT)
    }
    fun polylineMode(geometry: Geometry){
        distance = 0.0
        sketchEditor.start(geometry, SketchCreationMode.POLYLINE)
    }

    fun polygonMode(geometry: Geometry){
        area = 0.0
        sketchEditor.start(geometry, SketchCreationMode.POLYGON)
    }

    fun clean(){
        sketchEditor.clearGeometry()
    }

    fun wertexOriginal(unit: String):String{
        val geometry = sketchEditor.geometry
        if (geometry.isEmpty) return "0.00m"
        val lastSection = mutableListOf<Point>()
        if (geometry.geometryType == GeometryType.POLYLINE) {
            val polyline = geometry as Polyline
            val lastPart = polyline.parts.last()
            val points = lastPart.points.toList()
            val pointsCount = points.count()
            if (pointsCount < 2) return "0.00m"
            lastSection.add(points[pointsCount-2])
            lastSection.add(points.last())
        }  else if (geometry.geometryType == GeometryType.POLYGON){
            val polyline = geometry as Polygon
            val lastPart = polyline.parts.last()
            val points = lastPart.points.toList()
            val pointsCount = points.count()
            if (pointsCount < 2) return "0.00m"
            lastSection.add(points[pointsCount-2])
            lastSection.add(points.last())
        } else {
            return "0.00m"
        }
        val pointsCollection = PointCollection(lastSection)
        val partToCalculate = Part(pointsCollection)
        val newPolyline = Polyline(partToCalculate)
        var length = GeometryEngine.length(newPolyline)
        val decimalFormat = DecimalFormat("#.00")
        if (unit == "mi"){
            length *= 1609.344
        }
        length = decimalFormat.format(length).toDouble()
        return length.toString() + "m"
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
//        dunam = area / 1000.0
//        formatDunam = decimalFormat.format(dunam).toString()
      //  if (formatDunam == ".00" || formatDunam == ".00m") formatDunam = "0.00m"
        var areaFormat = decimalFormat.format(area).toString()
        if (areaFormat == ".00m" || areaFormat == ".00") areaFormat = "0.00"
        areaFormat += "m²"
        return areaFormat
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

        var formatDistance = decimalFormat.format(distance).toString() + "m"
        if (formatDistance == ".00m") formatDistance = "0.00m"
        return formatDistance
//        val toast = Toast.makeText(context, toastMsg1+ formattedDistance+ toastMsg2, Toast.LENGTH_LONG)
//        toast.setGravity(Gravity.CENTER, 0, 0)
//        toast.show()
//        FormatJSONGeometry.polylineToJSON(geometry)
    }
    fun isPolylineNotEmpty():Boolean{
        return distance > 0.0
    }

    fun stopSketcher(layout: ConstraintLayout){
        isEditMode = false
        if (!isWorking) return else isWorking = false
        sketchEditor.stop()
        ObjectAnimator.ofFloat(layout,"translationY", layout.height.toFloat()).apply{
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
        isEditMode = false
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
            SketcherEditorTypes.POINT -> {
                pointMode()
            }
        }
    }

    fun startSketching(sketcherEditorTypes: SketcherEditorTypes, mMapView: MapView, geometry: Geometry) {
        sketchEditor.stop()
        isEditMode = true
        this.sketcherEditorTypes = sketcherEditorTypes
        this.sketchEditor = SketchEditor()
        sketchEditor = sketchEditor
        mMapView.sketchEditor = sketchEditor
        when (sketcherEditorTypes) {
            SketcherEditorTypes.POLYLINE -> {
                polylineMode(geometry)
            }
            SketcherEditorTypes.POLYGON -> {
                polygonMode(geometry)
            }
            SketcherEditorTypes.POINT ->{
                pointMode(geometry)
            }
        }
    }
    fun undo(){
        sketchEditor.undo()
    }
}