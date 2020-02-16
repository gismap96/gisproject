package com.grappiapp.grappygis.ProjectRelated

import android.util.Log
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPolygonFeatureCollection
import com.grappiapp.grappygis.ClientFeatureLayers.GrappiField

object UserPolygon {

    val TAG = "userPolyline"
    var userPolygon: ClientPolygonFeatureCollection? = null
    val grappiFields = mutableListOf<GrappiField>()

    fun initFields(){
        grappiFields.clear()
        grappiFields.add(GrappiField("category", "esriFieldTypeString", "category", 255))
        grappiFields.add(GrappiField("description", "esriFieldTypeString", "description", 255))
        grappiFields.add(GrappiField("number", "esriFieldTypeString", "number", 255))
        grappiFields.add(GrappiField("isUpdated","esriFieldTypeString","isUpdated", 6))
        grappiFields.add(GrappiField("imageURL", "esriFieldTypeString","imageURL", 255))
        grappiFields.add(GrappiField("area", "esriFieldTypeString","area", 255))
        Log.d(UserPolyline.TAG, UserPolyline.grappiFields.toString())
    }
}