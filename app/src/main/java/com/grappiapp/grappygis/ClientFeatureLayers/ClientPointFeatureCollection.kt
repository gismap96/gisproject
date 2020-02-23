package com.grappiapp.grappygis.ClientFeatureLayers

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.util.Log
import android.widget.Toast
import com.grappiapp.grappygis.ClientLayerPhotoController.ClientPhotoController
import com.grappiapp.grappygis.ClientLayersHandler.ClientLayersController
import com.grappiapp.grappygis.ProjectRelated.MapProperties
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
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
//    val pinBlackStarDrawable = BitmapDrawable(context.resources, getBitmapFromVectorDrawable(R.drawable.ic_star_black))
    var pictureMarkerSymbol = PictureMarkerSymbol(pinBlueStarDrawable)
    var renderer = SimpleRenderer(pictureMarkerSymbol)
    var WIDTH = 20f
    var HEIGHT = 20f

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
        layer.name = context.resources.getString(R.string.my_points) + "$$##"
        this.fieldsArray = ClientLayersController.generateFieldsArray(json)
        this.fields = ClientLayersController.generateGrappiFields(json)
        this.featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POINT, spatialReference)
        pictureMarkerSymbol.width = WIDTH
        pictureMarkerSymbol.height = HEIGHT
        this.featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
        createFeaturesFromJSON(json)
        addFeatureList()
    }

    private fun addFeatureList(){
        featureCollectionTable.addFeaturesAsync(features)
    }
    private fun createFeaturesFromJSON(json: JSONObject){
        val features = json.getJSONArray("features")
        val gson = Gson()
        for (i in 0 until features.length()) {
            val item = features.getJSONObject(i)
            item.getJSONObject("attributes").remove("OBJECTID")
            val geometry = item.getJSONObject("geometry")
            val pointsArray = ClientLayersController.generatePointsArray(SketcherEditorTypes.POINT, geometry, spatialReference)
            val point = pointsArray[0]
            val attributesJSON = item.getJSONObject("attributes").toString()
            val attTypeToken = object : TypeToken<HashMap<String, Any>>() {}.type
            val mAttMap = gson.fromJson(attributesJSON, attTypeToken) as HashMap<String, Any>
            addFeaturesFromJSON(mAttMap, point)
        }
    }
    private fun addFeaturesFromJSON(attributes: HashMap<String, Any>, point: Point){
//        var pictureMarker = PictureMarkerSymbol(pinBlackStarDrawable)
//        attributes["isUpdated"]?.let {
//            if (it == "yes") pictureMarker = PictureMarkerSymbol(pinBlueStarDrawable)
//        }
//        pictureMarker.height = 40f
//        pictureMarker.width = 40f
//        renderer.symbol = pictureMarker
        val feature = featureCollectionTable.createFeature(attributes,point) as Feature
        features.add(feature)
    }

    fun createFeature(attributes: HashMap<String, Any>,geometry: Geometry, callback: (()-> Unit)?){
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
//        var pictureMarker = PictureMarkerSymbol(pinBlackStarDrawable)
//        newAttributes["isUpdated"]?.let {
//            if (it == "yes") pictureMarker = PictureMarkerSymbol(pinBlueStarDrawable)
//        }
//        pictureMarker.height = 40f
//        pictureMarker.width = 40f
//        renderer.symbol = pictureMarker
//        renderer.symbol = pictureMarker
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
        pictureMarkerSymbol.width = WIDTH
        pictureMarkerSymbol.height = HEIGHT
        this.featureCollectionTable = FeatureCollectionTable(fieldsArray, GeometryType.POINT, spatialReference)
        this.featureCollectionTable.renderer = renderer
        this.collection.tables.add(featureCollectionTable)
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
                attributesJSON.put(it.name, attributesMap[it.name])
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

    fun identifyFeatureById(id: String): Int{
        var count = 0
        features.forEach {
            if (it.attributes["Id"] == id) return count
            count++
        }
        return -1
    }

    fun getFeatureGeometry(id: String):Geometry?{
        val featureNum = identifyFeatureById(id)
        if (featureNum >= 0){
            val mFeature = features[featureNum]
            return mFeature.geometry
        }
        return null
    }
    fun editFeatureGeometry(context: Context, layerId: String, geometry: Geometry){
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val featureNum = identifyFeatureById(layerId)
        val editFeature = features[featureNum]
        val attributes = editFeature.attributes
        val newFeature = featureCollectionTable.createFeature(attributes,geometry) as Feature
        features.removeAt(featureNum)
        features.add(newFeature)
        featureCollectionTable.deleteFeatureAsync(editFeature).addDoneListener {
            featureCollectionTable.addFeatureAsync(newFeature).addDoneListener {
                uploadJSON(object: OnPointsUploaded{
                    override fun onPointsUploadFinished() {
                        progressDialog.dismiss()
                        Toast.makeText(context, context.resources.getString(R.string.layer_updated),Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

    }

    fun editFeatureAttributes(context: Context, layerId: String, attributes: HashMap<String, Any>){
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val featureNum = identifyFeatureById(layerId)
        val editFeature = features[featureNum]
        val newAttributes = attributes.toMutableMap()
        newAttributes["Id"] = UUID.randomUUID().toString()
        newAttributes["imageURL"] = editFeature.attributes["imageURL"]!!
        val geometry = editFeature.geometry
        val newFeature = featureCollectionTable.createFeature(newAttributes, geometry) as Feature
        features.add(newFeature)
        features.removeAt(featureNum)
        featureCollectionTable.deleteFeatureAsync(editFeature).addDoneListener {
            featureCollectionTable.addFeatureAsync(newFeature).addDoneListener {
                uploadJSON(object: OnPointsUploaded{
                    override fun onPointsUploadFinished() {
                        progressDialog.dismiss()
                    }

                })
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
                                uploadJSON(object: OnPointsUploaded {
                                    override fun onPointsUploadFinished() {
                                        progressDialog.dismiss()
                                        Toast.makeText(context, context.getString(R.string.layer_updated),Toast.LENGTH_SHORT).show()
                                    }

                                })

                            }

                        }

                    }
                }
            }

        }
    }

    fun deleteFeature(layerId: String, context: Context){
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
                    uploadJSON(object: OnPointsUploaded{
                        override fun onPointsUploadFinished() {
                            progressDialog.dismiss()
                            Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                        }

                    })
                }.addOnFailureListener {
                    features.removeAt(objectID)
                    uploadJSON(object: OnPointsUploaded{
                        override fun onPointsUploadFinished() {
                            progressDialog.dismiss()
                            Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } else {
                features.removeAt(objectID)
                uploadJSON(object: OnPointsUploaded{
                    override fun onPointsUploadFinished() {
                        progressDialog.dismiss()
                        Toast.makeText(context, context.getString(R.string.layer_updated), Toast.LENGTH_SHORT).show()
                    }

                })
            }

        }
    }

    interface OnPointsUploaded{
        fun onPointsUploadFinished()
    }
}