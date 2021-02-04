package com.grappiapp.grappygis.ClientFeatureLayers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.*
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import java.text.DecimalFormat
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
    var hasCalculatedAttribute = false
    var fields = mutableListOf<GrappiField>()
    val featureSearchMap = mutableMapOf<String, Feature>() // <ID, Feature>
    private var fieldsArray = mutableListOf<Field>()
    lateinit var layer: FeatureCollectionLayer
    private lateinit var featureCollectionTable: FeatureCollectionTable
    lateinit var spatialReference: SpatialReference
    lateinit var renderer: SimpleRenderer

    constructor(context: Context, name: String, grappiFields: MutableList<GrappiField>, spatialReference: SpatialReference, type: SketcherEditorTypes, mapView: MapView, hasCalculatedAttribute: Boolean): this(context) {
        collection = FeatureCollection() //create feature collection
        //create layer:
        layer = FeatureCollectionLayer(collection)
        this.name = name
        this.layer.name = "$name##$$"
        //initializing fields:
        this.fields = grappiFields
        generateIDField()
        val setFields = fields.toSet()
        this.spatialReference = spatialReference
        fields.clear()
        fields = setFields.toMutableList()
        fieldsTransform()
        this.hasCalculatedAttribute = hasCalculatedAttribute
        this.id = UUID.randomUUID().toString() //for future identification
        //generate renderer according to type:
        this.type = type // geometry type -> Grappi enum
        generateRenderer(context)
        //create feature collction table:
        featureCollectionTable = FeatureCollectionTable(fieldsArray, type.getARCGISGeometryType(), spatialReference)
        featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        //add layer to map view:
        mapView.map.operationalLayers.add(layer)
    }

    fun createFeature(attributes: HashMap<String, Any>, geometry: Geometry, callback: ()-> Unit){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        if (hasCalculatedAttribute){
            val calculatedAtt = addCalculatedAttribute(geometry)
            when (type){
                SketcherEditorTypes.POINT -> {}
                SketcherEditorTypes.POLYLINE -> {
                    newAttributes["length"] = calculatedAtt
                }
                SketcherEditorTypes.POLYGON -> {
                    newAttributes["area"] = calculatedAtt
                }
                SketcherEditorTypes.MULTIPOINTS -> {}
            }


        }
        val feature = featureCollectionTable.createFeature(newAttributes, geometry)
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature).addDoneListener {
            callback()
        }
    }

    private fun addCalculatedAttribute(geometry: Geometry): String{
        return when (type){
            SketcherEditorTypes.POINT -> {
                ""
            }
            SketcherEditorTypes.POLYLINE -> {
                calculateLength(geometry)
            }
            SketcherEditorTypes.POLYGON -> {
                calculateArea(geometry)
            }
            SketcherEditorTypes.MULTIPOINTS -> {""}
        }
    }
    private fun calculateLength(geometry: Geometry): String {
        val polyline = geometry as Polyline
        var length = GeometryEngine.length(polyline)
        val decimalFormat = DecimalFormat("#.00")
        val unit = spatialReference.unit.abbreviation
        if (unit == "mi") {
            length *= 1609.344
        }
        var formatDistance = decimalFormat.format(length).toString() + "m"
        if (formatDistance == ".00m") formatDistance = "0.00m"
        return formatDistance
    }

    private fun calculateArea(geometry: Geometry): String {
        val polygon = geometry as Polygon
        var area = GeometryEngine.area(polygon)
        val decimalFormat = DecimalFormat("#.00")
        val unit = spatialReference.unit.abbreviation
        if (unit == "mi") {
            area *= 1609.344
        }
        var formatArea = decimalFormat.format(area).toString() + "m²"
        if (formatArea == ".00m") formatArea = "0.00m"
        return formatArea
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