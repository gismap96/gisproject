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
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.symbology.*


object SketchEditorController {

    var sketchEditor = SketchEditor()
    var sketcherEditorTypes = SketcherEditorTypes.POINT
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

    fun area(mMapView: MapView, context: Context){
        val geometry = sketchEditor.geometry
        val envelope = geometry.extent
        var area = GeometryEngine.area(envelope)
        val unit = mMapView.spatialReference.unit.name
        var toastMSG1 = "השטח ל"
//        if (sketcherEditorTypes == SketcherEditorTypes.POLYLINE){
//            toastMSG1 = "המרחק ל"
//            GeometryEngine.length()
//        }
        val toastMSG2 = " הוא "
        val toastMSG3 = " ב "
        val finalMSG = toastMSG1+sketcherEditorTypes.title+toastMSG2+area.toInt().toString()+toastMSG3+unit
        val toast = Toast.makeText(context, finalMSG, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        Log.d(TAG, "the area for ${sketcherEditorTypes.title} is: $area, unit: $unit")

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
            SketcherEditorTypes.POINT -> {
            }
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