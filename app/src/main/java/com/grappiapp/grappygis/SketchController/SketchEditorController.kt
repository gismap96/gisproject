package com.grappiapp.grappygis.SketchController

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.*
import com.grappiapp.grappygis.R
import java.text.DecimalFormat
import java.util.*


object SketchEditorController {

    var sketchEditor = SketchEditor()
    var sketcherEditorTypes = SketcherEditorTypes.POLYLINE
    var layoutHeight = 0
    val TAG = "sketcherController"
    var distance = 0.0
    var area = 0.0
    var isWorking = false
    var isEditMode = false
    var trackerPoints = mutableListOf<Point>()
    var trackingTimer = Timer()
    var isTracking = false
    var isTrackerPolygon = false

    fun toggleTrackerMode(bottomContainer: ConstraintLayout, context: Context, mMapView: MapView, onShapeChangeTrackerListener: OnShapeChangeTrackerListener){
        val modeButton = bottomContainer.findViewById<ImageView>(R.id.trackerModeIV)
        if (isTrackerPolygon){
            modeButton.setImageDrawable(context.resources.getDrawable(R.drawable.ic_line_measurement))
        } else{
            modeButton.setImageDrawable(context.resources.getDrawable(R.drawable.ic_polygon_area_measurement))
        }
        isTrackerPolygon = !isTrackerPolygon
        if (isTrackerPolygon){
            sketcherEditorTypes = SketcherEditorTypes.POLYGON
        } else {
            sketcherEditorTypes = SketcherEditorTypes.POLYLINE
        }
        setTrackerSketcher(mMapView, onShapeChangeTrackerListener)

    }
//    fun getGeometryLastPoint(): Point{
//        val geometry = sketchEditor.geometry
//        when (sketcherEditorTypes){
//            SketcherEditorTypes.POINT -> {
//                return geometry as Point
//            }
//            SketcherEditorTypes.POLYLINE -> {
//                val polyline = geometry as Polyline
//                return getGeometryLastPoint(polyline)
//            }
//            SketcherEditorTypes.POLYGON -> {
//                val polygon = geometry as Polygon
//                return getGeometryLastPoint(polygon.toPolyline())
//            }
//            SketcherEditorTypes.MULTIPOINTS -> {
//                val
//            }
//        }
//    }
//    fun getGeometryLastPoint(polyline: Polyline): Point{
//        val points = polyline.parts.partsAsPoints
//        return points.last()
//    }

