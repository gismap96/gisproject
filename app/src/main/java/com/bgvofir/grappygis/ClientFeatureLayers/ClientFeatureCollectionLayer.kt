package com.bgvofir.grappygis.ClientFeatureLayers

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.bgvofir.grappygis.ClientLayersHandler.ClientLayersController
import com.bgvofir.grappygis.ClientLayersHandler.ClientLayersController.generatePointsArray
import com.bgvofir.grappygis.ProjectRelated.ProjectId
import com.bgvofir.grappygis.R
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
    var lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(255,97,97) ,3.5f)
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
        layer.name = "$name\$\$##"
        this.id = id
        generateIDField()
        this.fields.plusAssign(fields)
        this.lineSymbol = lineSymbol
        this.spatialReference = spatialReference
        initProperties()
    }
    constructor(json: JSONObject, mMapView: MapView, context: Context): this() {
        this.spatialReference = mMapView.spatialReference
        collection = FeatureCollection()
        layer = FeatureCollectionLayer(collection)
        layer.name = context.resources.getString(R.string.my_polyline) + "\$\$##"
        this.fieldsArray = ClientLayersController.generateFieldsArray(json)
        this.fields = ClientLayersController.generateGrappiFields(json)
        this.featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYLINE, spatialReference)
        this.featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        createFeaturesFromJSON(json)
        addFeatureList()
    }

    private fun addFeatureList(){
        featureCollectionTable.addFeaturesAsync(features)
    }
    private fun createFeaturesFromJSON(json: JSONObject) {
        val features = json.getJSONArray("features")
        val gson = Gson()
        for (i in 0 until features.length()) {
            val item = features.getJSONObject(i)
            item.getJSONObject("attributes").remove("OBJECTID")
            val geometry = item.getJSONObject("geometry")
            val pointsArray = generatePointsArray(SketcherEditorTypes.POLYLINE, geometry, spatialReference)
            var pointsCollection = PointCollection(pointsArray)
            val polyline = PolylineBuilder(pointsCollection)
            val polylineGeometry = polyline.toGeometry()
            val attributesJSON = item.getJSONObject("attributes").toString()
            val attTypeToken = object : TypeToken<HashMap<String, Any>>() {}.type
            val mAttMap = gson.fromJson(attributesJSON, attTypeToken) as HashMap<String, Any>
            generateFeatureForJSON(mAttMap, polylineGeometry)
        }
    }
    private fun generateFeatureForJSON(attributes: HashMap<String, Any>, geometry: Geometry){
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
        val setFields = fields.toSet()
        fields.clear()
        fields = setFields.toMutableList()
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

    fun uploadJSON(callback: OnPolylineUploadFinish){
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polyline.json")
        val json = generateARCGISJSON().toString().toByteArray()
        childRef.putBytes(json).addOnSuccessListener {
            callback.onPolylineUploadFinish()
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
        json.put("OBJECTID", "OBJECTID")
        fieldsArray.forEach {
            json.put(it.name, it.alias)
        }
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
    fun identifyFeatureById(id: String): Int{
        var count = 0
        features.forEach {
            if (it.attributes["Id"] == id) return count
            count++
        }
        return -1
    }
    fun deleteFeature(layerId: String, context: Activity){
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val objectID = identifyFeatureById(layerId)
        if (objectID < 0){
            Toast.makeText(context, "failed to update layer", Toast.LENGTH_LONG).show()
        }
        featureCollectionTable.deleteFeatureAsync(features[objectID]).addDoneListener{
            val url = features[objectID].attributes["imageURL"] as String
            val storage = FirebaseStorage.getInstance()
            if (url.count() > 5) {
                val reference = storage.getReferenceFromUrl(url)
                reference.delete().addOnSuccessListener {
                    features.removeAt(objectID)
                    uploadJSON(object : ClientFeatureCollectionLayer.OnPolylineUploadFinish {
                        override fun onPolylineUploadFinish() {
                            progressDialog.dismiss()
                            Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                        }
                    })
                }.addOnFailureListener {
                    features.removeAt(objectID)
                    uploadJSON(object : ClientFeatureCollectionLayer.OnPolylineUploadFinish {
                        override fun onPolylineUploadFinish() {
                            progressDialog.dismiss()
                            Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } else {
                features.removeAt(objectID)
                uploadJSON(object : ClientFeatureCollectionLayer.OnPolylineUploadFinish {
                    override fun onPolylineUploadFinish() {
                        progressDialog.dismiss()
                        Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }

    }


    interface OnPolylineUploadFinish{
        fun onPolylineUploadFinish()
    }
}