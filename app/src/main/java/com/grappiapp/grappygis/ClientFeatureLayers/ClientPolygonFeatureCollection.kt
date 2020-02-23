package com.grappiapp.grappygis.ClientFeatureLayers

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.grappiapp.grappygis.ClientLayerPhotoController.ClientPhotoController
import com.grappiapp.grappygis.ClientLayersHandler.ClientLayersController
import com.grappiapp.grappygis.GeoViewController.GeoViewController
import com.grappiapp.grappygis.ProjectRelated.MapProperties
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*

class ClientPolygonFeatureCollection(context: Context){
    val TAG = "PolygonCollection"
    var collection = FeatureCollection()
    var layer = FeatureCollectionLayer(collection)
    var features = mutableListOf<Feature>()
    private var name = "$$##"
    var spatialReference = SpatialReference.create(2039)
    var id = UUID.randomUUID().toString()
    var fields = mutableListOf<GrappiField>()
    private var fieldsArray = mutableListOf<Field>()
    private lateinit var featureCollectionTable: FeatureCollectionTable
    var lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, ContextCompat.getColor(context, R.color.white) ,2f)
    var fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.VERTICAL, ContextCompat.getColor(context, R.color.colorAccent), lineSymbol)
    var renderer = SimpleRenderer(fillSymbol)

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
    constructor(context: Context, json: JSONObject): this(context){
        this.spatialReference = MapProperties.spatialReference
        collection = FeatureCollection()
        layer = FeatureCollectionLayer(collection)
        layer.name = context.getString(R.string.my_polygon) + "$$##"
        this.fieldsArray = ClientLayersController.generateFieldsArray(json)
        this.fields = ClientLayersController.generateGrappiFields(json)
        this.featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYGON, spatialReference)
        this.featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        createFeaturesFromJSON(json)
        addFeatureList()
    }
    fun addFeatureList(){
        featureCollectionTable.addFeaturesAsync(features)
    }
    fun createFeaturesFromJSON(json: JSONObject){
        val features = json.getJSONArray("features")
        val gson = Gson()
        for (i in 0 until features.length()){
            val item = features.getJSONObject(i)
            item.getJSONObject("attributes").remove("OBJECTID")
            val geometry = item.getJSONObject("geometry")
            val pointsArray = ClientLayersController.generatePointsArray(SketcherEditorTypes.POLYGON, geometry, spatialReference)
            val pointsCollection = PointCollection(pointsArray)
            val polygon = PolygonBuilder(pointsCollection)
            val polygonGeometry = polygon.toGeometry()
            val attributesJSON = item.getJSONObject("attributes").toString()
            val attTypeToken = object : TypeToken<HashMap<String, Any>>() {}.type
            val mAttMap = gson.fromJson(attributesJSON, attTypeToken) as HashMap<String, Any>
            generateFeatureForJSON(mAttMap, polygonGeometry)
        }
    }
    fun generateFeatureForJSON(attributes: HashMap<String, Any>, geometry: Geometry){
        val feature = featureCollectionTable.createFeature(attributes, geometry) as Feature
        this.features.add(feature)
    }
    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        val formatArea = calculateArea(geometry)
        newAttributes["area"] = formatArea
        val feature = featureCollectionTable.createFeature(newAttributes,geometry) as Feature
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature)
    }
    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry, callback: ()-> Unit){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        val formatArea = calculateArea(geometry)
        newAttributes["area"] = formatArea
        val feature = featureCollectionTable.createFeature(newAttributes,geometry) as Feature
        features.add(feature)
        featureCollectionTable.addFeatureAsync(feature).addDoneListener(callback)
    }

    fun uploadJSON(callback: () -> Unit){
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polygon.json")
        val json = generateARCGISJSON().toString().toByteArray()
        childRef.putBytes(json).addOnSuccessListener {
            callback()
        }.addOnFailureListener{
            e->
            Log.d(TAG,e.toString())
        }
    }
    fun uploadJSON(callback: OnClientPolygonUploadFinished){
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polygon.json")
        val json = generateARCGISJSON().toString().toByteArray()
        childRef.putBytes(json).addOnSuccessListener {
            callback.onClientPolygonUploaded()
        }.addOnFailureListener{
            e->
            callback.onClientPolygonUploaded()
            Log.d(TAG,e.toString())
        }
    }

    private fun calculateArea(geometry: Geometry): String {
        val envelope = geometry.extent
        var area = GeometryEngine.area(envelope)
        val decimalFormat = DecimalFormat("#.00")
        val unit = spatialReference.unit.abbreviation
        if (unit == "mi") {
            area *= 1609.344
        }
        var formatArea = decimalFormat.format(area).toString() + "m²"
        if (formatArea == ".00m") formatArea = "0.00m"
        return formatArea
    }
    private fun initProperties(){
        val setFields = fields.toSet()
        fields.clear()
        fields = setFields.toMutableList()
        if (fields.size > 0){
            fieldsTransform()
        }
        featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POLYGON, spatialReference)
        featureCollectionTable.renderer = renderer
        collection.tables.add(featureCollectionTable)
    }

    private fun generateIDField(){
        fields.add(GrappiField("Id", "esriFieldTypeString", "Id", 50))

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

    fun generateARCGISJSON(): JSONObject{
        var resultJson = JSONObject()
        resultJson.put("displayFieldName", "")
        resultJson.put("fieldAliases", generateFieldAliasesForJSON())
        resultJson.put("geometryType", "esriGeometryPolygon")
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
                attributesJSON.put(it.name, attributesMap[it.name])
            }
            featureJSON.put("attributes", attributesJSON)
            val geometryJson = JSONObject(it.geometry.toJson())
            val geometryJSONArray = geometryJson.getJSONArray("rings")
            val geometryWrapperJSON = JSONObject().put("rings", geometryJSONArray)
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
    fun identifyFeatureById(id: String): Int{
        var count = 0
        features.forEach {
            if (it.attributes["Id"] == id) return count
            count++
        }
        return -1
    }
    fun editFeatureGeometry(id: String, geometry: Geometry, context: Context){
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val featureNum = identifyFeatureById(id)
        val editFeature = features[featureNum]
        val attributes = editFeature.attributes
        val newArea = calculateArea(geometry)
        attributes["area"] = newArea
        val newFeature = featureCollectionTable.createFeature(attributes,geometry) as Feature
        features.removeAt(featureNum)
        features.add(newFeature)
        featureCollectionTable.deleteFeatureAsync(editFeature).addDoneListener {
            featureCollectionTable.addFeatureAsync(newFeature).addDoneListener {
                uploadJSON{
                    progressDialog.dismiss()
                    Toast.makeText(context, context.resources.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun editFeatureAttributes(context: Context,id: String, attributes: HashMap<String, Any>){
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val featureNum = identifyFeatureById(id)
        val editFeature = features[featureNum]
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        newAttributes["imageURL"] = editFeature.attributes["imageURL"]!!
        newAttributes["area"] = editFeature.attributes["area"]!!
        val geometry = editFeature.geometry
        val newFeature = featureCollectionTable.createFeature(newAttributes, geometry) as Feature
        features.add(newFeature)
        features.removeAt(featureNum)
        featureCollectionTable.deleteFeatureAsync(editFeature).addDoneListener {
            featureCollectionTable.addFeatureAsync(newFeature).addDoneListener {
                uploadJSON {
                    progressDialog.dismiss()
                }
            }
        }
    }
    fun getFeatureGeometry(id: String):Geometry?{
        val featureNum = identifyFeatureById(id)
        if (featureNum >= 0){
            val mFeature = features[featureNum]
            return mFeature.geometry
        }
        return null
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
                    uploadJSON{
                        progressDialog.dismiss()
                        Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    features.removeAt(objectID)
                    uploadJSON{
                        progressDialog.dismiss()
                        Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                features.removeAt(objectID)
                uploadJSON{
                    progressDialog.dismiss()
                    Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
    fun editFeatureImage(context: Context, id: String, uri: Uri?){
        val featureNum = identifyFeatureById(id)
        uri?.let {mUri ->
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle(context.getString(R.string.updating_layer))
            progressDialog.setCancelable(false)
            progressDialog.show()
            val storage = FirebaseStorage.getInstance()
            val reference = storage.reference
            var ref = reference.child("settlements/" + ProjectId.projectId + "/images/" + UUID.randomUUID().toString())
            if (features[featureNum].attributes["imageURL"].toString().trim() != ""){
                val oldURL = features[featureNum].attributes["imageURL"] as String
                ref = storage.getReferenceFromUrl(oldURL)
                Picasso.get().invalidate(oldURL)
                val compressedImage = ClientPhotoController.reduceImageSize(mUri)
                compressedImage?.let{
                    ref.putFile(it).addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(context, context.getString(R.string.layer_updated),Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val feature = features[featureNum]
                val compressedImage = ClientPhotoController.reduceImageSize(mUri)
                compressedImage?.let{
                    ref.putFile(it).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            uri->
                            features[featureNum].attributes["imageURL"] = uri.toString()
                            featureCollectionTable.updateFeatureAsync(feature).addDoneListener {
                                uploadJSON{
                                    progressDialog.dismiss()
                                    Toast.makeText(context, context.getString(R.string.layer_updated),Toast.LENGTH_SHORT).show()
                                }

                            }

                        }

                    }
                }
            }

        }
    }
    interface OnClientPolygonUploadFinished{
        fun onClientPolygonUploaded()
    }
}