package com.bgvofir.grappygis.ProjectRelated

import android.util.Log
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientFeatureLayers.ClientPointFeatureCollection
import com.bgvofir.grappygis.ClientFeatureLayers.GrappiField

object UserPoints {
    val TAG = "userPoints"
    var userPoints: ClientPointFeatureCollection? = null
    val grappiFields = mutableListOf<GrappiField>()

    fun initFields(){
        grappiFields.clear()
        grappiFields.add(GrappiField("category", "esriFieldTypeString", "category", 255))
        grappiFields.add(GrappiField("description", "esriFieldTypeString", "description", 255))
        grappiFields.add(GrappiField("number", "esriFieldTypeDouble", "number"))
        grappiFields.add(GrappiField("isUpdated","esriFieldTypeString","isUpdated", 6))
        grappiFields.add(GrappiField("imageURL", "esriFieldTypeString","imageURL", 255))
        Log.d(TAG, grappiFields.toString())
    }
}