    fun isTrackingMode(): Boolean{
        return isTracking
    }
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
    fun multiPointMode(){
        sketchEditor.start(SketchCreationMode.MULTIPOINT)
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
    fun multiPointMode(geometry: Geometry){
        sketchEditor.start(geometry, SketchCreationMode.MULTIPOINT)
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
        if (geometry.isEmpty || geometry == null) return "0.00m"
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
//        val linearUnit = LinearUnit(LinearUnitId.METERS)
//        var length = GeometryEngine.lengthGeodetic(newPolyline.extent, linearUnit, GeodeticCurveType.NORMAL_SECTION)
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
        val unit = mMapView.spatialReference.unit.abbreviation
        var areaUnit = AreaUnit(AreaUnitId.SQUARE_METERS)
        if (unit == "mi"){
            areaUnit = AreaUnit(AreaUnitId.ACRES)
        }
        area = GeometryEngine.areaGeodetic(geometry, areaUnit, GeodeticCurveType.GEODESIC)
//        area = GeometryEngine.area(geometry as Polygon)
        Log.d(TAG, area.toString())
        val decimalFormat = DecimalFormat("#.00")
//        dunam = area / 1000.0
//        formatDunam = decimalFormat.format(dunam).toString()
      //  if (formatDunam == ".00" || formatDunam == ".00m") formatDunam = "0.00m"
        var areaFormat = decimalFormat.format(area).toString()
        if (areaFormat == ".00m" || areaFormat == ".00") areaFormat = "0.00"
        if (unit == "mi"){
            areaFormat += "acre"
        }else {
            areaFormat += "m²"
        }
        return areaFormat
    }

    fun polylineDistance(mMapView: MapView): String{
        val geometry = sketchEditor.geometry
        val line = geometry as Polyline
        distance = GeometryEngine.length(line)
//        val linearUnit = LinearUnit(LinearUnitId.METERS)
//        distance = GeometryEngine.lengthGeodetic(geometry, linearUnit, GeodeticCurveType.NORMAL_SECTION)
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
        val geometry = getGeometry()
        return geometry != null && !geometry.isEmpty
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

    fun trackerSketcherMode(bottomContainer: ConstraintLayout, context: Context){
        openSketcherBarContainer(bottomContainer)
        val undoBtn = bottomContainer.findViewById<ImageView>(R.id.undoSkecherIV)
        undoBtn.visibility = View.GONE
        val cleanBtn = bottomContainer.findViewById<ImageView>(R.id.cleanSketcherIV)
        cleanBtn.visibility = View.GONE
        val playBtn = bottomContainer.findViewById<ImageView>(R.id.startTrackingIV)
        playBtn.setImageDrawable(context.resources.getDrawable(android.R.drawable.ic_media_play))
        playBtn.visibility = View.VISIBLE
        val modeButton = bottomContainer.findViewById<ImageView>(R.id.trackerModeIV)
        modeButton.setImageDrawable(context.resources.getDrawable(R.drawable.ic_line_measurement))
        modeButton.visibility = View.VISIBLE
        sketcherEditorTypes = SketcherEditorTypes.POLYLINE
    }
    fun setTrackerFalse(){
        isTracking = false
        trackingTimer.cancel()
        trackerPoints= mutableListOf()
    }
    fun playTracker(mapView: MapView, bottomContainer: ConstraintLayout, onShapeChangeTrackerListener: OnShapeChangeTrackerListener){
        val playBtn = bottomContainer.findViewById<ImageView>(R.id.startTrackingIV)

        if (isTracking){
            playBtn.setImageDrawable(mapView.context.resources.getDrawable(android.R.drawable.ic_media_play))
            trackingTimer.cancel()
        }else {
            playBtn.setImageDrawable(mapView.context.resources.getDrawable(android.R.drawable.ic_media_pause))
            startTracking(mapView, onShapeChangeTrackerListener)
        }
        isTracking = !isTracking
    }
    fun startTracking(mapView: MapView, onShapeChangeTrackerListener: OnShapeChangeTrackerListener){
        val time = 2000
        setTrackerSketcher(mapView, onShapeChangeTrackerListener)

        trackingTimer = Timer()
        trackingTimer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                if (mapView.locationDisplay != null){
                    if (mapView.locationDisplay.location != null){
                        val currentLocation = mapView.locationDisplay.location.position
                        mapView.setViewpointCenterAsync(currentLocation, mapView.mapScale)
                        if (trackerPoints.size > 1){
                            val lastPoint = trackerPoints.last()
                            val vertexList = mutableListOf<Point>()
                            vertexList.add(lastPoint)
                            vertexList.add(currentLocation)
                            val pointsCollection = PointCollection(vertexList)
                            val polyline = Polyline(pointsCollection)
                            val linearUnit = LinearUnit(LinearUnitId.METERS)
                            val length = GeometryEngine.lengthGeodetic(polyline, linearUnit, GeodeticCurveType.NORMAL_SECTION)
                            Log.d(TAG, "length: $length")
                            if (length > 1.0){
                                trackerPoints.add(currentLocation)
                                drawTracker(mapView,onShapeChangeTrackerListener)
                            }
                        }else {
                            trackerPoints.add(currentLocation)
                            drawTracker(mapView, onShapeChangeTrackerListener)
                        }

                    }
                }
            }

        },0.toLong(),time.toLong())
    }

    private fun setTrackerSketcher(mapView: MapView, onShapeChangeTrackerListener: OnShapeChangeTrackerListener) {
        sketchEditor.stop()
        sketchEditor = SketchEditor()
        mapView.sketchEditor = sketchEditor
        if (isTrackerPolygon) {
            sketchEditor.start(SketchCreationMode.POLYGON)
        } else {
            sketchEditor.start(SketchCreationMode.POLYLINE)
        }
        drawTracker(mapView, onShapeChangeTrackerListener)
    }

    private fun drawTracker(mapView: MapView,onShapeChangeTrackerListener: OnShapeChangeTrackerListener) {
        if (trackerPoints.size == 0){
            return
        }
        val vertex = wertexOriginal(mapView.spatialReference.unit.abbreviation)
        if (isTrackerPolygon){
            val polygon = Polygon(PointCollection(trackerPoints))
            sketchEditor.replaceGeometry(polygon)
            onShapeChangeTrackerListener.OnShapeChangeTrackerListener(polygonArea(mapView), vertex)
        } else {
            val polyline = Polyline(PointCollection(trackerPoints))
            sketchEditor.replaceGeometry(polyline)
            onShapeChangeTrackerListener.OnShapeChangeTrackerListener(polylineDistance(mapView), vertex)
        }

    }

    fun startSketching(sketcherEditorTypes: SketcherEditorTypes, mMapView: MapView) {
        sketchEditor.stop()
        isEditMode = false
        if (sketcherEditorTypes == SketcherEditorTypes.TRACKER){
            return
        }
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
            SketcherEditorTypes.MULTIPOINTS, SketcherEditorTypes.HYDRANTS -> {
                multiPointMode()
            }
            SketcherEditorTypes.TRACKER -> {}
        }
    }

    fun startSketchingFreehand(mMapView: MapView) {
        sketchEditor.stop()
        isEditMode = false
        this.sketchEditor = SketchEditor()
        mMapView.sketchEditor = sketchEditor
        sketchEditor.start(SketchCreationMode.FREEHAND_LINE)
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
            SketcherEditorTypes.MULTIPOINTS, SketcherEditorTypes.HYDRANTS -> {
                multiPointMode(geometry)
            }
        }
    }
    fun undo(){
        sketchEditor.undo()
    }
    interface OnShapeChangeTrackerListener{
        fun OnShapeChangeTrackerListener(totalArea: String, vertex: String)
    }
}