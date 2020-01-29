package com.bgvofir.grappygis.ClientFeatureLayers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import com.bgvofir.grappygis.ProjectRelated.ProjectId
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
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

    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry, callback: (()-> Unit)?){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        var pictureMarker = PictureMarkerSymbol(pinBlackStarDrawable)
        newAttributes["isUpdated"]?.let {
            if (it == "yes") pictureMarker = PictureMarkerSymbol(pinBlueStarDrawable)
        }
        pictureMarker.height = 40f
        pictureMarker.width = 40f
        renderer.symbol = pictureMarker
        val feature = featureCollectionTable.createFeature(newAttributes,geometry) as Feature
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature).addDoneListener {
            callback?.let {
                it()
            }
        }
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


    fun uploadJSON(callback: OnPointsUploaded){
        val mProjectID = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectID/userLayers/$username/points.json")
        val json = generateARCGISJSON().toString().toByteArray()
        childRef.putBytes(json).addOnSuccessListener {
            callback.onPointsUploadFinished()
            Log.d(TAG, "file sent")
        }.addOnFailureListener{
            e->
            Log.d(TAG, e.toString())
        }
    }

    fun generateARCGISJSON(): JSONObject{
        val resultJSON = JSONObject()
        resultJSON.put("displayFieldName", "")
        resultJSON.put("fieldAliases", generateFieldAliasesForJSON())
        resultJSON.put("geometryType", "esriGeometryPoint")
        resultJSON.put("spatialReference", generateSpatialReferenceJSON())
        resultJSON.put("fields", generateFieldsElementForJSON())
        resultJSON.put("features", generateFeaturesForJSON())
        return resultJSON
    }

    private fun generateFeaturesForJSON(): JSONArray{
        val json = JSONArray()
        var objectIdNum = 1
        features.forEach {
            var featureJSON = JSONObject()
            var attributesJSON = JSONObject()
            attributesJSON.put("OBJECTID", objectIdNum)
            val attributesMap = mutableMapOf<String, Any>()
            it.attributes.forEach{
                //if (it.key != "ObjectID") attributesJSON.put(it.key, it.value)
                if (it.value != null){
                    if (it.key != "ObjectID") attributesMap[it.key] = it.value
                }

            }
            fields.forEach {
                if (it.name == "number"){
                    attributesJSON.put(it.name, attributesMap[it.name] as Double)
                } else {
                    attributesJSON.put(it.name, attributesMap[it.name])
                }
            }
            featureJSON.put("attributes", attributesJSON)
            val geometryJson = JSONObject(it.geometry.toJson())
//            val geometryJSONArray = geometryJson.getJSONArray("paths")
//            val geometryWrapperJSON = JSONObject().put("paths", geometryJSONArray)
//            featureJSON.put("geometry", geometryWrapperJSON)
            val x = geometryJson.getDouble("x")
            val y = geometryJson.getDouble("y")
            val pointGeometryJSON = JSONObject()
            pointGeometryJSON.put("x", x)
            pointGeometryJSON.put("y", y)
            featureJSON.put("geometry", pointGeometryJSON)
            json.put(featureJSON)
            objectIdNum ++
        }
        return json
    }

    private fun generateFieldsElementForJSON(): JSONArray {
        val json = JSONArray()
        val objectid = JSONObject()
        objectid.put("name", "OBJECTID")
        objectid.put("type", "esriFieldTypeOID")
        objectid.put("alias", "OBJECTID")
        json.put(objectid)
        fields.forEach {
            val temp = JSONObject()
            temp.put("name", it.name)
            temp.put("type", it.type)
            temp.put("alias", it.alias)
            it.length?.let {
                length-> temp.put("length", length)
            }
            json.put(temp)
        }
        return json

    }

    private fun generateSpatialReferenceJSON():JSONObject{
        val sr = JSONObject()
        sr.put("wkid", spatialReference.wkid)
        sr.put("latestWkid", spatialReference.wkid)
        return sr
    }

    private fun generateFieldAliasesForJSON(): JSONObject{
        val json = JSONObject()
        json.put("OBJECTID", "OBJECTID")
        fieldsArray.forEach {
            json.put(it.name, it.alias)
        }
        return json
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

    interface OnPointsUploaded{
        fun onPointsUploadFinished()
    }
}