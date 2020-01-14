package com.bgvofir.grappygis.ClientLayersHandler

import android.graphics.Point
import android.util.Log
import com.bgvofir.grappygis.ClientFeatureLayers.GrappiField
import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController.point
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController
import com.bgvofir.grappygis.ProjectRelated.ProjectId
import com.bgvofir.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.SpatialReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import javax.security.auth.callback.Callback

object ClientLayersController {

    val TAG = "clientLayerCtrl"
    var localClientPolylineFile = File.createTempFile("polyline", "json")

    fun fetchClientPolyline(callback: OnClientLayersJSONDownloaded){
        val mProjectId = ProjectId.projectId
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/polyline.json")
        childRef.getFile(localClientPolylineFile).addOnSuccessListener {
            val json = JSONObject(generateJson())
            callback.onClientPolylineJSONDownloaded(json)
//            val features = json.getJSONArray("features")
//            val gson = Gson()
//            val allAttributesMap = mutableListOf<Map<String, Any>>()
//            for (i in 0 until features.length()){
//                val item = features.getJSONObject(i)
//                val geometry = item.getJSONObject("geometry")
//                val pointsArray = generatePointsArray(SketcherEditorTypes.POLYLINE, geometry)
//                val attributesJSON = item.getJSONObject("attributes").toString()
//                val attTypeToken = object: TypeToken<Map<String, Any>>(){}.type
//                val mAttMap = gson.fromJson(attributesJSON, attTypeToken) as Map<String, Any>
//                allAttributesMap.add(mAttMap)
//
//            }
        }
    }

    fun generateFieldsArray(json: JSONObject): MutableList<Field>{
        val gson = Gson()
//        val mapTypeToken = object: TypeToken<Map<String, Any>>(){}.type
//        val fieldAliases = json.getJSONObject("fieldAliases")
//        val aliases = gson.fromJson(fieldAliases.toString(),mapTypeToken) as Map<String, Any>
//        Log.d(TAG, aliases.toString())
        val fieldsArray = json.getJSONArray("fields")
        val fields = gson.fromJson(fieldsArray.toString(), mutableListOf<GrappiField>()::class.java) as MutableList<GrappiField>
        val arcGISFields = fieldsTransform(fields)
        Log.d(TAG, fields.toString())
        return arcGISFields
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
                    var pathPoints = mutableListOf<Point>()
                    for (j in 0 until innerArray.length()){
                        val x = innerArray.getJSONArray(j).get(0) as Double
                        val y = innerArray.getJSONArray(j).get(1) as Double
                        val point = com.esri.arcgisruntime.geometry.Point(x, y, spatialReference)
                        pointsCollection.add(point)
                    }
                }
            }
            SketcherEditorTypes.POLYGON ->{

            }
        }
        return pointsCollection
    }

    private fun generateJson(): String{
        val length = localClientPolylineFile.length().toInt()

        val bytes = ByteArray(length)

        val `in` = FileInputStream(localClientPolylineFile)
        try {
            `in`.read(bytes)
        } finally {
            `in`.close()
        }

        return String(bytes)
    }
    interface OnClientLayersJSONDownloaded{
        fun onClientPolylineJSONDownloaded(json: JSONObject)
        fun onClientPolygonnJSONDownloaded(json: JSONObject)
    }
}