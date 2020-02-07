package com.grappiapp.grappygis.FormatGeometry

import android.util.Log
import com.esri.arcgisruntime.data.FeatureCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject

object FormatJSONCollectionFeature {
    val TAG = "formatJSONCollection"
    val firebaseStorage = FirebaseStorage.getInstance()

    fun pointToJson(feature: FeatureCollection, mProjectId: String){
        val jsonObj = JSONObject(feature.toJson())
        val jsonArray1 = jsonObj.getJSONArray("layers")
        val layer1 = JSONObject(jsonArray1.get(0).toString())
        val json = layer1.getJSONObject("layerDefinition").toString().toByteArray()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/JSON/$username.json")

        childRef.putBytes(json).addOnSuccessListener {
            Log.d(TAG, "file sent")
        }

    }
}