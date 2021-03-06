package com.grappiapp.grappygis.ClientLayersHandler

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.grappiapp.grappygis.ClientFeatureLayers.GrappiField
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.PointBuilder
import com.esri.arcgisruntime.geometry.SpatialReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.OfflineMode.OfflineModeController
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

object ClientLayersController {

    val TAG = "clientLayerCtrl"
    var localClientPolylineFile = File.createTempFile("polyline", "json")
    var localPointsFile = File.createTempFile("points", "json")
    var localPolygonFile = File.createTempFile("polygon", "json")
    lateinit var sharedPreferences: SharedPreferences

    fun fetchClientPolyline(context: Context, callback: OnClientLayersJSONDownloaded){
        sharedPreferences = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(Consts.DOES_OFFLINE_POLYLINE_DATA_EXIST, false)){
            val editor = sharedPreferences.edit()
            editor.putBoolean(Consts.DOES_OFFLINE_POLYLINE_DATA_EXIST, false)
            editor.apply()
            val json = OfflineModeController.getOfflineJSON(SketcherEditorTypes.POLYLINE)
            json?.let {
                callback.onClientPolylineJSONDownloaded(it)
                return
            }
            callback.onEmptyClientPolylineJSON()
            return
        }
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polyline.json")
        childRef.getFile(localClientPolylineFile).addOnSuccessListener {
            val json = JSONObject(generateJson(localClientPolylineFile))
            callback.onClientPolylineJSONDownloaded(json)
        }.addOnFailureListener{
            callback.onEmptyClientPolylineJSON()
        }
    }
    fun fetchClientPoints(callback: OnClientLayersJSONDownloaded){
        if (sharedPreferences.getBoolean(Consts.DOES_OFFLINE_POINT_DATA_EXIST, false)){
            val editor = sharedPreferences.edit()
            editor.putBoolean(Consts.DOES_OFFLINE_POINT_DATA_EXIST, false)
            editor.apply()
            val json = OfflineModeController.getOfflineJSON(SketcherEditorTypes.POINT)
            json?.let {
                callback.onClientPointsJSONDownloaded(it)
                return
            }
            callback.onEmptyClientPointsJSON()
            return
        }
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/points.json")
        childRef.getFile(localPointsFile).addOnSuccessListener {
            val json = JSONObject(generateJson(localPointsFile))
            callback.onClientPointsJSONDownloaded(json)
        }.addOnFailureListener{
            callback.onEmptyClientPointsJSON()
        }
    }

    fun fetchClientPolygon(callback: OnClientLayersJSONDownloaded){
        if (sharedPreferences.getBoolean(Consts.DOES_OFFLINE_POLYGON_DATA_EXIST, false)){
            val editor = sharedPreferences.edit()
            editor.putBoolean(Consts.DOES_OFFLINE_POLYGON_DATA_EXIST, false)
            editor.apply()
            val json = OfflineModeController.getOfflineJSON(SketcherEditorTypes.POLYGON)
            json?.let {
                callback.onClientPolygonJSONDownloaded(it)
                return
            }
            callback.onEmptyClientPolygon()
            return
        }
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polygon.json")
        childRef.getFile(localPolygonFile).addOnSuccessListener {
            val json = JSONObject(generateJson(localPolygonFile))
            callback.onClientPolygonJSONDownloaded(json)
        }.addOnFailureListener{
            callback.onEmptyClientPolygon()
        }
    }

    fun generateFieldsArray(json: JSONObject): MutableList<Field>{
        val gson = Gson()
//        val mapTypeToken = object: TypeToken<Map<String, Any>>(){}.type
//        val fieldAliases = json.getJSONObject("fieldAliases")
//        val aliases = gson.fromJson(fieldAliases.toString(),mapTypeToken) as Map<String, Any>
//        Log.d(TAG, aliases.toString())
        val fieldsArray = json.getJSONArray("fields")
        val typeToken = object: TypeToken<List<GrappiField>>(){}.type
        val fields = gson.fromJson(fieldsArray.toString(), typeToken) as List<GrappiField>
        val arcGISFields = fieldsTransform(fields)
        Log.d(TAG, fields.toString())
        return arcGISFields
    }

    fun generateGrappiFields(json: JSONObject): MutableList<GrappiField>{
        val gson = Gson()
        val fieldsArray = json.getJSONArray("fields")
        fieldsArray.remove(0)
        val typeToken = object: TypeToken<MutableList<GrappiField>>(){}.type
        val fields = gson.fromJson(fieldsArray.toString(), typeToken) as MutableList<GrappiField>
        return fields
    }


    fun fieldsTransform(fields: List<GrappiField>): MutableList<Field>{
        val fieldsArray = mutableListOf<Field>()
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
                it.type.contains("OID") -> {

                }
            }
        }
        return fieldsArray

    }
    fun generatePointsArray(sketcherEditorTypes: SketcherEditorTypes, json: JSONObject, spatialReference: SpatialReference):
    MutableList<com.esri.arcgisruntime.geometry.Point>{
        var pointsCollection = mutableListOf<com.esri.arcgisruntime.geometry.Point>()
        when (sketcherEditorTypes){
            SketcherEditorTypes.POLYLINE ->{
                val paths = json.getJSONArray("paths")
                for (i in 0 until paths.length()){
                    val innerArray = paths.getJSONArray(i)
                    for (j in 0 until innerArray.length()){
                        val x = innerArray.getJSONArray(j).get(0) as Double
                        val y = innerArray.getJSONArray(j).get(1) as Double
                        val point = PointBuilder(x, y).toGeometry()
                        Log.d(TAG, point.toString())
                        pointsCollection.add(point)
                    }
                }
            }
            SketcherEditorTypes.POLYGON ->{
                val rings = json.getJSONArray("rings")
                for (i in 0 until rings.length()){
                    val innerArray = rings.getJSONArray(i)
                    for (j in 0 until innerArray.length()){
                        val x = innerArray.getJSONArray(j).get(0) as Double
                        val y = innerArray.getJSONArray(j).get(1) as Double
                        val point = PointBuilder(x, y).toGeometry()
                        pointsCollection.add(point)
                    }
                }
            }
            SketcherEditorTypes.POINT -> {
                val x = json.getDouble("x")
                val y = json.getDouble("y")
                pointsCollection.add(com.esri.arcgisruntime.geometry.Point(x, y))
            }
        }
        return pointsCollection
    }

    private fun generateJson(file: File): String{
        val length = file.length().toInt()

        val bytes = ByteArray(length)

        val `in` = FileInputStream(file)
        try {
            `in`.read(bytes)
        } finally {
            `in`.close()
        }

        return String(bytes)
    }
    interface OnClientLayersJSONDownloaded{
        fun onClientPolylineJSONDownloaded(json: JSONObject)
        fun onEmptyClientPolylineJSON()
        fun onClientPointsJSONDownloaded(json: JSONObject)
        fun onEmptyClientPointsJSON()
        fun onClientPolygonJSONDownloaded(json: JSONObject)
        fun onEmptyClientPolygon()
    }
}