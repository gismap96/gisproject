package com.bgvofir.grappygis.ClientFeatureLayers

import android.graphics.Color
import android.util.Log
import com.bgvofir.grappygis.ClientLayersHandler.ClientLayersController
import com.bgvofir.grappygis.ClientLayersHandler.ClientLayersController.generatePointsArray
import com.bgvofir.grappygis.ProjectRelated.ProjectId
import com.bgvofir.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class ClientFeatureCollectionLayer () {
    val TAG = "CollectionFeature"
    var collection = FeatureCollection()
    var layer = FeatureCollectionLayer(collection)
    var features = mutableListOf<Feature>()
    private var name = "$$##"
    var spatialReference = SpatialReference.create(2039)
    var id = UUID.randomUUID().toString()
    var fields = mutableListOf<GrappiField>()
    private var fieldsArray = mutableListOf<Field>()
    private lateinit var featureCollectionTable: FeatureCollectionTable
    var lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 3.5f)
    var renderer = SimpleRenderer(lineSymbol)

    constructor(name: String, id: String): this(){
        this.name = name
        layer.name = "$name$$##"
        this.id = id
        generateIDField()
    }
    constructor(name: String, id: String, fields: MutableList<GrappiField>, spatialReference: SpatialReference): this(){
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
    constructor(name: String, id: String, fields: MutableList<GrappiField>, lineSymbol: SimpleLineSymbol, spatialReference: SpatialReference): this(){
        this.name = name
        layer.name = "$name$$##"
        this.id = id
        generateIDField()
        this.fields.plusAssign(fields)
        this.lineSymbol = lineSymbol
        this.spatialReference = spatialReference
        initProperties()
    }
    constructor(json: JSONObject, mMapView: MapView): this() {
        this.spatialReference = mMapView.spatialReference
        collection = FeatureCollection()
        layer = FeatureCollectionLayer(collection)
        layer.name = "שכבת משתמש" + "##$$"
        this.fieldsArray = ClientLayersController.generateFieldsArray(json)
        this.featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYLINE, spatialReference)
        this.featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        createFeaturesFromJSON(json)
        addFeatureList()
    }

    private fun addFeatureList(){
        Log.d(TAG, features[0].featureTable.hasGeometry().toString())
        featureCollectionTable.addFeaturesAsync(features)
    }
    private fun createFeaturesFromJSON(json: JSONObject) {
        val features = json.getJSONArray("features")
        val gson = Gson()
        for (i in 0 until features.length()) {
            val item = features.getJSONObject(i)
            val geometry = item.getJSONObject("geometry")
            val pointsArray = generatePointsArray(SketcherEditorTypes.POLYLINE, geometry, spatialReference)
            var pointsCollection = PointCollection(pointsArray)
            val polyline = PolylineBuilder(pointsCollection)
            val polylineGeometry = polyline.toGeometry()
            val attributesJSON = item.getJSONObject("attributes").toString()
            val attTypeToken = object : TypeToken<HashMap<String, Any>>() {}.type
            val mAttMap = gson.fromJson(attributesJSON, attTypeToken) as HashMap<String, Any>
            generateFeature(mAttMap, polylineGeometry)
        }
    }
    fun generateFeature(attributes: HashMap<String, Any>,geometry: Geometry){
        val feature = featureCollectionTable.createFeature(attributes, geometry) as Feature
        features.add(feature)
    }
    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        val feature = featureCollectionTable.createFeature(newAttributes,geometry) as Feature
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature)
    }

    private fun initProperties(){
        if (fields.size > 0){
            fieldsTransform()
        }
        featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYLINE, spatialReference)
        featureCollectionTable.renderer = renderer
        collection.tables.add(featureCollectionTable)
    }

    private fun generateIDField(){
//        fields.add(GrappiField("FID", "esriFieldTypeOID", "FID"))
        fields.add(GrappiField("Id", "esriFieldTypeString", "Id", 50))

    }
    fun setColor(alpha: Int, red: Int, green: Int, blue: Int, width: Float){
        lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH_DOT,Color.argb(alpha, red, green,blue),width)
    }

    fun setColor(){
        lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.GREEN, 4f)
        renderer = SimpleRenderer(lineSymbol)
        featureCollectionTable.renderer = renderer
    }


    fun setNameForLayer(name: String){
        this.name = name
        layer.name = name
    }

    fun addClientFeatureCollectionToMap(toMap: MapView){
        layer.name = name
        toMap.map.operationalLayers.add(layer)
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

    fun uploadJSON(){
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polyline.json")
        val json = generateARCGISJSON().toString().toByteArray()
        childRef.putBytes(json).addOnSuccessListener {
            Log.d(TAG, "file sent")
        }.addOnFailureListener{
            e->
            Log.d(TAG,e.toString())
        }
    }
    fun generateARCGISJSON(): JSONObject{
        var resultJson = JSONObject()
        resultJson.put("displayFieldName", "")
        resultJson.put("fieldAliases", generateFieldAliasesForJSON())
        resultJson.put("geometryType", "esriGeometryPolyline")
        resultJson.put("spatialReference", generateSpatialReferenceJSON())
        resultJson.put("fields", generateFieldsElementForJSON())
        resultJson.put("features", generateFeaturesForJSON())
        return resultJson
    }
//    fun polylineToJSON(geometry: Geometry){
//        val result = JSONObject()
//        result.put("displayFieldName", "meow")
//        result.put("fieldAliases", FormatJSONGeometry.fieldAlisas())
//        result.put("geometryType", "esriGeometryPolyline")
//        result.put("spatialReference", FormatJSONGeometry.spatialReference())
//        val fieldsElement = JSONArray()
//        fieldsElement.put(FormatJSONGeometry.fields("FID", "esriFieldTypeOID"))
//        fieldsElement.put(FormatJSONGeometry.fields("Id", "esriFieldTypeInteger"))
//        result.put("fields", fieldsElement)
//        var features = JSONArray()
//        var mFirstFeature = JSONObject()
//        mFirstFeature.put("attributes", FormatJSONGeometry.attributes(0, 0))
//        var geometryJSONObject = JSONObject()
//        geometryJSONObject.put("paths", FormatJSONGeometry.getPaths(geometry))
//        mFirstFeature.put("geometry", geometryJSONObject)
//
//        features.put(mFirstFeature)
//        result.put("features", features)
//        Log.d(FormatJSONGeometry.TAG, result.toString())
//    }

    private fun generateFeaturesForJSON(): JSONArray{
        val json = JSONArray()
        var objectIdNum = 1
        features.forEach {
            var featureJSON = JSONObject()
            var attributesJSON = JSONObject()
            attributesJSON.put("OBJECTID", objectIdNum)
            it.attributes.forEach{
                if (it.key != "ObjectID") attributesJSON.put(it.key, it.value)
            }
            featureJSON.put("attributes", attributesJSON)
            val geometryJson = JSONObject(it.geometry.toJson())
            val geometryJSONArray = geometryJson.getJSONArray("paths")
            val geometryWrapperJSON = JSONObject().put("paths", geometryJSONArray)
            featureJSON.put("geometry", geometryWrapperJSON)
            json.put(featureJSON)
            objectIdNum ++
        }
        return json
    }
    private fun generateFieldAliasesForJSON(): JSONObject{
        val json = JSONObject()
        fieldsArray.forEach {
            json.put(it.name, it.alias)
        }
        json.put("OBJECTID", "OBJECTID")
        return json
    }

    private fun generateSpatialReferenceJSON():JSONObject{
        val sr = JSONObject()
        sr.put("wkid", spatialReference.wkid)
        sr.put("latestWkid", spatialReference.wkid)
        return sr
    }

    private fun generateFieldsElementForJSON():JSONArray{
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
}