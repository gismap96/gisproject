package com.grappiapp.grappygis.ClientFeatureLayers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.*
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import java.util.*

class FlexibleFeatureLayer (val context: Context){

    val TAG = "FlexLayer"
    var WIDTH = 20f
    var HEIGHT = 20f
    var LINEWIDTH = 3.5f
    var type = SketcherEditorTypes.POINT
    var collection = FeatureCollection()
    var features = mutableListOf<Feature>()
    private var name = "$$##"
    var id = UUID.randomUUID().toString()
    var fields = mutableListOf<GrappiField>()
    val featureSearchMap = mutableMapOf<String, Feature>() // <ID, Feature>
    private var fieldsArray = mutableListOf<Field>()
    lateinit var layer: FeatureCollectionLayer
    private lateinit var featureCollectionTable: FeatureCollectionTable
    lateinit var spatialReference: SpatialReference
    lateinit var renderer: SimpleRenderer

    constructor(context: Context, name: String, grappiFields: MutableList<GrappiField>, spatialReference: SpatialReference, type: SketcherEditorTypes, mapView: MapView): this(context){
        collection = FeatureCollection() //create feature collection
        layer = FeatureCollectionLayer(collection) //create layer
        this.name = name
        this. fields = grappiFields
        generateIDField()
        val setFields = fields.toSet()
        this.type = type
        this.spatialReference = spatialReference
        fields.clear()
        fields = setFields.toMutableList()
        fieldsTransform()
        generateRenderer(context)
        this.spatialReference = spatialReference
        //create feature collction table
        createFeatureCollectionTable()
        featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        mapView.map.operationalLayers.add(layer)
    }

    private fun createFeatureCollectionTable(){
        var mFeatureCollectionTable: FeatureCollectionTable? = null
        when (type){
            SketcherEditorTypes.POINT -> {
                mFeatureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POINT, spatialReference)
            }
            SketcherEditorTypes.POLYLINE -> {
                mFeatureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYLINE, spatialReference)
            }
            SketcherEditorTypes.POLYGON -> {
                mFeatureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYGON, spatialReference)
            }
        }
        featureCollectionTable = mFeatureCollectionTable
    }
    fun generateRenderer(context: Context){
        when (type){
            SketcherEditorTypes.POINT -> generateRendererForPoint(context)
            SketcherEditorTypes.POLYLINE -> generateRendererForPolyline(context)
            SketcherEditorTypes.POLYGON -> generateRendererForPolygon(context)
        }
    }
    fun generateRendererForPoint(context: Context){
        val pinSymbol = BitmapDrawable(context.resources, getBitmapFromVectorDrawable(R.drawable.ic_star_blue))
        val pictureMarkerSymbol = PictureMarkerSymbol(pinSymbol)
        pictureMarkerSymbol.height = HEIGHT
        pictureMarkerSymbol.width = WIDTH
        renderer = SimpleRenderer(pictureMarkerSymbol)
    }

    fun generateRendererForPoint(pictureMarkerSymbol: PictureMarkerSymbol){
        renderer = SimpleRenderer(pictureMarkerSymbol)
    }

    fun generateRendererForPolyline(context: Context){
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(255,97,97) ,LINEWIDTH)
        renderer = SimpleRenderer(lineSymbol)
    }

    fun generateRendererForPolyline(lineSymbol: SimpleLineSymbol){
        renderer = SimpleRenderer(lineSymbol)
    }

    fun generateRendererForPolygon(context: Context){
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, ContextCompat.getColor(context, R.color.white) ,LINEWIDTH)
        val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.VERTICAL, ContextCompat.getColor(context, R.color.colorAccent), lineSymbol)
        renderer = SimpleRenderer(fillSymbol)
    }
    fun generateRendererForPolygon(fillSymbol: SimpleFillSymbol){
        renderer = SimpleRenderer(fillSymbol)
    }
    private fun generateIDField(){
        fields.add(GrappiField("Id", "esriFieldTypeString", "Id", 50))

    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    private fun fieldsTransform(){
        fields.forEach {
            when {
                it.type.contains("String") -> {
                    var length = 255
                    it.length?.let{
                        length = it
                    }
                    fieldsArray.add(Field.createString(it.name,it.alias, length))
                }
                it.type.contains("Integer") -> fieldsArray.add(Field.createInteger(it.name,it.alias))
                it.type.contains("Double") -> fieldsArray.add(Field.createDouble(it.name,it.alias))
            }
        }

    }


}