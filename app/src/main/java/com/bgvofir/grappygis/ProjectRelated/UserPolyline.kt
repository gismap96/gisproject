package com.bgvofir.grappygis.ProjectRelated

import android.util.Log
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientFeatureLayers.GrappiField

object UserPolyline{
    val TAG = "userPolyline"
    var userPolyline: ClientFeatureCollectionLayer? = null
    val grappiFields = mutableListOf<GrappiField>()

    fun initFields(){
        grappiFields.clear()
        grappiFields.add(GrappiField("category", "esriFieldTypeString", "סיווג", 255))
        grappiFields.add(GrappiField("description", "esriFieldTypeString", "תאור", 255))
        grappiFields.add(GrappiField("number", "esriFieldDouble", "מספר"))
        grappiFields.add(GrappiField("isUpdated","esriFieldTypeString","לעדכון", 6))
        Log.d(TAG, grappiFields.toString())
    }
}