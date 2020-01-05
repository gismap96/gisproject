package com.bgvofir.grappygis.FormatGeometry

import android.os.Build.ID
import android.util.Log
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.internal.jni.fa
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

object FormatJSONGeometry{


    val TAG = "JSONFormat"
    fun polygonToJson(geometry: Geometry){
        val result = JSONObject()
        result.put("displayFieldName", "meow")
        result.put("fieldAliases", fieldAlisas())
        result.put("geometryType", "esriGeometryPolygon")
        result.put("spatialReference", spatialReference())
        val fieldsElement = JSONArray()
        fieldsElement.put(fields("FID","esriFieldTypeOID"))
        fieldsElement.put(fields("Id", "esriFieldTypeInteger"))
        result.put("fields", fieldsElement)
        var features = JSONArray()
        var mFirstFeature = JSONObject()
        mFirstFeature.put("attributes",attributes(0,0))
        var geometryJSONObject = JSONObject()
        geometryJSONObject.put("rings", getRingsJsonObject(geometry))
        mFirstFeature.put("geometry", geometryJSONObject)

        features.put(mFirstFeature)
        result.put("features", features)
        Log.d(TAG, result.toString())
    }

    fun getRingsJsonObject(geometry: Geometry) : JSONArray {
        var geometryJson = JSONObject(geometry.toJson())
        return geometryJson.getJSONArray("rings")
    }

    fun fieldAlisas(): JSONObject{
        val fa = JSONObject()
        fa.put("FID", "FID")
        fa.put("Id", "Id")
        return fa
    }
    fun spatialReference():JSONObject{
        val sr = JSONObject()
        sr.put("wkid", 2039)
        sr.put("latestWkid", 2039)
        return sr
    }

    fun fields(twice: String,type: String): JSONObject{
        val fields = JSONObject()
        fields.put("name", twice)
        fields.put("type", type)
        fields.put("alias", twice)
        return fields
    }
    fun attributes(fid: Int, id: Int):JSONObject{
        val att = JSONObject()
        att.put("FID", fid)
        att.put("Id", id)
        return att
    }
//
//    class formattedJSONToShape(displayFieldName: String, fieldAliases: FieldAliases){
//        var displayFieldName = "displayFieldName"
//        var fieldAliases = fieldAliases
//        val geometryTryType = "esriGeometryPolygon"
//
//        fun toJson(){
//            var jsonObject = JsonObject()
//            jsonObject.addProperty(displayFieldName, "")
//        }
//    }
//
//    fun getFieldAl(FID: String, Id: String){
//        fun toJson(){
//            val fa = JsonObject()
//            fa.addProperty("FID", FID)
//            fa.addProperty("Id", Id)
//        }
//    }
//
//    class FieldAliases(FID: String, Id: String){
//        var FID = FID
//        var Id = Id
//        fun toJson(){
//            val fa = JsonObject()
//            fa.addProperty("FID", FID)
//            fa.addProperty("Id", Id)
//        }
//
//    }
//
//    class InnerSpatialReference(){
//        val wkid = 2039
//        val latestwkid = 2039
//    }
//
//    class InnerField(name: String, type: String, alias: String){
//        val name = name
//        val type = type
//        val alias = alias
//    }
//
//    class InnerFeatures(attributes: MutableList<InnerAttributes>, ring: JsonObject){
//        val attributes = attributes
//        val rings = ring
//    }
//
//    class InnerAttributes(){
//        val FID = 0
//        val Id = 0
//    }


}