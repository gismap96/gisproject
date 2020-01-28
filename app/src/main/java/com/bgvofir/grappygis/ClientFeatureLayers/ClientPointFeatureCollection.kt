package com.bgvofir.grappygis.ClientFeatureLayers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import java.util.*

class ClientPointFeatureCollection(val context: Context) {
    val TAG = "PointCollection"
    var collection = FeatureCollection()
    var layer = FeatureCollectionLayer(collection)
    var features = mutableListOf<Feature>()
    private var name = "$$##"
    var spatialReference = SpatialReference.create(2039)
    var id = UUID.randomUUID().toString()
    var fields = mutableListOf<GrappiField>()
    private var fieldsArray = mutableListOf<Field>()
    private lateinit var featureCollectionTable: FeatureCollectionTable
    val pinBlueStarDrawable = BitmapDrawable(context.resources, getBitmapFromVectorDrawable(R.drawable.ic_star_blue))
    val pinBlackStarDrawable = BitmapDrawable(context.resources, getBitmapFromVectorDrawable(R.drawable.ic_star_black))
    var pictureMarkerSymbol = PictureMarkerSymbol(pinBlackStarDrawable)
    var renderer = SimpleRenderer(pictureMarkerSymbol)

    constructor(context: Context, name: String, id: String, fields: MutableList<GrappiField>, spatialReference: SpatialReference): this(context){
        collection = FeatureCollection()
        layer = FeatureCollectionLayer(collection)
        this.name = name
        layer.name = "$name$$##"
        generateIDField()
        this.id = id
        this.fields.plusAssign(fields)
        this.spatialReference = spatialReference
        initProperties()
    }

    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        var pictureMarker = PictureMarkerSymbol(pinBlackStarDrawable)
        newAttributes["isUpdated"]?.let {
            if (it == "yes") pictureMarker = PictureMarkerSymbol(pinBlueStarDrawable)
            pictureMarkerSymbol.height = 20f
            pictureMarkerSymbol.width = 20f
        }
        renderer.symbol = pictureMarker
        val feature = featureCollectionTable.createFeature(newAttributes,geometry) as Feature
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature)
    }

    private fun initProperties(){
        val setFields = fields.toSet()
        fields.clear()
        fields = setFields.toMutableList()
        if (fields.size > 0){
            fieldsTransform()
        }
        pictureMarkerSymbol.height = 20f
        pictureMarkerSymbol.width = 20f
        featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POINT, spatialReference)
        featureCollectionTable.renderer = renderer
        collection.tables.add(featureCollectionTable)
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
}