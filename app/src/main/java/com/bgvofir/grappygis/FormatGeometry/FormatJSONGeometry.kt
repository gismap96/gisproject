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


    fun polygonToJson(geometry: Geometry){
        val result = JSONObject()
        result.put("fieldAliases", fieldAlisas())
        result.put("geometryType", "esriGeometryPolygon")
        result.put("spatialReference", spatialReference())
        val fieldsElement = JsonArray()
        fieldsElement.add(fields("FID","esriFieldTypeOID"))
        fieldsElement.add(fields("Id", "esriFieldTypeInteger"))
        result.put("fields", fieldsElement)
        val features = JsonArray()
        val mFirstFeature = JSONObject()
        mFirstFeature.put("attributes",attributes(0,0))
        mFirstFeature.put("rings", getRingsJsonObject(geometry))
    }

    fun getRingsJsonObject(geometry: Geometry) : JSONArray {
        var geometryJson = JSONObject(geometry.toJson())
        return geometryJson.getJSONArray("rings")
    }

    fun fieldAlisas(): JsonElement{
        val fa = JsonObject()
        fa.addProperty("FID", "FID")
        fa.addProperty("Id", "Id")
        return fa
    }
    fun spatialReference():JsonElement{
        val sr = JsonObject()
        sr.addProperty("wkid", 2039)
        sr.addProperty("latestWkid", 2039)
        return sr
    }

    fun fields(twice: String,type: String): JsonElement{
        val fields = JsonObject()
        fields.addProperty("name", twice)
        fields.addProperty("type", type)
        fields.addProperty("alias", twice)
        return fields
    }
    fun attributes(fid: Int, id: Int):JsonElement{
        val att = JsonObject()
        att.addProperty("FID", fid)
        att.addProperty("Id", id)
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